FROM postgres:9.6
MAINTAINER ICGC <dcc-support@icgc.org>

ENV DCC_HOME /opt/dcc
RUN useradd -m dcc &&  mkdir -p $DCC_HOME
WORKDIR $DCC_HOME

ENV PGDATA /var/lib/postgresql/data/pgdata
ENV POSTGRES_DB=song
ENV POSTGRES_USER=postgres
ENV POSTGRES_PASSWORD=password
ENV POSTGRES_INIT_SQL /docker-entrypoint-initdb.d/init.sql

ENV DOWNLOAD_URL https://artifacts.oicr.on.ca/artifactory/dcc-release/org/icgc/dcc/song-server/[RELEASE]/song-server-[RELEASE].jar 
ENV JAR  $DCC_HOME/song-server.jar
RUN apt update && \
    apt install -y unzip wget && \
    wget $DOWNLOAD_URL -O $JAR && \
    unzip -p song-server.jar schema.sql > $POSTGRES_INIT_SQL
RUN echo "INSERT INTO STUDY (id,name,description,organization) VALUES ('ABC123','DS-1','First Dummy Study','Sample Data Research Institute');" >> $POSTGRES_INIT_SQL
