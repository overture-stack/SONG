FROM song_base_db:latest
MAINTAINER ICGC <dcc-support@icgc.org>

ENV DCC_HOME /opt/dcc
RUN useradd -m dcc &&  mkdir -p $DCC_HOME
WORKDIR $DCC_HOME

ENV PGDATA /var/lib/postgresql/data/pgdata
ENV POSTGRES_DB=dcc_identifier
ENV POSTGRES_USER=postgres
ENV POSTGRES_PASSWORD=password
ENV POSTGRES_INIT_SQL /docker-entrypoint-initdb.d/init.sql

ENV DOWNLOAD_URL  https://artifacts.oicr.on.ca/artifactory/dcc-release/org/icgc/dcc/dcc-id-server/[RELEASE]/dcc-id-server-[RELEASE].jar 
ENV JAR $DCC_HOME/dcc-id-server.jar
RUN wget $DOWNLOAD_URL -O $JAR && \
    unzip -p dcc-id-server.jar BOOT-INF/classes/sql/schema.sql > $POSTGRES_INIT_SQL
