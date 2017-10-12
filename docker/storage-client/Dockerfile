FROM icgc/icgc-storage-client:latest
MAINTAINER ICGC <dcc-support@icgc.org>

ENV DCC_HOME /opt/dcc
ENV DCC_DATA $DCC_HOME/data
ADD conf $DCC_HOME/conf
CMD cp -r /icgc/icgc-storage-client/* $DCC_DATA && \
    rm -f $DCC_DATA/conf/application.properties && \
    cp -f $DCC_HOME/conf/application-aws.properties $DCC_DATA/conf/application-aws.properties
