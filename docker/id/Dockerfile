From java
MAINTAINER ICGC <dcc-support@icgc.org>


ENV TAR_BALL_LOC /dcc-id.tar.gz
ADD https://artifacts.oicr.on.ca/artifactory/dcc-release/org/icgc/dcc/dcc-id-server/[RELEASE]/dcc-id-server-[RELEASE]-dist.tar.gz ${TAR_BALL_LOC}

# RUN if [ -f ${TAR_BALL}  ]; then \
#     tar zxvf ${TAR_BALL}; mv /dcc-id-server-* /dcc-id-server; \
#     else \
#         mv ${TAR_BALL}/dcc-id-server-* /dcc-id-server; \
#     fi

RUN tar zxvf ${TAR_BALL_LOC} && mv /dcc-id-server-* /dcc-id-server
RUN rm -rf ${TAR_BALL_LOC}
 
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

