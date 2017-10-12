FROM postgres:9.6
MAINTAINER ICGC <dcc-support@icgc.org>

ENV DCC_HOME /opt/dcc
RUN useradd -m dcc &&  mkdir -p $DCC_HOME
WORKDIR $DCC_HOME

ENV PGDATA /var/lib/postgresql/data/pgdata
ENV POSTGRES_DB=dcc_identifier
ENV POSTGRES_USER=postgres
ENV POSTGRES_PASSWORD=password
ENV POSTGRES_INIT_SQL /docker-entrypoint-initdb.d/init.sql

ADD https://artifacts.oicr.on.ca/artifactory/dcc-release/org/icgc/dcc/dcc-id-server/[RELEASE]/dcc-id-server-[RELEASE].jar $DCC_HOME/dcc-id-server.jar
RUN apt update && \
    apt install -y unzip && \
    unzip -p dcc-id-server.jar BOOT-INF/classes/sql/schema.sql > $POSTGRES_INIT_SQL
