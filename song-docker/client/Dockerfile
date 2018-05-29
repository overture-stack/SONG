FROM song_base-ubuntu:latest
MAINTAINER ICGC <dcc-support@icgc.org>

ENV CLIENT_RELEASE_VERSION 1.0.3
ENV CLIENT_HOME $DCC_DATA
ENV DOWNLOAD_URL  https://artifacts.oicr.on.ca/artifactory/dcc-release/org/icgc/dcc/song-client/${CLIENT_RELEASE_VERSION}/song-client-${CLIENT_RELEASE_VERSION}-dist.tar.gz
ENV TARBALL $DCC_HOME/download.tar.gz

ADD config $DCC_CONFIG

ENV EXPAND_SCRIPT $DCC_HOME/tools/expand.py
ENV INPUT_FILE  $DCC_CONFIG/application.yml.template
ENV OUTPUT_FILE  $CLIENT_HOME/conf/application.yml

RUN wget $DOWNLOAD_URL -O  $TARBALL && \
    tar zxvf $TARBALL && \
    mv -f $DCC_HOME/song-client-* $DCC_HOME/song-client

CMD rm -rf $CLIENT_HOME/* && \
        cp -rf $DCC_HOME/song-client/* $CLIENT_HOME && \
        python3 $EXPAND_SCRIPT $INPUT_FILE $OUTPUT_FILE

