#!/usr/bin/env bash
#
# Builds the song-server and song-client images from the root Dockerfile and checks that they run:
#   - song-server starts with the production profiles and its CMD, and answers GET /isAlive
#   - song-client runs `sing ping` against it and prints "true"
#
# Everything runs on a private Docker network with a throwaway Postgres container. The server
# trusts a throwaway RSA key, and sing authenticates with a JWT signed by it, because the server
# validates any bearer token it receives, even on routes that need none. Containers, the network
# and the keys are removed on exit. Exits non-zero when any check fails.
#
# Requires docker, curl and openssl.
#
# Usage: dev/docker-smoke-test.sh

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
RUN_ID="song-smoke-$$"
NETWORK="${RUN_ID}"
DB_CONTAINER="${RUN_ID}-db"
SERVER_CONTAINER="${RUN_ID}-server"
SERVER_IMAGE="song-server:smoke-test"
CLIENT_IMAGE="song-client:smoke-test"
# Same image the root docker-compose.yml uses for song-db.
POSTGRES_IMAGE="postgres:11.1"
STARTUP_TIMEOUT_SECONDS=180
# Under target/ so it is ignored by git and inside a directory Docker Desktop can mount.
KEY_DIR="${ROOT_DIR}/target/${RUN_ID}"

cleanup() {
  docker rm -f "${SERVER_CONTAINER}" "${DB_CONTAINER}" >/dev/null 2>&1 || true
  docker network rm "${NETWORK}" >/dev/null 2>&1 || true
  rm -rf "${KEY_DIR}"
}
trap cleanup EXIT

base64_url() {
  openssl base64 -e -A | tr '+/' '-_' | tr -d '='
}

# Prints an RS256 JWT, valid for an hour, signed with the key in $1.
signed_jwt() {
  local private_key="$1"
  local now
  now="$(date +%s)"
  local header='{"alg":"RS256","typ":"JWT"}'
  local payload="{\"sub\":\"smoke-test\",\"iat\":${now},\"exp\":$((now + 3600)),\"context\":{\"scope\":[]}}"
  local signing_input
  signing_input="$(printf '%s' "${header}" | base64_url).$(printf '%s' "${payload}" | base64_url)"
  local signature
  signature="$(printf '%s' "${signing_input}" | openssl dgst -sha256 -sign "${private_key}" | base64_url)"
  printf '%s.%s' "${signing_input}" "${signature}"
}

mkdir -p "${KEY_DIR}"
openssl genrsa -out "${KEY_DIR}/private.pem" 2048 2>/dev/null
openssl rsa -in "${KEY_DIR}/private.pem" -pubout -out "${KEY_DIR}/public.pem" 2>/dev/null
# The server runs as a non-root user, so the mounted key must be world-readable.
chmod 644 "${KEY_DIR}/public.pem"
ACCESS_TOKEN="$(signed_jwt "${KEY_DIR}/private.pem")"

echo "Building ${SERVER_IMAGE} and ${CLIENT_IMAGE}"
docker build --target server -t "${SERVER_IMAGE}" "${ROOT_DIR}"
docker build --target client -t "${CLIENT_IMAGE}" "${ROOT_DIR}"

docker network create "${NETWORK}" >/dev/null

echo "Starting Postgres"
docker run -d --name "${DB_CONTAINER}" --network "${NETWORK}" \
  -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=password -e POSTGRES_DB=song \
  "${POSTGRES_IMAGE}" >/dev/null
until docker exec "${DB_CONTAINER}" pg_isready -U postgres -d song >/dev/null 2>&1; do
  sleep 1
done

echo "Starting song-server with its image CMD"
docker run -d --name "${SERVER_CONTAINER}" --network "${NETWORK}" -p 127.0.0.1::8080 \
  -v "${KEY_DIR}/public.pem:/song-server/public.pem:ro" \
  -e SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_PUBLIC_KEY_LOCATION="file:/song-server/public.pem" \
  -e SPRING_PROFILES_ACTIVE="prod,secure,default" \
  -e SPRING_DATASOURCE_URL="jdbc:postgresql://${DB_CONTAINER}/song?stringtype=unspecified" \
  -e SPRING_DATASOURCE_USERNAME=postgres \
  -e SPRING_DATASOURCE_PASSWORD=password \
  "${SERVER_IMAGE}" >/dev/null

SERVER_PORT="$(docker port "${SERVER_CONTAINER}" 8080/tcp | head -1 | sed 's/.*://')"
echo "Waiting for http://localhost:${SERVER_PORT}/isAlive"
deadline=$((SECONDS + STARTUP_TIMEOUT_SECONDS))
until [ "$(curl -s "http://localhost:${SERVER_PORT}/isAlive" || true)" = "true" ]; do
  if [ "$(docker inspect -f '{{.State.Running}}' "${SERVER_CONTAINER}")" != "true" ]; then
    echo "FAIL: song-server exited during startup"
    docker logs "${SERVER_CONTAINER}" | tail -50
    exit 1
  fi
  if [ "${SECONDS}" -ge "${deadline}" ]; then
    echo "FAIL: song-server did not answer /isAlive within ${STARTUP_TIMEOUT_SECONDS}s"
    docker logs "${SERVER_CONTAINER}" | tail -50
    exit 1
  fi
  sleep 2
done
echo "PASS: song-server answers GET /isAlive"

# `sing` is run through PATH, as users do; the launcher resolves its home from its own path.
echo "Running sing ping"
ping_output="$(docker run --rm --network "${NETWORK}" \
  -e CLIENT_SERVER_URL="http://${SERVER_CONTAINER}:8080" \
  -e CLIENT_ACCESS_TOKEN="${ACCESS_TOKEN}" \
  -e CLIENT_STUDY_ID="ABC123" \
  "${CLIENT_IMAGE}" sing ping 2>&1)" || {
  echo "FAIL: sing ping exited with an error"
  echo "${ping_output}"
  exit 1
}
if ! echo "${ping_output}" | grep -q "true"; then
  echo "FAIL: sing ping did not print true"
  echo "${ping_output}"
  exit 1
fi
echo "PASS: sing ping prints true"
