FROM java:8-jre
MAINTAINER ICGC <dcc-support@icgc.org>

#
# Configuration
#
ENV DCC_HOME /opt/dcc
ENV SONG_HOME $DCC_HOME/song-server
ENV SONG_LOGS $DCC_HOME/song-server/logs
RUN useradd -m dcc &&  mkdir -p $DCC_HOME
WORKDIR $DCC_HOME

EXPOSE 8080

ADD config $DCC_HOME/config 

ADD https://artifacts.oicr.on.ca/artifactory/dcc-release/org/icgc/dcc/song-server/[RELEASE]/song-server-[RELEASE]-dist.tar.gz  $DCC_HOME/song-server.tar.gz
RUN tar zxvf $DCC_HOME/song-server.tar.gz && mv -f $DCC_HOME/song-server-*  song-server
RUN rm -rf $DCC_HOME/song-server.tar.gz
RUN cp -f $DCC_HOME/config/wrapper.conf $DCC_HOME/song-server/conf/
RUN mv $DCC_HOME/song-server/logs  $DCC_HOME/song-server/logs.bak
RUN ln -s $DCC_HOME/ext_logs $DCC_HOME/song-server/logs

CMD $DCC_HOME/song-server/bin/song-server start \
	set.SPRING_CONFIG_LOCATION=$DCC_HOME/config/ \
	set.LOGGING_PATH=$SONG_LOGS \
	set.LOG_PATH=$SONG_LOGS \
	set.SPRING_PROFILES_ACTIVE=dev,test; FOR_100_YEARS=$((100*365*24*60*60));while true;do sleep $FOR_100_YEARS;done

