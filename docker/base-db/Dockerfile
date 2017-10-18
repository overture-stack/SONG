FROM postgres:9.6
MAINTAINER ICGC <dcc-support@icgc.org>

ENV DCC_HOME /opt/dcc
ENV DCC_DATA $DCC_HOME/data
ENV DCC_CONFIG $DCC_HOME/config
RUN apt update && apt install -y wget unzip vim
RUN useradd -m dcc &&  mkdir -p $DCC_HOME
WORKDIR $DCC_HOME

