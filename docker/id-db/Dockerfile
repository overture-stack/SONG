FROM song_base-db:latest
MAINTAINER ICGC <dcc-support@icgc.org>

ENV PGDATA /var/lib/postgresql/data/pgdata
ENV POSTGRES_INIT_SQL /docker-entrypoint-initdb.d/init.sql
ENV DOWNLOAD_URL  https://artifacts.oicr.on.ca/artifactory/dcc-release/org/icgc/dcc/dcc-id-server/[RELEASE]/dcc-id-server-[RELEASE].jar 
ENV JARFILE $DCC_HOME/dcc-id-server.jar

RUN wget $DOWNLOAD_URL -O $JARFILE && \
    unzip -p dcc-id-server.jar BOOT-INF/classes/sql/schema.sql > $POSTGRES_INIT_SQL
