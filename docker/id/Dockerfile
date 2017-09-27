From java
MAINTAINER ICGC <dcc-support@icgc.org>

ADD https://artifacts.oicr.on.ca/artifactory/dcc-release/org/icgc/dcc/dcc-id-server/[RELEASE]/dcc-id-server-[RELEASE]-dist.tar.gz /dcc-id.tar.gz

RUN  mv dcc-id.tar.gz/dcc-id-server-* /dcc-id-server
RUN rm -rf /dcc-id.tar.gz
 
ADD init.sh /dcc-id-server/init.sh

ENV POSTGRES_HOST=id-db
ENV POSTGRES_PORT=5432
ENV POSTGRES_DB=dcc_identifier
ENV POSTGRES_USER=postgres
ENV POSTGRES_PASSWORD=password

ENV AUTH_SERVER_URL=
ENV AUTH_SERVER_CLIENTID=
ENV AUTH_SERVER_CLIENTSECRET=

EXPOSE 8443 8444

CMD /dcc-id-server/init.sh;FOR_100_YEARS=$((100*365*24*60*60));while true;do sleep $FOR_100_YEARS;done

