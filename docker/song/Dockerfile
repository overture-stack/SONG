FROM java:8-jre
MAINTAINER ICGC <dcc-support@icgc.org>

#
# Configuration
#
ENV DCC_HOME /opt/dcc
RUN useradd -m dcc &&  mkdir -p $DCC_HOME
WORKDIR $DCC_HOME

EXPOSE 8080

ADD config $DCC_HOME/config 

ADD https://artifacts.oicr.on.ca/artifactory/dcc-release/org/icgc/dcc/song-server/[RELEASE]/song-server-[RELEASE].jar  $DCC_HOME/song-server.jar 

CMD java -jar song-server.jar --spring.config.location=$DCC_HOME/config/ --security.basic.enabled=false --spring.profiles.active="dev,test" --logging.level.root=WARN --logging-level.org.icgc-dcc=INFO; FOR_100_YEARS=$((100*365*24*60*60));while true;do sleep $FOR_100_YEARS;done 
