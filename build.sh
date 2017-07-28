#!/bin/sh
HERE=`dirname $0`
cd $HERE
song-server/src/main/resources/db/setup.sh
mvn clean package
