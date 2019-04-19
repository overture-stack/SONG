FROM openjdk:8-jdk-alpine as root

RUN apk update && apk upgrade && \
    apk add --no-cache maven

###############################################################################################################

FROM root as builder

# Build song-server jar
WORKDIR /srv
COPY . /srv
RUN mvn package -DskipTests

###############################################################################################################

FROM root as server

# Paths
ENV SONG_HOME /song-server
ENV SONG_LOGS $SONG_HOME/logs
ENV JAR_FILE            /song-server.jar
ENV RESOURCES /srv/song-server/src/main/resources
ENV FLYWAY_DIR $RESOURCES/db/migration
RUN mkdir -p $RESOURCES /srv/song
COPY . /srv/song
COPY --from=builder /srv/song-server/target/song-server-*-exec.jar $JAR_FILE

WORKDIR $SONG_HOME

CMD mkdir -p  $SONG_HOME $SONG_LOGS \
		&& cd /srv/song/song-server \
		&& mvn  -Dflyway.locations=db/migration -Dflyway.user=${SPRING_DATASOURCE_USERNAME} -Dflyway.password=${SPRING_DATASOURCE_PASSWORD} -Dflyway.url=${SPRING_DATASOURCE_URL} "flyway:migrate" \
        && java -Dlog.path=$SONG_LOGS \
        -jar $JAR_FILE \
        --spring.config.location=classpath:/application.yml \
        --spring.profiles.active=prod,secure,default
