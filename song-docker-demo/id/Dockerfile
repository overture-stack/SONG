FROM song_base-ubuntu:latest
MAINTAINER OVERTURE <contact@overture.bio>

ENV ID_RELEASE_VERSION 5.3.0
ENV ID_HOME $DCC_HOME/dcc-id-server
ENV ID_SCRIPTS $DCC_HOME/scripts
ENV TARBALL $DCC_HOME/dcc-id-server.tar.gz
ENV DOWNLOAD_URL https://artifacts.oicr.on.ca/artifactory/dcc-release/org/icgc/dcc/dcc-id-server/${ID_RELEASE_VERSION}/dcc-id-server-${ID_RELEASE_VERSION}-dist.tar.gz

ADD config $DCC_CONFIG
ADD scripts $ID_SCRIPTS

ENV WAIT_FOR_POSTGRES_SCRIPT $DCC_HOME/tools/wait-for-postgres.sh
ENV EXPAND_SCRIPT $DCC_HOME/tools/expand.py
ENV INPUT_FILE  $DCC_CONFIG/application-production.properties.template
ENV OUTPUT_FILE $ID_HOME/conf/application-production.properties

        #cp -f $DCC_CONFIG/logback.xml $ID_HOME/conf/logback.xml && \
RUN  cd $DCC_HOME && \
		apt install -y postgresql && \
        wget $DOWNLOAD_URL -O $TARBALL && \
        tar zxvf $TARBALL && \
        rm -rf $TARBALL && \
        mv $DCC_HOME/dcc-id-server-* $ID_HOME && \
        rm -rf $ID_HOME/logs && \
        ln -s $DCC_HOME/id_logs $ID_HOME/logs
 
CMD $WAIT_FOR_POSTGRES_SCRIPT ${POSTGRES_HOST} ${POSTGRES_PORT} ${POSTGRES_USER}  ${POSTGRES_PASSWORD} && \
        python3 $EXPAND_SCRIPT  $INPUT_FILE $OUTPUT_FILE && \
        $ID_SCRIPTS/run.sh; \
        FOR_100_YEARS=$((100*365*24*60*60));while true;do sleep $FOR_100_YEARS;done

