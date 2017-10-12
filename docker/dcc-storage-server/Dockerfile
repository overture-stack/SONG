FROM java:8-jre
MAINTAINER ICGC <dcc-support@icgc.org>

#
# Configuration
#

ENV CONTAINER_NAME dcc-storage-server
ENV DCC_HOME /opt/dcc
ENV DCC_STORAGE_HOME $DCC_HOME/dcc-storage-server
ENV DCC_STORAGE_LOGS $DCC_STORAGE_HOME/logs
RUN useradd -m dcc &&  mkdir -p $DCC_HOME
WORKDIR $DCC_HOME

EXPOSE 8080

ADD config $DCC_HOME/config

ENV TARBALL $DCC_HOME/${CONTAINER_NAME}.tar.gz

ADD https://artifacts.oicr.on.ca/artifactory/dcc-release/org/icgc/dcc/dcc-storage-server/[RELEASE]/dcc-storage-server-[RELEASE]-dist.tar.gz $TARBALL
RUN tar zxvf $TARBALL && \
    mv -f $DCC_HOME/dcc-storage-server-*  $DCC_STORAGE_HOME && \
    rm -rf $TARBALL $DCC_STORAGE_HOME/conf/application.* && \
    ln -s $DCC_HOME/config/application.properties $DCC_STORAGE_HOME/conf/application.properties

CMD $DCC_STORAGE_HOME/bin/dcc-storage-server start \
    ;FOR_100_YEARS=$((100*365*24*60*60));while true;do sleep $FOR_100_YEARS;done

# CMD $DCC_STORAGE_HOME/bin/dcc-storage-server start \
# 	set.SPRING_CONFIG_LOCATION=$DCC_HOME/config/ \
# 	set.LOGGING_PATH=$DCC_STORAGE_LOGS \
# 	set.LOG_PATH=$DCC_STORAGE_LOGS \
# 	set.SPRING_PROFILES_ACTIVE=dev,test; FOR_100_YEARS=$((100*365*24*60*60));while true;do sleep $FOR_100_YEARS;done

