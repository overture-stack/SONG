
start-db:
	@docker-compose up -d --no-deps db

fresh-db: start-db
	@docker-compose exec db psql -U postgres postgres -c 'drop database song;'
	@docker-compose exec db psql -U postgres postgres -c 'create database song;'

run-flyway-migration: 
	@mvn package -DskipTests
	@cd song-server && mvn flyway:migrate  -Dflyway.url=jdbc:postgresql://localhost:8082/song?stringtype=unspecified -Dflyway.user=postgres -Dflyway.password=password -Dflyway.locations=classpath:db/migration

login-psql:
	@PGPASSWORD=password psql -h localhost -p 8082  -U postgres song

format:
	@mvn fmt:format

build-server:
	@mvn package -DskipTests -pl song-server -am 

build-core:
	@mvn package -DskipTests -pl song-core -am 
	
build-client:
	@mvn package -DskipTests -pl song-client -am 

clean:
	@mvn clean
