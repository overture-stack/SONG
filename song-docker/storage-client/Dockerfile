FROM overture/score:1.1.1
MAINTAINER OVERTURE <contact@overture.bio>

ENV DCC_HOME /opt/dcc
ENV DCC_CONFIG /opt/dcc/config
ENV DCC_DATA /opt/dcc/data
ADD config $DCC_CONFIG

ENV EXPAND_SCRIPT $DCC_HOME/tools/expand.py
ENV INPUT_FILE $DCC_CONFIG/application-aws.properties.template
ENV OUTPUT_FILE $DCC_DATA/conf/application-aws.properties

CMD cp -r /score/score-client/* $DCC_DATA && \
    mkdir -p $DCC_DATA/logs && \
    chmod -R 777 $DCC_DATA && \
    rm -f $DCC_DATA/conf/application.properties && \
    python3 $EXPAND_SCRIPT $INPUT_FILE $OUTPUT_FILE 
