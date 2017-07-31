#!/bin/sh
HERE=`dirname $0`
cd $HERE

DB=$1
psql postgres -c "DROP DATABASE IF EXISTS $DB;"
createdb $DB
