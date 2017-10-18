FROM song_base-db:latest
MAINTAINER ICGC <dcc-support@icgc.org>

ENV POSTGRES_HOST=$DB_HOST
ENV POSTGRES_DB=$DB_NAME
ENV POSTGRES_USER=$DB_USER
ENV POSTGRES_PASSWORD=$DB_PASSWORD
ENV PGDATA /var/lib/postgresql/data/pgdata
ENV POSTGRES_INIT_SQL /docker-entrypoint-initdb.d/init.sql

ENV DOWNLOAD_URL https://artifacts.oicr.on.ca/artifactory/dcc-release/org/icgc/dcc/song-server/[RELEASE]/song-server-[RELEASE].jar 
ENV JAR  $DCC_HOME/song-server.jar
RUN wget $DOWNLOAD_URL -O $JAR && \
    unzip -p song-server.jar schema.sql > $POSTGRES_INIT_SQL
RUN echo "INSERT INTO STUDY (id,name,description,organization) VALUES ('ABC123','DS-1','First Dummy Study','Sample Data Research Institute');" >> $POSTGRES_INIT_SQL
