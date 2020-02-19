###############################
# Maven builder
###############################
# -alpine-slim image does not support --release flag
FROM adoptopenjdk/openjdk11:jdk-11.0.6_10-alpine as builder

# Build song-server jar
COPY . /srv
WORKDIR /srv
RUN ./mvnw clean package -DskipTests

###############################
# Song Client
###############################
FROM adoptopenjdk/openjdk11:jre-11.0.6_10-alpine as client

ENV SONG_CLIENT_HOME   /song-client
ENV CLIENT_DIST_DIR    /song-client-dist
ENV JAVA_HOME /opt/java/openjdk
ENV PATH $PATH:$SONG_CLIENT_HOME/bin
ENV SONG_USER song
ENV SONG_UID 9999
ENV SONG_GID 9999

RUN addgroup -S -g $SONG_GID $SONG_USER  \
    && adduser -S -u $SONG_UID -G $SONG_USER $SONG_USER  \
    && mkdir $SONG_CLIENT_HOME \
    && chown -R $SONG_UID:$SONG_GID $SONG_CLIENT_HOME \
	&& apk add bash

COPY --from=builder /srv/song-client/target/song-client-*-dist.tar.gz /song-client.tar.gz

RUN tar zxvf song-client.tar.gz -C /tmp \
	&& rm -rf song-client.tar.gz \
    && mv -f /tmp/song-client-*  /tmp/song-client-dist  \
    && cp -r /tmp/song-client-dist $CLIENT_DIST_DIR \
	&& mkdir -p $CLIENT_DIST_DIR/logs \
	&& touch $CLIENT_DIST_DIR/logs/client.log \
	&& chmod 777 $CLIENT_DIST_DIR/logs/client.log \
	&& mv $CLIENT_DIST_DIR/* $SONG_CLIENT_HOME \
	&& chown -R $SONG_UID:$SONG_GID $SONG_CLIENT_HOME

# Set working directory for convenience with interactive usage
WORKDIR $SONG_CLIENT_HOME

###############################
# Song Server
###############################
FROM adoptopenjdk/openjdk11:jre-11.0.6_10-alpine as server

# Paths
ENV SONG_HOME /song-server
ENV SONG_LOGS $SONG_HOME/logs
ENV JAR_FILE            /song-server.jar
ENV SONG_USER song
ENV SONG_UID 9999
ENV SONG_GID 9999

RUN addgroup -S -g $SONG_GID $SONG_USER  \
    && adduser -S -u $SONG_UID -G $SONG_USER $SONG_USER  \
    && mkdir -p $SONG_HOME $SONG_LOGS \
    && chown -R $SONG_UID:$SONG_GID $SONG_HOME

COPY --from=builder /srv/song-server/target/song-server-*-exec.jar $JAR_FILE

USER $SONG_UID

WORKDIR $SONG_HOME

CMD java -Dlog.path=$SONG_LOGS \
        -jar $JAR_FILE \
        --spring.config.location=classpath:/bootstrap.properties,classpath:/application.yml
