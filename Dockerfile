FROM openjdk:8-jdk-alpine as builder

# Build song-server jar
COPY . /srv
WORKDIR /srv
RUN ./mvnw clean package -DskipTests

###############################################################################################################
FROM song_base-ubuntu:latest as client

ENV DCC_HOME /opt/dcc
ENV DCC_DATA $DCC_HOME/data
ENV DCC_TOOLS $DCC_HOME/tools
ENV DCC_CONFIG $DCC_HOME/config
ENV CLIENT_HOME $DCC_DATA
ENV TARBALL $DCC_HOME/download.tar.gz

RUN apt install -y jq
COPY song-docker-demo/client/config/* $DCC_CONFIG/

ENV SAVE_STUDY_SCRIPT $DCC_TOOLS/save_study.sh
ENV EXPAND_SCRIPT $DCC_TOOLS/expand.py
ENV INPUT_FILE  $DCC_CONFIG/application.yml.template
ENV OUTPUT_FILE  $CLIENT_HOME/conf/application.yml

COPY --from=builder /srv/song-client/target/song-client-*-dist.tar.gz $TARBALL

RUN cd $DCC_HOME && \
    tar zxvf $TARBALL && \
    mv -f $DCC_HOME/song-client-* $DCC_HOME/song-client

CMD rm -rf $CLIENT_HOME/* && \
		cp -rf $DCC_HOME/song-client/* $CLIENT_HOME && \
		python3 $EXPAND_SCRIPT $INPUT_FILE $OUTPUT_FILE && \
		$SAVE_STUDY_SCRIPT $SERVER_URL $CLIENT_STUDY_ID $AUTH_TOKEN 

###############################################################################################################

FROM openjdk:8-jre-alpine as server

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
