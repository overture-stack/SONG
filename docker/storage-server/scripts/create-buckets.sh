echo '$DCC_DATA='$DCC_DATA
echo '$STORAGE_SERVER_DATA_BUCKET='$STORAGE_SERVER_DATA_BUCKET
echo '$STORAGE_SERVER_STATE_BUCKET='$STORAGE_SERVER_STATE_BUCKET
echo '$STORAGE_SERVER_DATA_DIR='$STORAGE_SERVER_DATA_DIR
echo '$STORAGE_SERVER_OBJECT_SENTINEL='$STORAGE_SERVER_OBJECT_SENTINEL

function setup_bucket {
	bucket=$1
	data_dir=${STORAGE_SERVER_DATA_DIR}
	sentinel_object=${STORAGE_SERVER_OBJECT_SENTINEL}
	dir=$DCC_DATA/minio/$bucket/$data_dir
	if [ ! -d $dir ]; then
		mkdir -p $dir
	fi

	sentinel="$dir/$sentinel_object"
	if [ ! -f $sentinel ]; then
		echo "Touching \"$sentinel\""
		touch $sentinel
	fi
}

setup_bucket $STORAGE_SERVER_DATA_BUCKET
setup_bucket $STORAGE_SERVER_STATE_BUCKET
