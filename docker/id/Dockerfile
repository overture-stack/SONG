FROM song_base-ubuntu:latest
MAINTAINER ICGC <dcc-support@icgc.org>

ENV POSTGRES_HOST $ID_DB_HOST
ENV POSTGRES_PORT $ID_DB_PORT
ENV POSTGRES_DB $ID_DB_NAME
ENV POSTGRES_USER $ID_DB_USER
ENV POSTGRES_PASSWORD $ID_DB_PASSWORD

ENV ID_HOME $DCC_HOME/dcc-id-server
ENV TARBALL $DCC_HOME/dcc-id-server.tar.gz
ENV DOWNLOAD_URL https://artifacts.oicr.on.ca/artifactory/dcc-release/org/icgc/dcc/dcc-id-server/[RELEASE]/dcc-id-server-[RELEASE]-dist.tar.gz

ADD init.sh $DCC_HOME/init.sh

#rtisma EXPOSE $POSTGRES_PORT

RUN wget $DOWNLOAD_URL -O $TARBALL && \
        tar zxvf $TARBALL && \
        mv $DCC_HOME/dcc-id-server-* $ID_HOME && \
        rm -rf $TARBALL $ID_HOME/logs && \
        ln -s $DCC_HOME/id_logs $ID_HOME/logs
 
CMD $DCC_HOME/init.sh;FOR_100_YEARS=$((100*365*24*60*60));while true;do sleep $FOR_100_YEARS;done

