FROM song_base-ubuntu:latest
MAINTAINER OVERTURE <contact@overture.bio>

ENV AUTH_INIT $DCC_HOME/init
ENV AUTH_DATA $DCC_HOME/auth_data
ENV SIMPLE_AUTH_ROOT $DCC_HOME/SimpleAuth

ADD init $AUTH_INIT

RUN git clone https://github.com/andricDu/SimpleAuth $SIMPLE_AUTH_ROOT && \
	pip3 install -r $SIMPLE_AUTH_ROOT/requirements.txt

CMD $AUTH_INIT/run.sh $AUTH_PORT


