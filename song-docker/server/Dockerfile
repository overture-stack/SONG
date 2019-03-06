FROM song_base-ubuntu:latest
MAINTAINER OVERTURE <contact@overture.bio>

#
# Configuration
#
ENV SERVER_RELEASE_VERSION 1.4.1
ENV SONG_HOME $DCC_HOME/song-server
ENV SONG_LOGS $DCC_HOME/song-server/logs
ENV DOWNLOAD_URL  https://artifacts.oicr.on.ca/artifactory/dcc-release/bio/overture/song-server/${SERVER_RELEASE_VERSION}/song-server-${SERVER_RELEASE_VERSION}-dist.tar.gz  
ENV TARBALL $DCC_HOME/song-server.tar.gz

ADD config $DCC_CONFIG

ENV WAIT_FOR_POSTGRES_SCRIPT $DCC_HOME/tools/wait-for-postgres.sh
ENV EXPAND_SCRIPT $DCC_HOME/tools/expand.py
ENV INPUT_FILE  $DCC_CONFIG/application-secure.properties.template
ENV OUTPUT_FILE $SONG_HOME/conf/application-secure.properties

RUN apt install -y postgresql && \
        wget $DOWNLOAD_URL -O $TARBALL && \
	    tar zxvf $TARBALL &&  \
	    mv -f $DCC_HOME/song-server-*  $DCC_HOME/song-server && \
	    rm -rf $TARBALL && \
	    mv $DCC_HOME/song-server/logs  $SONG_HOME/logs.bak && \
	    ln -s $DCC_HOME/server_logs $SONG_HOME/logs

#Note: must be parameter.4 becuase parameter.4 in jar/conf/wrapper.conf has --spring.profiles=active=dev,test. 
#      If you do not override parameter.4, you will get dev,test profiles IN ADDITION TO any other specified profiles
#      need to override it
	    #wrapper.app.parameter.6=--server.port=${SERVER_PORT} \
CMD chmod 755 $WAIT_FOR_POSTGRES_SCRIPT && \
	    $WAIT_FOR_POSTGRES_SCRIPT ${DB_HOST} ${DB_PORT} ${DB_USER} ${DB_PASSWORD} && \
	    python3 $EXPAND_SCRIPT $INPUT_FILE $OUTPUT_FILE && \
	    $SONG_HOME/bin/song-server start \
            wrapper.java.additional.2=-Dcom.sun.management.jmxremote.port=${SERVER_JMX_PORT} \
	    wrapper.app.parameter.4=--spring.profiles.active=prod,secure,default \
	    set.LOGGING_PATH=$SONG_LOGS \
	    set.LOG_PATH=$SONG_LOGS; \
	    FOR_100_YEARS=$((100*365*24*60*60));while true;do sleep $FOR_100_YEARS;done
