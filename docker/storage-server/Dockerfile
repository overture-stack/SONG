FROM song_base_ubuntu:latest
MAINTAINER ICGC <dcc-support@icgc.org>

#
# Configuration
#
ENV DCC_STORAGE_HOME $DCC_HOME/dcc-storage-server
ENV DCC_STORAGE_LOGS $DCC_STORAGE_HOME/logs
ENV DOWNLOAD_URL https://artifacts.oicr.on.ca/artifactory/dcc-release/org/icgc/dcc/dcc-storage-server/[RELEASE]/dcc-storage-server-[RELEASE]-dist.tar.gz
ENV TARBALL $DCC_HOME/dcc-storage-server.tar.gz
ENV MODIFIED_APPLICATION_PROP /tmp/modified-application.properties

ADD config $DCC_CONFIG

EXPOSE $STORAGE_SERVER_PORT

# Modify template application.properties file with values defined in environment variables
RUN cp -f $DCC_CONFIG/application.properties $MODIFIED_APPLICATION_PROP && \
	sed -i "s/s3.accessKey=.*/s3.accessKey=${MINIO_ACCESS_KEY}/" $MODIFIED_APPLICATION_PROP && \
	sed -i "s/s3.secretKey=.*/s3.secretKey=${MINIO_SECRET_KEY}/" $MODIFIED_APPLICATION_PROP && \
	sed -i "s/s3.endpoint=.*/s3.endpoint=http:\/\/${MINIO_HOST}:${MINIO_PORT}/" $MODIFIED_APPLICATION_PROP && \
	sed -i "s/auth.server.url=.*/auth.server.url=http:\/\/${AUTH_HOST}:${AUTH_PORT}\/${AUTH_ENDPOINT}/" $MODIFIED_APPLICATION_PROP && \
	sed -i "s/auth.server.clientId=.*/auth.server.clientId=${AUTH_SERVER_CLIENTID}/" $MODIFIED_APPLICATION_PROP && \
	sed -i "s/auth.server.clientSecret=.*/auth.server.clientSecret=${AUTH_SERVER_CLIENTSECRET}/" $MODIFIED_APPLICATION_PROP && \
	sed -i "s/metadata.url=.*/metadata.url=http:\/\/${SERVER_HOST}:${SERVER_PORT}/" $MODIFIED_APPLICATION_PROP && \
	sed -i "s/server.port=.*/server.port=${STORAGE_SERVER_PORT}/" $MODIFIED_APPLICATION_PROP


RUN wget $DOWNLOAD_URL -O $TARBALL && \
	    tar zxvf $TARBALL && \
	    mv -f $DCC_HOME/dcc-storage-server-*  $DCC_STORAGE_HOME && \
	    rm -rf $TARBALL $DCC_STORAGE_HOME/conf/application.* && \
            cp -f $MODIFIED_APPLICATION_PROP $DCC_STORAGE_HOME/conf/application.properties && \
	    ln -s $DCC_STORAGE_HOME/conf/application.properties $DCC_STORAGE_HOME/conf/application-secure.properties  && \
	    ln -s $DCC_STORAGE_HOME/conf/application.properties $DCC_STORAGE_HOME/conf/application-prod.properties  && \
	    ln -s $DCC_STORAGE_HOME/conf/application.properties $DCC_STORAGE_HOME/conf/application-collaboratory.properties  && \
	    mv $DCC_STORAGE_LOGS  ${DCC_STORAGE_LOGS}.bak  && \
	    ln -s $DCC_HOME/storage_server_logs $DCC_STORAGE_LOGS

CMD $DCC_STORAGE_HOME/bin/dcc-storage-server start  \
    wrapper.app.parameter.5=--spring.profiles.active=collaboratory,prod,secure \
    ;FOR_100_YEARS=$((100*365*24*60*60));while true;do sleep $FOR_100_YEARS;done


