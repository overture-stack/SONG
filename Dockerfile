FROM openjdk:11-jdk as builder

# Build song-server jar
COPY . /srv
WORKDIR /srv
RUN ./mvnw clean package -DskipTests

###############################################################################################################
FROM openjdk:11-jre-stretch as client

ENV SONG_CLIENT_HOME   /song-client
ENV CLIENT_DIST_DIR    /song-client-dist
ENV PATH /usr/local/openjdk-11/bin:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:$SONG_CLIENT_HOME/bin
ENV SONG_USER song
ENV SONG_UID 9999

RUN useradd -r -u $SONG_UID $SONG_USER  \
    && mkdir $SONG_CLIENT_HOME \
    && chown -R $SONG_USER $SONG_CLIENT_HOME

COPY --from=builder /srv/song-client/target/song-client-*-dist.tar.gz /song-client.tar.gz

RUN tar zxvf song-client.tar.gz -C /tmp \
	&& rm -rf song-client.tar.gz \
    && mv -f /tmp/song-client-*  /tmp/song-client-dist  \
    && cp -r /tmp/song-client-dist $CLIENT_DIST_DIR \
	&& mkdir -p $CLIENT_DIST_DIR/logs \
	&& touch $CLIENT_DIST_DIR/logs/client.log \
	&& chmod 777 $CLIENT_DIST_DIR/logs/client.log \
	&& mkdir -p $SONG_CLIENT_HOME \
	&& mv $CLIENT_DIST_DIR/* $SONG_CLIENT_HOME 

# Set working directory for convenience with interactive usage
WORKDIR $SONG_CLIENT_HOME

###############################################################################################################

FROM openjdk:11-jre as server

# Paths
ENV SONG_HOME /song-server
ENV SONG_LOGS $SONG_HOME/logs
ENV SONG_USER song
ENV SONG_UID 9999
ENV JAR_FILE            /song-server.jar

RUN useradd -r -u $SONG_UID $SONG_USER  \
    && mkdir $SONG_HOME \
    && chown -R $SONG_USER $SONG_HOME


COPY --from=builder /srv/song-server/target/song-server-*-exec.jar $JAR_FILE

WORKDIR $SONG_HOME

CMD mkdir -p  $SONG_HOME $SONG_LOGS \
        && java -Dlog.path=$SONG_LOGS \
        -jar $JAR_FILE \
        --spring.config.location=classpath:/bootstrap.properties,classpath:/application.yml
