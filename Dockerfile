FROM openjdk:8-jdk-alpine as builder 

RUN apk update && apk upgrade && \
    apk add --no-cache maven

# Build song-server jar
WORKDIR /srv
COPY . /srv
RUN mvn package -DskipTests

###############################################################################################################

FROM openjdk:8-jre-alpine

# Paths
ENV SONG_HOME /song-server
ENV SONG_LOGS $SONG_HOME/logs
ENV JAR_FILE            /song-server.jar

COPY --from=builder /srv/song-server/target/song-server-*-exec.jar $JAR_FILE

WORKDIR $SONG_HOME

CMD mkdir -p  $SONG_HOME $SONG_LOGS \
        && java -Dlog.path=$SONG_LOGS \
        -jar $JAR_FILE \
        --spring.config.location=classpath:/application.yml \
        --spring.profiles.active=prod,secure,default

#&& FOR_100_YEARS=$((100*365*24*60*60));while true;do sleep $FOR_100_YEARS;done
