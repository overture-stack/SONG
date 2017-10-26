
import boto3

class S3Client:
    def __init__(self, host, port, access_key, secret_key, data_bucket, state_bucket, sentinal_key):

        self.__host = host
        self.__port = port
        self.__access_key=access_key
        self.__secret_key=secret_key
        self.__data_bucket=data_bucket
        self.__state_bucket=state_bucket
        self.__sentinal_key=sentinal_key
        self.__sentinal_temp_path = 'empty_file.txt'

        # state
        self.__client = self.__connect()
        self.__buckets = self.__client.list_buckets()

    def __get_url(self):
        return 'http://'+host+':'+port

    def __connect(self):
        return boto3.client('s3',
                aws_access_key=access_key,
                aws_secret_access_key=secret_key,
                use_ssl=False,
                endpoint_url=self.__get_url())

    def setup(self):
        self.setup_data_bucket()
        self.setup_state_bucket()

    def setup_data_bucket(self):
        self.__setup_bucket(self.__data_bucket)

    def setup_state_bucket(self):
        self.__setup_bucket(self.__state_bucket)


    def __get_local_sentinal_pathname(self):
        if not os.path.exists(self.__sentinal_temp_path):
            open(self.__sentinal_temp_path).close()
        return self.__sentinal_temp_path

    def __setup_bucket(self, name):
        if not self.__bucket_exists(name):
            self.__client.create_bucket(Bucket=name)
        self.__setup_sentinal(name)

    def __setup_sentinal(self, bucket_name):
        if not self.__sentinal_exists(bucket_name):
            local_sentinal_path = self.__get_local_sentinal_pathname()
            self.__client.upload_file(local_sentinal_path, bucket_name, self.__sentinal_key)

    def __bucket_exists(self, name):
        return name in self.__buckets

    def __sentinal_exists(self, bucket_name):
        bucket = self.__client.Bucket(bucket_name)
        objs = list(bucket.objects.filter(Prefix=self.__sentinal_key))
        return len(objs)>0 and objs[0].key == self.__sentinal_key

def get_env(env_name):
    value = os.environ[env_name]
    if value is None:
        raise Exception('The env variable '+env_name+' does not exist')
    else:
        return value


def main():
    host = get_env('MINIO_HOST')
    port = get_env('MINIO_PORT')
    access_key = get_env('MINIO_ACCESS_KEY')
    secret_key = get_env('MINIO_SECRET_KEY')
    data_bucket = get_env('STORAGE_SERVER_DATA_BUCKET')
    state_bucket = get_env('STORAGE_SERVER_STATE_BUCKET')
    sentinal_key = get_env('STORAGE_SERVER_SENTINAL_KEY')
    s3Client = S3Client( host,
            port,
            access_key,
            secret_key,
            data_bucket,
            state_bucket,
            sentinal_key)
    s3Client.setup()


if __name__ == "__main__":
    main()
