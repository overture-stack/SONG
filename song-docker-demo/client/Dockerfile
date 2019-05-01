FROM song_base-ubuntu:latest
MAINTAINER OVERTURE <contact@overture.bio>

ENV CLIENT_RELEASE_VERSION 1.4.1
ENV CLIENT_HOME $DCC_DATA
ENV DOWNLOAD_URL  https://artifacts.oicr.on.ca/artifactory/dcc-release/bio/overture/song-client/${CLIENT_RELEASE_VERSION}/song-client-${CLIENT_RELEASE_VERSION}-dist.tar.gz
ENV TARBALL $DCC_HOME/download.tar.gz

ADD config $DCC_CONFIG

ENV SAVE_STUDY_SCRIPT $DCC_HOME/tools/save_study.sh
ENV EXPAND_SCRIPT $DCC_HOME/tools/expand.py
ENV INPUT_FILE  $DCC_CONFIG/application.yml.template
ENV OUTPUT_FILE  $CLIENT_HOME/conf/application.yml

RUN cd $DCC_HOME && \
	wget $DOWNLOAD_URL -O  $TARBALL && \
    tar zxvf $TARBALL && \
    mv -f $DCC_HOME/song-client-* $DCC_HOME/song-client

CMD rm -rf $CLIENT_HOME/* && \
        cp -rf $DCC_HOME/song-client/* $CLIENT_HOME && \
        python3 $EXPAND_SCRIPT $INPUT_FILE $OUTPUT_FILE && \
		$SAVE_STUDY_SCRIPT $SERVER_URL $CLIENT_STUDY_ID $AUTH_TOKEN 


