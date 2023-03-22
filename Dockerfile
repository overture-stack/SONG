###############################
# Maven builder
###############################
# -alpine and -alpine-slim image does not support --release flag nor arm arch
FROM adoptopenjdk/openjdk11:jdk-11.0.6_10-slim as builder

# Build song-server jar
COPY . /srv
WORKDIR /srv
RUN ./mvnw clean package -DskipTests

###############################
# Song Client
###############################
# -alpine image only works on amd64 arch
FROM adoptopenjdk/openjdk11:jre-11.0.6_10 as client

ENV CLIENT_DIST_DIR   /song-client-dist
ENV JAVA_HOME /opt/java/openjdk
ENV SONG_HOME   /song-client
ENV SONG_UID 9999
ENV SONG_GID 9999
ENV SONG_USER song
ENV SONG_LOGS $SONG_HOME/logs
ENV PATH $PATH:$SONG_HOME/bin

RUN addgroup --system --gid $SONG_GID $SONG_USER  \
    && adduser --system --uid $SONG_UID --ingroup $SONG_USER $SONG_USER  \
    && mkdir -p $SONG_HOME $SONG_LOGS \
    && chown -R $SONG_UID:$SONG_GID $SONG_HOME \
	&& apt-get update && apt-get install -y bash jq

COPY --from=builder /srv/song-client/target/song-client-*-dist.tar.gz /song-client.tar.gz

RUN tar zxvf song-client.tar.gz -C /tmp \
	&& rm -rf song-client.tar.gz \
    && mv -f /tmp/song-client-*  /tmp/song-client-dist  \
    && cp -r /tmp/song-client-dist $CLIENT_DIST_DIR \
	&& mv $CLIENT_DIST_DIR/* $SONG_HOME \
	&& rm -rf $CLIENT_DIST_DIR \
	&& mkdir -p $SONG_LOGS \
	&& chmod 777 -R $SONG_LOGS \
	&& chown -R $SONG_USER:$SONG_USER $SONG_HOME

USER $SONG_USER

# Set working directory for convenience with interactive usage
WORKDIR $SONG_HOME

###############################
# Song Server
###############################
FROM adoptopenjdk/openjdk11:jre-11.0.6_10 as server

# Paths
ENV SONG_HOME /song-server
ENV SONG_LOGS $SONG_HOME/logs
ENV JAR_FILE            /song-server.jar
ENV SONG_USER song
ENV SONG_UID 9999
ENV SONG_GID 9999

RUN addgroup --system --gid $SONG_GID $SONG_USER  \
    && adduser --system --uid $SONG_UID --ingroup $SONG_USER $SONG_USER  \
    && mkdir -p $SONG_HOME $SONG_LOGS \
    && chown -R $SONG_UID:$SONG_GID $SONG_HOME

COPY --from=builder /srv/song-server/target/song-server-*-exec.jar $JAR_FILE

USER $SONG_UID

WORKDIR $SONG_HOME

CMD java -Dlog.path=$SONG_LOGS \
        -jar $JAR_FILE \
        --spring.config.location=classpath:/bootstrap.properties,classpath:/application.yml
