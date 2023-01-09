FROM overture/score-server:1.6.1

ENV DCC_HOME /opt/dcc
ENV DCC_SCRIPTS $DCC_HOME/scripts

ADD scripts $DCC_SCRIPTS

COPY .aws /root/.aws

CMD mkdir -p  $SCORE_HOME $SCORE_LOGS && \
    /bin/sh -c $DCC_SCRIPTS/create-buckets.sh && \
    java -Dlog.path=$SCORE_LOGS -jar $JAR_FILE