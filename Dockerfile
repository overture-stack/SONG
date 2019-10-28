FROM openjdk:11-jdk as builder

# Build song-server jar
COPY . /srv
WORKDIR /srv
RUN ./mvnw clean package -DskipTests

###############################################################################################################
FROM openjdk:11-jre-stretch as client

ENV DCC_HOME /opt/dcc
ENV DCC_DATA $DCC_HOME/data
ENV DCC_TOOLS $DCC_HOME/tools

RUN apt update &&  \
    apt install -y python3 curl jq && \ 
    mkdir -p $DCC_HOME

ENV DCC_CONFIG $DCC_HOME/config
ENV CLIENT_HOME $DCC_DATA
ENV TARBALL $DCC_HOME/download.tar.gz

COPY song-docker-demo/client/config/* $DCC_CONFIG/

ENV SAVE_STUDY_SCRIPT $DCC_TOOLS/save_study.sh
ENV EXPAND_SCRIPT $DCC_TOOLS/expand.py
ENV INPUT_FILE  $DCC_CONFIG/application.yml.template
ENV OUTPUT_FILE  $CLIENT_HOME/conf/application.yml

COPY --from=builder /srv/song-client/target/song-client-*-dist.tar.gz $TARBALL

RUN cd $DCC_HOME && \
    tar zxvf $TARBALL && \
    mv -f $DCC_HOME/song-client-* $DCC_HOME/song-client

WORKDIR $SONG_HOME/song-client

CMD rm -rf $CLIENT_HOME/* && \
		cp -rf $DCC_HOME/song-client/* $CLIENT_HOME && \
		python3 $EXPAND_SCRIPT $INPUT_FILE $OUTPUT_FILE
		


###############################################################################################################

FROM openjdk:11-jre as server

# Paths
ENV SONG_HOME /song-server
ENV SONG_LOGS $SONG_HOME/logs
ENV JAR_FILE            /song-server.jar

COPY --from=builder /srv/song-server/target/song-server-*-exec.jar $JAR_FILE

WORKDIR $SONG_HOME

CMD mkdir -p  $SONG_HOME $SONG_LOGS \
        && java -Dlog.path=$SONG_LOGS \
        -jar $JAR_FILE \
        --spring.config.location=classpath:/application.yml
