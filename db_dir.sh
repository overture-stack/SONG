#!/bin/sh
echo "select setting from pg_settings where name='data_directory';" | psql test_db | tail -n 3 | head -n 1
