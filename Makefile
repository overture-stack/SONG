VERSION := 1.4.1
DOCKERFILE_SERVER := Dockerfile.server
DOCKER_CONTAINER_NAME := song-server-$(VERSION)
DOCKER_IMAGE_NAME := overture/song-server:$(VERSION)

help:
	@grep '^[A-Za-z0-9_-]\+:.*' ./Makefile | sed 's/:.*//'

docker-server-clean:
	docker kill $(DOCKER_CONTAINER_NAME)

docker-server-purge: docker-server-clean
	docker rmi $(DOCKER_IMAGE_NAME)

docker-server-logs:
	docker logs $(DOCKER_CONTAINER_NAME)

docker-server-build: $(DOCKERFILE_SERVER)
	docker build --build-arg SONG_VERSION=${VERSION} --build-arg SONG_ARTIFACTORY_REPO_NAME=dcc-release -f $(DOCKERFILE_SERVER) -t $(DOCKER_IMAGE_NAME) ./

docker-server-run: docker-server-build
	docker run  \
		--rm  \
		--log-driver json-file \
		--detach \
		--network host \
		--name $(DOCKER_CONTAINER_NAME) \
		-e SERVER_PORT=8080 \
		-e AUTH_SERVER_URL=http://localhost:8084/check_token/ \
		-e AUTH_SERVER_CLIENTID=3kJhz9pNtC0pFHAxr2SPkUkGjXrkWWqGcnPC0vBP \
		-e AUTH_SERVER_CLIENTSECRET=v9mjRtuEVwpt7cgqnsq6mxtCa5FbUOpKLGh7WX8a1dWbBKfrM3iV3VYMtE60jr3W7GLWtNeYIaJ8EUxPkaInclWVXf64qKdR3IKwyfpDU7JhvWEwIYQYdwV1YAUZjB2e \
		-e AUTH_SERVER_UPLOADSCOPE=collab.upload \
		-e AUTH_SERVER_DOWNLOADSCOPE=collab.download \
		-e SCORE_URL=http://localhost:8087 \
		-e MANAGEMENT_SERVER_PORT=8081 \
		-e ID_IDURL=http://localhost:8086 \
		-e ID_AUTHTOKEN=ad83ebde-a55c-11e7-abc4-cec278b6b50a \
		-e ID_REALIDS=true \
		-e SPRING_DATASOURCE_USERNAME=postgres \
		-e SPRING_DATASOURCE_PASSWORD=password \
		-e SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:8082/song?stringtype=unspecified \
		$(DOCKER_IMAGE_NAME)

