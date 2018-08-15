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
    echo "select 1" | $PROG $DB &> /dev/null 

    if [ $? != 0 ]; then
       echo "Postgres is not running..."
       if [ -z "$PGDATA" ];then 
          initdb $DB 
          pg_ctl start -D $DB 
       else 
          pg_ctl start 
       fi
    fi
    echo "create database $DB;" | psql postgres 
    mvn clean package
fi
