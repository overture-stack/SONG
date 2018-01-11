from song.rest import Rest
from song.endpoints import Endpoints
import json
import os


class Config(object):

    def __init__(self, server_url, study_id, access_token, debug=False):
        self.__server_url = server_url
        self.__study_id = study_id
        self.__access_token = access_token
        self.__debug = debug

    @property
    def server_url(self):
        return self.__server_url

    @property
    def study_id(self):
        return self.__study_id or 'ABC123'

    @property
    def access_token(self):
        return self.__access_token

    @property
    def debug(self):
        return self.__debug


class Client(object):

    def __init__(self, config):
        self.__config = config
        self.__rest = Rest(config.access_token)
        self.__endpoints = Endpoints(config.server_url)

    @property
    def config(self):
        return self.__config

    def upload_file(self, file_path, is_async_validation=False):
        if not os.path.exists(file_path):
            raise Exception("The file {} does not exist".format(file_path))

        with open(file_path, 'r') as json_data:
            return self.upload_json(json.load(json_data), is_async_validation=is_async_validation)

    def upload_json(self, json_payload, is_async_validation=False):
        response = self.__rest.post(
            self.__endpoints.upload(
                self.config.study_id,
                is_async_validation=is_async_validation), json_data=json_payload)
        return response.json()

    def status(self, upload_id):
        endpoint = self.__endpoints.status(self.config.study_id, upload_id)
        return self.__rest.get(endpoint).json()
