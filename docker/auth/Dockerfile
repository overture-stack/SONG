FROM song_base_ubuntu:latest
MAINTAINER ICGC <dcc-support@icgc.org>

ENV AUTH_INIT $DCC_HOME/init
ENV AUTH_DATA $DCC_HOME/auth_data
ENV SIMPLE_AUTH_ROOT $DCC_HOME/SimpleAuth

ADD init $AUTH_INIT

RUN pip install --upgrade pip  && \
	git clone https://github.com/andricDu/SimpleAuth $SIMPLE_AUTH_ROOT && \
	pip install -r $SIMPLE_AUTH_ROOT/requirements.txt

CMD $AUTH_INIT/run.sh


