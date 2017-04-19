psql << EOF
drop database sodalite;
create database sodalite;
EOF
psql -d sodalite < postgres_schema.sql
psql -d sodalite < insert_test_data.sql
