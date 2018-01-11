import song.utils as utils


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
