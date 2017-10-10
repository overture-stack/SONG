FROM python:latest
MAINTAINER ICGC <dcc-support@icgc.org>

ENV SERVER_PORT 9090
ENV DCC_HOME /opt/dcc
RUN useradd -m dcc &&  mkdir -p $DCC_HOME
ENV AUTH_INIT $DCC_HOME/init
ENV AUTH_DATA $DCC_HOME/auth_data
ENV SIMPLE_AUTH_ROOT $DCC_HOME/SimpleAuth

ADD init $AUTH_INIT

WORKDIR $DCC_HOME

EXPOSE $SERVER_PORT

RUN apt update \
	&& apt install -y \
        git \
        sqlite3 \
        python-pip \
	&& pip install --upgrade pip \
	&& git clone https://github.com/andricDu/SimpleAuth $SIMPLE_AUTH_ROOT \
	&& pip install -r $SIMPLE_AUTH_ROOT/requirements.txt

CMD $AUTH_INIT/run.sh


