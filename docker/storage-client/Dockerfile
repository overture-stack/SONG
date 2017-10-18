FROM icgc/icgc-storage-client:latest
MAINTAINER ICGC <dcc-support@icgc.org>

ADD config $DCC_CONFIG

CMD cp -r /icgc/icgc-storage-client/* $DCC_DATA && \
    rm -f $DCC_DATA/conf/application.properties && \
    cp -f $DCC_CONFIG/application-aws.properties $DCC_DATA/conf/application-aws.properties
