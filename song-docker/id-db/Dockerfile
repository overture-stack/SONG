FROM song_base-db:latest
MAINTAINER OVERTURE <contact@overture.bio>

ENV ID_RELEASE_VERSION 5.1.4
ENV PGDATA /var/lib/postgresql/data/pgdata
ENV POSTGRES_INIT_SQL /docker-entrypoint-initdb.d/init.sql
ENV DOWNLOAD_URL  https://artifacts.oicr.on.ca/artifactory/dcc-release/org/icgc/dcc/dcc-id-server/${ID_RELEASE_VERSION}/dcc-id-server-${ID_RELEASE_VERSION}.jar 
ENV JARFILE $DCC_HOME/dcc-id-server.jar

RUN wget $DOWNLOAD_URL -O $JARFILE && \
    unzip -p dcc-id-server.jar BOOT-INF/classes/sql/schema.sql > $POSTGRES_INIT_SQL
