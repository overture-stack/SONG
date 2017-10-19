FROM song_base-ubuntu:latest
MAINTAINER ICGC <dcc-support@icgc.org>

#
# Configuration
#
ENV SONG_HOME $DCC_HOME/song-server
ENV SONG_LOGS $DCC_HOME/song-server/logs
ENV DOWNLOAD_URL  https://artifacts.oicr.on.ca/artifactory/dcc-release/org/icgc/dcc/song-server/[RELEASE]/song-server-[RELEASE]-dist.tar.gz  
ENV TARBALL $DCC_HOME/song-server.tar.gz

ADD config $DCC_CONFIG

ENV EXPAND_SCRIPT $DCC_HOME/tools/expand.py
ENV INPUT_FILE  $SONG_HOME/conf/application.yml.template
ENV OUTPUT_FILE $SONG_HOME/conf/application.yml

RUN wget $DOWNLOAD_URL -O $TARBALL && \
	    tar zxvf $TARBALL &&  \
	    mv -f $DCC_HOME/song-server-*  $DCC_HOME/song-server && \
	    rm -rf $TARBALL && \
	    cp -f $DCC_CONFIG/* $SONG_HOME/conf/ && \
	    mv $DCC_HOME/song-server/logs  $SONG_HOME/logs.bak && \
	    ln -s $DCC_HOME/server_logs $SONG_HOME/logs

CMD  python3 $EXPAND_SCRIPT $INPUT_FILE $OUTPUT_FILE && \
       $SONG_HOME/bin/song-server start \
       set.LOGGING_PATH=$SONG_LOGS \
       set.LOG_PATH=$SONG_LOGS \
       set.SPRING_PROFILES_ACTIVE=prod,secure; FOR_100_YEARS=$((100*365*24*60*60));while true;do sleep $FOR_100_YEARS;done
