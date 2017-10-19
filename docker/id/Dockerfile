FROM song_base-ubuntu:latest
MAINTAINER ICGC <dcc-support@icgc.org>

ENV ID_HOME $DCC_HOME/dcc-id-server
ENV TARBALL $DCC_HOME/dcc-id-server.tar.gz
ENV DOWNLOAD_URL https://artifacts.oicr.on.ca/artifactory/dcc-release/org/icgc/dcc/dcc-id-server/[RELEASE]/dcc-id-server-[RELEASE]-dist.tar.gz

ADD run.sh $DCC_HOME/run.sh

RUN wget $DOWNLOAD_URL -O $TARBALL && \
        tar zxvf $TARBALL && \
        mv $DCC_HOME/dcc-id-server-* $ID_HOME && \
        rm -rf $TARBALL $ID_HOME/logs && \
        ln -s $DCC_HOME/id_logs $ID_HOME/logs
 
CMD $DCC_HOME/run.sh;FOR_100_YEARS=$((100*365*24*60*60));while true;do sleep $FOR_100_YEARS;done

