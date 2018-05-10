FROM song_base-db:latest
MAINTAINER ICGC <dcc-support@icgc.org>

ENV SERVER_RELEASE_VERSION 1.0.1
ENV PGDATA /var/lib/postgresql/data/pgdata
ENV POSTGRES_INIT_SQL /docker-entrypoint-initdb.d/init.sql

ENV DOWNLOAD_URL https://artifacts.oicr.on.ca/artifactory/dcc-release/org/icgc/dcc/song-server/${SERVER_RELEASE_VERSION}/song-server-${SERVER_RELEASE_VERSION}.jar 
ENV JAR  $DCC_HOME/song-server.jar
ENV INIT_FILE /docker-entrypoint-initdb.d/init.sh

RUN wget $DOWNLOAD_URL -O $JAR && \
        unzip -p song-server.jar schema.sql > $POSTGRES_INIT_SQL && \
        echo "INSERT INTO STUDY (id,name,description,organization) VALUES ('ABC123','DS-1','First Dummy Study','Sample Data Research Institute');" >> $POSTGRES_INIT_SQL


