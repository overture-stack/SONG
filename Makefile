PG_PASSWORD = "password"
PG_HOST = "localhost"
PG_PORT = 8082

SONG_SERVER_URL="http://localhost:8080"
SONG_ACCESS_TOKEN="ad83ebde-a55c-11e7-abc4-cec278b6b50a"
SONG_STUDY_ID="ABC123"

start-db:
	@docker-compose up -d --no-deps db

fresh-db: start-db
	@docker-compose exec db psql -U postgres postgres -c 'drop database song;'
	@docker-compose exec db psql -U postgres postgres -c 'create database song;'

run-flyway-migration: 
	@mvn package -DskipTests
	@cd song-server && mvn flyway:migrate  -Dflyway.url=jdbc:postgresql://localhost:8082/song?stringtype=unspecified -Dflyway.user=postgres -Dflyway.password=password -Dflyway.locations=classpath:db/migration

login-psql:
	@PGPASSWORD=$(PG_PASSWORD) psql -h $(PG_HOST) -p $(PG_PORT)  -U postgres song

containerized-login-psql:
	@docker-compose exec -e "PGPASSWORD=$(PG_PASSWORD)" db psql -h $(PG_HOST) -p $(PG_PORT)  -U postgres song

format:
	@mvn fmt:format

build-server:
	@mvn package -DskipTests -pl song-server -am 

build-core:
	@mvn package -DskipTests -pl song-core -am 

build-client:
	@mvn package -DskipTests -pl song-client -am 

analyze:
	@mvn dependency:analyze-report

package-client:
	@mvn package -pl song-client -am 

package-server:
	@mvn package -pl song-server -am 

build-sdk:
	@mvn package -DskipTests -pl song-java-sdk -am 

clean:
	@mvn clean

default_study:
	@curl \
	-vvv \
	-H "Authorization: Bearer $(SONG_ACCESS_TOKEN)" \
	-H "Content-Type: application/json" \
	-H "accept: */*" \
	-X POST \
	-d "@test_study" \
	"$(SONG_SERVER_URL)/studies/$(SONG_STUDY_ID)/"