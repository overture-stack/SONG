FROM song_base-ubuntu:latest
MAINTAINER ICGC <dcc-support@icgc.org>

ENV CLIENT_HOME $DCC_DATA
ENV DOWNLOAD_URL  https://artifacts.oicr.on.ca/artifactory/dcc-release/org/icgc/dcc/song-client/[RELEASE]/song-client-[RELEASE]-dist.tar.gz
ENV TARBALL $DCC_HOME/download.tar.gz

ADD config $DCC_CONFIG
RUN wget $DOWNLOAD_URL -O  $TARBALL && \
    tar zxvf $TARBALL && \
    mv -f $DCC_HOME/song-client-* $DCC_HOME/song-client
    

CMD rm -rf $CLIENT_HOME/* && \
        cp -rf $DCC_HOME/song-client/* $CLIENT_HOME && \
	sed -e "s/accessToken:.*/accessToken: ${AUTH_TOKEN}/; s/studyId:.*/studyId: ${CLIENT_STUDY_ID}/; s/serverUrl:.*/serverUrl: http://${SERVER_HOST}:${SERVER_PORT}/" $DCC_CONFIG/application.yml > $CLIENT_HOME/conf/application.yml

        #cp -f $DCC_CONFIG/application.yml $CLIENT_HOME/conf/application.yml
