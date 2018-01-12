import json
import os
import logging

import song.utils as utils
from song.rest import Rest


logging.basicConfig(level=logging.INFO)
log = logging.getLogger("song.client")

class Api(object):

    def __init__(self, config):
        self.__config = config
        self.__rest = Rest(config.access_token, debug=config.debug)
        self.__endpoints = Endpoints(config.server_url)

    @property
    def config(self):
        return self.__config

    def upload(self, json_payload, is_async_validation=False):
        response = self.__rest.post(
            self.__endpoints.upload(
                self.config.study_id,
                is_async_validation=is_async_validation), data=json_payload)
        return response.json()

    def status(self, upload_id):
        endpoint = self.__endpoints.status(self.config.study_id, upload_id)
        return self.__rest.get(endpoint).json()

    def save(self, upload_id, ignore_analysis_id_collisions=False):
        endpoint = self.__endpoints.save_by_id(
            self.config.study_id, upload_id, ignore_analysis_id_collisions=ignore_analysis_id_collisions)
        return self.__rest.post(endpoint).json()

    def get_analysis_files(self, analysis_id):
        endpoint = self.__endpoints.get_analysis_files(self.config.study_id, analysis_id)
        return self.__rest.get(endpoint).json()

    def get_analysis(self, analysis_id):
        endpoint = self.__endpoints.get_analysis(self.config.study_id, analysis_id)
        return self.__rest.get(endpoint).json()

    def is_alive(self):
        endpoint = self.__endpoints.is_alive()
        response = self.__rest.get(endpoint)
        return response.content == 'true'

    def publish(self, analysis_id):
        endpoint = self.__endpoints.publish(self.config.study_id, analysis_id)
        return self.__rest.put(endpoint).json()

    def suppress(self, analysis_id):
        endpoint = self.__endpoints.suppress(self.config.study_id, analysis_id)
        return self.__rest.put(endpoint).json()

    def id_search(self, sample_id=None, specimen_id=None, donor_id=None, file_id=None):
        endpoint = self.__endpoints.id_search(
            self.config.study_id, sample_id=sample_id, specimen_id=specimen_id, donor_id=donor_id, file_id=file_id)
        return self.__rest.get(endpoint).json()

    def info_search(self, is_include_info, **search_terms):
        endpoint = self.__endpoints.info_search(self.config.study_id, is_include_info, **search_terms)
        return self.__rest.get(endpoint).json()

    def get_study(self, study_id):
        endpoint = self.__endpoints.get_study(study_id)
        return self.__rest.get(endpoint).json()

    def get_entire_study(self, study_id):
        endpoint = self.__endpoints.get_entire_study(study_id)
        return self.__rest.get(endpoint).json()

    def save_study(self, study):
        endpoint = self.__endpoints.save_study(study.studyId)
        return self.__rest.post(endpoint, utils.to_json_string(study)).json()


class StudyClient(object):

    def __init__(self, api):
        if not isinstance(api, Api):
            raise Exception("The argument must be an instance of Api")
        self.__api = api

    def create_study(self, study):
        return self.__api.save_study(study) == '1'

    def has_study(self, study_id):
        val = self.__api.get_study(study_id)
        return val[0] is not None


class UploadClient(object):

    def __init__(self, api):
        if not isinstance(api, Api):
            raise Exception("The argument must be an instance of Api")
        self.__api = api

    def upload_file(self, file_path, is_async_validation=False):
        if not os.path.exists(file_path):
            raise Exception("The file {} does not exist".format(file_path))

        with open(file_path, 'r') as file_content:
            json_data = json.loads(file_content)  # just to validate the json
            return self.__api.upload(json_data, is_async_validation=is_async_validation)


class Manifest(object):

    def __init__(self, api):
        if not isinstance(api, Api):
            raise Exception("The argument must be an instance of Api")
        self.__api = api

    def create_manifest(self, analysis_id, output_file_path):
        utils.setup_output_file_path(output_file_path)
        self.__api.get_analysis_files(analysis_id)


class Endpoints(object):

    def __init__(self, server_url):
        self.__server_url = server_url

    def upload(self, study_id, is_async_validation=False):
        if is_async_validation:
            return "{}/upload/{}/async".format(self.__server_url, study_id)
        else:
            return "{}/upload/{}".format(self.__server_url, study_id)

    def save_by_id(self, study_id, upload_id, ignore_analysis_id_collisions):
        return "{}/upload/{}/save/{}?ignoreAnalysisIdCollisions={}".format(
            self.__server_url, study_id, upload_id, ignore_analysis_id_collisions)

    def status(self, study_id, upload_id):
        return "{}/upload/{}/status/{}".format(self.__server_url, study_id, upload_id)

    def get_analysis_files(self, study_id, analysis_id):
        return "{}/studies/{}/analysis/{}/files".format(self.__server_url, study_id, analysis_id)

    def get_analysis(self, study_id, analysis_id):
        return "{}/studies/{}/analysis/{}".format(self.__server_url, study_id, analysis_id)

    def is_alive(self):
        return "{}/isAlive".format(self.__server_url)

    def publish(self, study_id, analysis_id):
        return "{}/studies/{}/analysis/publish/{}".format(self.__server_url, study_id, analysis_id)

    def suppress(self, study_id, analysis_id):
        return "{}/studies/{}/analysis/suppress/{}".format(self.__server_url, study_id, analysis_id)

    def id_search(self, study_id, sample_id=None, specimen_id=None, donor_id=None, file_id=None):
        return self.__id_search(study_id, sampleId=sample_id, specimenId=specimen_id, donorId=donor_id, fileId=file_id)

    def __id_search(self, study_id, **kwargs):
        param_list = []
        for key, value in kwargs.items():
            if value is not None:
                param_list.append(key+'='+value)
        params = '&'.join(param_list)
        return "{}/studies/{}/analysis/search/id?{}".format(self.__server_url, study_id, params)

    def info_search(self, study_id, is_include_info, **search_terms):
        params = '&'.join(utils.convert_to_url_param_list(**search_terms))
        return "{}/studies/{}/analysis/search/info?includeInfo={}&{}".format(
            self.__server_url,study_id, is_include_info, params)

    def get_entire_study(self, study_id):
        return "{}/studies/{}/all".format(self.__server_url, study_id)

    def get_study(self, study_id):
        return "{}/studies/{}".format(self.__server_url, study_id)

    def save_study(self, study_id):
        return "{}/studies/{}/".format(self.__server_url, study_id)