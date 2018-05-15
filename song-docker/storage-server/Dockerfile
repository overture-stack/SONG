FROM song_base-ubuntu:latest
MAINTAINER ICGC <dcc-support@icgc.org>

#
# Configuration
#
ENV SCORE_SERVER_VERSION 1.1.1
ENV SCORE_HOME $DCC_HOME/score-server
ENV SCORE_LOGS $SCORE_HOME/logs

#rtisma-20171002 Temp Hack untill new release
ENV DOWNLOAD_URL https://artifacts.oicr.on.ca/artifactory/dcc-release/bio/overture/score-server/${SCORE_SERVER_VERSION}/score-server-${SCORE_SERVER_VERSION}-dist.tar.gz

ENV TARBALL $DCC_HOME/score-server.tar.gz
ENV DCC_SCRIPTS $DCC_HOME/scripts

ADD config $DCC_CONFIG
ADD scripts $DCC_SCRIPTS


ENV EXPAND_SCRIPT $DCC_HOME/tools/expand.py
ENV INPUT_FILE $DCC_CONFIG/application.yml.template
ENV OUTPUT_FILE $SCORE_HOME/conf/application.yml

RUN wget $DOWNLOAD_URL -O $TARBALL && \
	    tar zxvf $TARBALL && \
	    mv -f $DCC_HOME/score-server-*  $SCORE_HOME && \
	    rm -rf $TARBALL $SCORE_HOME/conf/application.* && \
	    mv $SCORE_LOGS  ${SCORE_LOGS}.bak  && \
	    ln -s $DCC_HOME/storage_server_logs $SCORE_LOGS

		#ln -s $SCORE_HOME/conf/application.yml $SCORE_HOME/conf/application-secure.yml  && \
		#ln -s $SCORE_HOME/conf/application.yml $SCORE_HOME/conf/application-prod.yml  && \
		#ln -s $SCORE_HOME/conf/application.yml $SCORE_HOME/conf/application-collaboratory.yml  && \
#        wrapper.app.parameter.5=--spring.profiles.active=collaboratory,prod,secure \
CMD python3 $EXPAND_SCRIPT $INPUT_FILE $OUTPUT_FILE && \
	/bin/bash -c $DCC_SCRIPTS/create-buckets.sh && \
        $SCORE_HOME/bin/dcc-storage-server start  \
	wrapper.java.additional.2=-Dcom.sun.management.jmxremote.port=${STORAGE_SERVER_JMX_PORT} \
        wrapper.app.parameter.6=--server.port=${STORAGE_SERVER_PORT} \
        wrapper.app.parameter.5=--spring.profiles.active=amazon,collaboratory,prod,secure \
        ;FOR_100_YEARS=$((100*365*24*60*60));while true;do sleep $FOR_100_YEARS;done

