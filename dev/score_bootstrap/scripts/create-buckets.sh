#!/bin/sh
echo '$DCC_DATA='$DCC_DATA
echo '$BUCKET_NAME_OBJECT='$BUCKET_NAME_OBJECT
echo '$BUCKET_NAME_STATE='$BUCKET_NAME_STATE
echo '$COLLABORATORY_DATA_DIRECTORY='$COLLABORATORY_DATA_DIRECTORY
echo '$OBJECT_SENTINEL='$OBJECT_SENTINEL

function setup_bucket {
	bucket=$1
	data_dir=${COLLABORATORY_DATA_DIRECTORY}
	sentinel_object=${OBJECT_SENTINEL}
	dir=$DCC_DATA/$bucket/$data_dir
	if [ ! -d $dir ]; then
		mkdir -p $dir
	fi

	sentinel="$dir/$sentinel_object"
	if [ ! -f $sentinel ]; then
		echo "Touching \"$sentinel\""
		touch $sentinel
	fi
}

setup_bucket $BUCKET_NAME_OBJECT
setup_bucket $BUCKET_NAME_STATE
