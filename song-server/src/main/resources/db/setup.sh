#!/bin/sh
HERE=`dirname $0`
cd $HERE
psql test_db < ./create_tables.sql
psql test_db < ./add_test_data.sql
