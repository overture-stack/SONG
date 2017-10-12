From java
MAINTAINER ICGC <dcc-support@icgc.org>


ENV DCC_HOME /opt/dcc
WORKDIR $DCC_HOME
ENV ID_HOME $DCC_HOME/dcc-id-server
ENV TAR_BALL_LOC $DCC_HOME/dcc-id-server.tar.gz

ENV POSTGRES_HOST id-db
ENV POSTGRES_PORT 5432
ENV POSTGRES_DB dcc_identifier
ENV POSTGRES_USER postgres
ENV POSTGRES_PASSWORD password

ENV AUTH_SERVER_URL http://auth:9090/check_token/
ENV AUTH_SERVER_CLIENTID 3kJhz9pNtC0pFHAxr2SPkUkGjXrkWWqGcnPC0vBP
ENV AUTH_SERVER_CLIENTSECRET v9mjRtuEVwpt7cgqnsq6mxtCa5FbUOpKLGh7WX8a1dWbBKfrM3iV3VYMtE60jr3W7GLWtNeYIaJ8EUxPkaInclWVXf64qKdR3IKwyfpDU7JhvWEwIYQYdwV1YAUZjB2e

EXPOSE 8443 8444

ADD init.sh $DCC_HOME/init.sh
ADD https://artifacts.oicr.on.ca/artifactory/dcc-release/org/icgc/dcc/dcc-id-server/[RELEASE]/dcc-id-server-[RELEASE]-dist.tar.gz ${TAR_BALL_LOC}
RUN tar zxvf ${TAR_BALL_LOC} && \
        mv $DCC_HOME/dcc-id-server-* $ID_HOME && \
        rm -rf ${TAR_BALL_LOC} $ID_HOME/logs && \
        ln -s $DCC_HOME/id_logs $ID_HOME/logs
 
CMD $DCC_HOME/init.sh;FOR_100_YEARS=$((100*365*24*60*60));while true;do sleep $FOR_100_YEARS;done

