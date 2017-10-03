FROM python:latest
MAINTAINER ICGC <dcc-support@icgc.org>

ENV DCC_HOME /opt/dcc
RUN useradd -m dcc &&  mkdir -p $DCC_HOME
ENV AUTH_DATA $DCC_HOME/auth_data
ENV SIMPLE_AUTH_ROOT $DCC_HOME/SimpleAuth

WORKDIR $DCC_HOME

ENV SERVER_PORT 9090

EXPOSE $SERVER_PORT

RUN apt update \
	&& apt install -y git python-pip \
	&& pip install --upgrade pip \
	&& git clone https://github.com/andricDu/SimpleAuth $SIMPLE_AUTH_ROOT \
	&& pip install -r $SIMPLE_AUTH_ROOT/requirements.txt
RUN rm -f $SIMPLE_AUTH_ROOT/db.sqlite3 && ln -s $AUTH_DATA/db.sqlite3 $SIMPLE_AUTH_ROOT/db.sqlite3
CMD python $SIMPLE_AUTH_ROOT/manage.py runserver  0.0.0.0:$SERVER_PORT ; FOR_100_YEARS=$((100*365*24*60*60));while true;do sleep $FOR_100_YEARS;done


