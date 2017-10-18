FROM song_base_ubuntu:latest
MAINTAINER ICGC <dcc-support@icgc.org>

#
# Configuration
#
ENV SONG_HOME $DCC_HOME/song-server
ENV SONG_LOGS $DCC_HOME/song-server/logs
ENV DOWNLOAD_URL  https://artifacts.oicr.on.ca/artifactory/dcc-release/org/icgc/dcc/song-server/[RELEASE]/song-server-[RELEASE]-dist.tar.gz  
ENV TARBALL $DCC_HOME/song-server.tar.gz

ADD config $DCC_CONFIG

RUN wget $DOWNLOAD_URL -O $TARBALL && \
	    tar zxvf $TARBALL &&  \
	    mv -f $DCC_HOME/song-server-*  song-server && \
	    rm -rf $TARBALL && \
	    cp -f $DCC_CONFIG/wrapper.conf $DCC_HOME/song-server/conf/ && \
	    mv $DCC_HOME/song-server/logs  $DCC_HOME/song-server/logs.bak && \
	    ln -s $DCC_HOME/server_logs $DCC_HOME/song-server/logs

CMD $DCC_HOME/song-server/bin/song-server start \
       wrapper.java.additional.6=-Dserver.port=${SERVER_PORT} \
       wrapper.java.additional.7=-Ddcc-storage.url=http://${STORAGE_SERVER_HOST}:${STORAGE_SERVER_PORT} \
       wrapper.java.additional.8=-Dmanagement.port=${SERVER_MANAGEMENT_PORT} \
       set.SPRING_CONFIG_LOCATION=$DCC_CONFIG \
       set.LOGGING_PATH=$SONG_LOGS \
       set.LOG_PATH=$SONG_LOGS \
       set.SPRING_PROFILES_ACTIVE=prod,secure; FOR_100_YEARS=$((100*365*24*60*60));while true;do sleep $FOR_100_YEARS;done
