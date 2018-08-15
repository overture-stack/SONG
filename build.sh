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
    status=$(echo "select 1" | $PROG $DB) 

    if [ $? != 0 ]; then
       echo "Postgres is not running..."
    fi

    if [ -z "$PGDATA" ];then 
       initdb $DB 
       pg_ctl start -D $DB 
    else 
       pg_ctl start 
    fi
    echo "create database $DB;" | psql postgres 
    mvn clean package
fi
