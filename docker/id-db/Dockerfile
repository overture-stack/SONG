FROM postgres:9.6
MAINTAINER ICGC <dcc-support@icgc.org>

ADD init.sql /docker-entrypoint-initdb.d/init.sql

ENV POSTGRES_DB=dcc_identifier
ENV POSTGRES_USER=postgres
ENV POSTGRES_PASSWORD=password
