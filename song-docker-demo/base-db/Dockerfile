FROM postgres:9.6
MAINTAINER OVERTURE <contact@overture.bio>

ENV DCC_HOME /opt/dcc
ENV DCC_DATA $DCC_HOME/data
ENV DCC_CONFIG $DCC_HOME/config
RUN apt update && \
        apt install -y wget unzip vim python3 nmap telnet && \
        useradd -m dcc && \
        echo "alias ls='ls --color'" >> ~/.bashrc && \
        echo "alias ll='ls -l'" >> ~/.bashrc && \
        echo "alias lr='ll -rt'" >> ~/.bashrc && \
        echo "alias la='ll -a'" >> ~/.bashrc && \
        mkdir -p $DCC_HOME
WORKDIR $DCC_HOME

