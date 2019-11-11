PG_PASSWORD = "password"
PG_HOST = "localhost"
PG_PORT = 8082


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
