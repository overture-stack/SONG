FROM ubuntu:latest
MAINTAINER ICGC <dcc-support@icgc.org>

ENV AUTH_HOME /opt/dcc
RUN useradd -m dcc &&  mkdir -p $AUTH_HOME

WORKDIR $AUTH_HOME

RUN apt update \
	&& apt install -y git python python-pip \
	&& pip install --upgrade pip \
	&& git clone https://github.com/andricDu/SimpleAuth $AUTH_HOME/SimpleAuth \
	&& pip install -r $AUTH_HOME/SimpleAuth/requirements.txt
RUN rm -f $AUTH_HOME/SimpleAuth/db.sqlite3 && ln -s $AUTH_HOME/simple-auth-data/db.sqlite3 $AUTH_HOME/SimpleAuth/db.sqlite3
CMD python $AUTH_HOME/SimpleAuth/manage.py runserver  0.0.0.0:8000 ; FOR_100_YEARS=$((100*365*24*60*60));while true;do sleep $FOR_100_YEARS;done


