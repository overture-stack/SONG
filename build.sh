#!/bin/sh
HERE=`dirname $0`
cd $HERE

PROG="psql"
DB="test_db"
echo " -- Checking if postgresql installed -- "
if ! type "$PROG" &> /dev/null; then
    echo "FAIL - Posgresql is NOT installed, since command \"$PROG\" DNE!!"
    exit 1
else
    echo "SUCCESS - Postgresql is installed"
    song-server/src/main/resources/db/setup.sh $DB
    mvn clean package
fi
