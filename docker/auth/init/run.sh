
INIT_DB=$AUTH_INIT/db.sqlite3
DATA_DB=$AUTH_DATA/db.sqlite3
SIMPLE_AUTH_DB=$SIMPLE_AUTH_ROOT/db.sqlite3


echo "INIT_DB=$INIT_DB"
echo "DATA_DB=$DATA_DB"
echo "SIMPLE_AUTH_DB=$SIMPLE_AUTH_DB"
echo "SERVER_PORT=$SERVER_PORT"

## Initially, copy the init db if nothing already exists in the auth_data volume
if [ ! -f $DATA_DB ]; then
    cp -f $INIT_DB $DATA_DB
fi

## Link the db.sqlite3 file to the one stored in the volume
rm -f $SIMPLE_AUTH_DB
ln -s $DATA_DB $SIMPLE_AUTH_DB

## Run the server
python $SIMPLE_AUTH_ROOT/manage.py runserver  0.0.0.0:$SERVER_PORT ; FOR_100_YEARS=$((100*365*24*60*60));while true;do sleep $FOR_100_YEARS;done

