# Copyright (c) 2018 The Ontario Institute for Cancer Research. All rights
# reserved.
#
# This program and the accompanying materials are made available under the
# terms of the GNU Public License v3.0. You should have received a copy of
# the GNU General Public License along with
# this program. If not, see <http://www.gnu.org/licenses/>.
#
# THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
# AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
# IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
# ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
# LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
# CONSEQUENTIAL DAMAGES (INCLUDING,BUT NOT LIMITED TO, PROCUREMENT OF
# SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,OR PROFITS; OR BUSINESS
# INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER
# IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
# ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
# POSSIBILITY OF SUCH DAMAGE.
#

import json
import logging
import os

import song.utils as utils
from song.model import Study, ManifestEntry, Manifest
from song.utils import SongClientException
from song.rest import BeanRest

logging.basicConfig(level=logging.INFO)
log = logging.getLogger("song.client")


def beanify(original_function):
    def new_function(*args, **kwargs):
        response = original_function(*args, **kwargs)
        return utils.to_bean(response)
    return new_function


class Api(object):

    def __init__(self, config):
        self.__config = config
        self.__rest = BeanRest(access_token=config.access_token, debug=config.debug)
        self.__endpoints = Endpoints(config.server_url)

    @property
    def config(self):
        return self.__config

    def upload(self, json_payload, is_async_validation=False):
        return self.__rest.post(
            self.__endpoints.upload(
                self.config.study_id,
                is_async_validation=is_async_validation), json=json_payload )

    def status(self, upload_id):
        endpoint = self.__endpoints.status(self.config.study_id, upload_id)
        return self.__rest.get(endpoint)

    def save(self, upload_id, ignore_analysis_id_collisions=False):
        endpoint = self.__endpoints.save_by_id(
            self.config.study_id, upload_id, ignore_analysis_id_collisions=ignore_analysis_id_collisions)
        return self.__rest.post(endpoint)

    def get_analysis_files(self, analysis_id):
        endpoint = self.__endpoints.get_analysis_files(self.config.study_id, analysis_id)
        return self.__rest.get(endpoint)

    def get_analysis(self, analysis_id):
        endpoint = self.__endpoints.get_analysis(self.config.study_id, analysis_id)
        return self.__rest.get(endpoint)

    def is_alive(self):
        endpoint = self.__endpoints.is_alive()
        return self.__rest.get(endpoint)

    def publish(self, analysis_id):
        endpoint = self.__endpoints.publish(self.config.study_id, analysis_id)
        return self.__rest.put(endpoint)

    def suppress(self, analysis_id):
        endpoint = self.__endpoints.suppress(self.config.study_id, analysis_id)
        return self.__rest.put(endpoint)

    def id_search(self, sample_id=None, specimen_id=None, donor_id=None, file_id=None):
        endpoint = self.__endpoints.id_search(
            self.config.study_id, sample_id=sample_id, specimen_id=specimen_id, donor_id=donor_id, file_id=file_id)
        return self.__rest.get(endpoint)

    def info_search(self, is_include_info, **search_terms):
        endpoint = self.__endpoints.info_search(self.config.study_id, is_include_info, **search_terms)
        return self.__rest.get(endpoint)

    def get_study(self, study_id):
        endpoint = self.__endpoints.get_study(study_id)
        return self.__rest.get(endpoint)

    def get_entire_study(self, study_id):
        endpoint = self.__endpoints.get_entire_study(study_id)
        return self.__rest.get(endpoint)

    def get_all_studies(self):
        endpoint = self.__endpoints.get_all_studies()
        return self.__rest.get(endpoint)

    def save_study(self, study):
        endpoint = self.__endpoints.save_study(study.studyId)
        # study_json = json.loads(study)
        return self.__rest.post(endpoint, json=study.__dict__)


class StudyClient(object):

    def __init__(self, api):
        utils.check_type(api, Api)
        self.__api = api

    def create(self, study):
        return self.__api.save_study(study) == '1'

    def has(self, study_id):
        val = self.__api.get_study(study_id)
        return val[0] is not None

    def read(self, study_id, recursive=False):
        # [Issue #146]: disabled since the song-server has a bug in the /studies/{studyId}/all
        # endpoint (https://github.com/overture-stack/SONG/issues/146)
        #
        # if recursive:
        #     val = self.__api.get_entire_study(study_id)
        #     if val is None:
        #         raise SongClientException('study.client.entire', "The study_id '{}' does not exist".format(study_id))
        #     return val
        val = self.__api.get_study(study_id)
        utils.check_song_state(val[0] is not None, 'study.client.get',
                               "The study_id '{}' does not exist".format(study_id))
        return Study.create_from_raw(val[0])


class UploadClient(object):

    def __init__(self, api):
        if not isinstance(api, Api):
            raise SongClientException('upload.client', "The argument must be an instance of Api")
        self.__api = api

    def upload_file(self, file_path, is_async_validation=False):
        if not os.path.exists(file_path):
            raise SongClientException('upload.client', "The file {} does not exist".format(file_path))

        with open(file_path, 'r') as file_content:
            json_data = json.load(file_content)  # just to validate the json
            return self.__api.upload(json_data, is_async_validation=is_async_validation)

    def check_upload_status(self, upload_id):
        return self.__api.status(upload_id)

    def save(self, upload_id, ignore_analysis_id_collisions=False):
        return self.__api.save(upload_id, ignore_analysis_id_collisions=ignore_analysis_id_collisions)

    def publish(self, analysis_id):
        return self.__api.publish(analysis_id)


class ManifestClient(object):

    def __init__(self, api):
        if not isinstance(api, Api):
            raise SongClientException("manifest.service", "The argument must be an instance of Api")
        self.__api = api

    def create_manifest(self, analysis_id, output_file_path):
        manifest = Manifest(analysis_id)
        for file_bean in self.__api.get_analysis_files(analysis_id):
            manifest_entry = ManifestEntry.create_manifest_entry(file_bean)
            manifest.add_entry(manifest_entry)
        utils.write_object(manifest, output_file_path, overwrite=True)


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

    def get_all_studies(self):
        return "{}/studies/all".format(self.__server_url)

    def get_study(self, study_id):
        return "{}/studies/{}".format(self.__server_url, study_id)

    def save_study(self, study_id):
        return "{}/studies/{}/".format(self.__server_url, study_id)