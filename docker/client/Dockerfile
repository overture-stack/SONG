From java
MAINTAINER ICGC <dcc-support@icgc.org>


ENV DCC_HOME /opt/dcc
WORKDIR $DCC_HOME
ENV CLIENT_HOME $DCC_HOME/data
ENV TARBALL $DCC_HOME/download.tar.gz

ENV POSTGRES_HOST id-db
ENV POSTGRES_PORT 5432
ENV POSTGRES_DB dcc_identifier
ENV POSTGRES_USER postgres
ENV POSTGRES_PASSWORD password

ADD https://artifacts.oicr.on.ca/artifactory/dcc-release/org/icgc/dcc/song-client/[RELEASE]/song-client-[RELEASE]-dist.tar.gz  $TARBALL
ADD conf $DCC_HOME/conf
RUN tar zxvf $TARBALL && \
        mv -f $DCC_HOME/song-client-* $DCC_HOME/song-client

CMD rm -rf $CLIENT_HOME/* && \
        cp -rf $DCC_HOME/song-client/* $CLIENT_HOME && \
        cp -f $DCC_HOME/conf/application.yml $CLIENT_HOME/conf/application.yml

