FROM java:8-jre
MAINTAINER ICGC <dcc-support@icgc.org>

#
# Configuration
#
ENV DCC_HOME /opt/dcc
ENV DCC_STORAGE_HOME $DCC_HOME/dcc-storage-server
ENV DCC_STORAGE_LOGS $DCC_STORAGE_HOME/logs
RUN useradd -m dcc &&  mkdir -p $DCC_HOME
WORKDIR $DCC_HOME

EXPOSE 9000

ADD config $DCC_HOME/config

ENV TARBALL $DCC_HOME/dcc-storage-server.tar.gz

ADD https://artifacts.oicr.on.ca/artifactory/dcc-release/org/icgc/dcc/dcc-storage-server/[RELEASE]/dcc-storage-server-[RELEASE]-dist.tar.gz $TARBALL
RUN tar zxvf $TARBALL && \
    mv -f $DCC_HOME/dcc-storage-server-*  $DCC_STORAGE_HOME && \
    rm -rf $TARBALL $DCC_STORAGE_HOME/conf/application.* && \
    ln -s $DCC_HOME/config/application.properties $DCC_STORAGE_HOME/conf/application.properties  && \
    ln -s $DCC_HOME/config/application.properties $DCC_STORAGE_HOME/conf/application-secure.properties  && \
    ln -s $DCC_HOME/config/application.properties $DCC_STORAGE_HOME/conf/application-prod.properties  && \
    ln -s $DCC_HOME/config/application.properties $DCC_STORAGE_HOME/conf/application-collaboratory.properties  && \
    mv $DCC_STORAGE_LOGS  ${DCC_STORAGE_LOGS}.bak  && \
    ln -s $DCC_HOME/storage_server_logs $DCC_STORAGE_LOGS

CMD $DCC_STORAGE_HOME/bin/dcc-storage-server start  \
    wrapper.app.parameter.5=--spring.profiles.active=collaboratory,prod,secure \
    ;FOR_100_YEARS=$((100*365*24*60*60));while true;do sleep $FOR_100_YEARS;done


