import os
import re
import time
from functools import total_ordering

from enum import unique, Enum
import logging

from song import utils as utils
from song.client import UploadClient, Api
from song.utils import check_state, SongClientException
from song.model import ApiConfig
import json

logging.basicConfig(level=logging.INFO)
log = logging.getLogger("song.tools")


class EGAUploader(object):

    def __init__(self, api, payload_dir):

        # Dependencies
        self.api = api
        self.payload_dir = os.path.realpath(payload_dir)

        # State
        self.__upload_status_map = {}

        # Check
        utils.check_type(api, Api)
        utils.check_dir(self.payload_dir)

    def __calc_depth(self, root_path ):
        rel_root_path = os.path.realpath(root_path).replace(self.payload_dir, '')
        return len(rel_root_path.split(os.sep)) - 1

    def __use_root_dir(self, root_dir):
        is_study_level = self.__calc_depth(root_dir) == 1
        study_id_candidate = root_dir.split(os.sep)[-1]
        is_not_hidden_dir = re.search('^\.', study_id_candidate) is None
        return is_not_hidden_dir and is_study_level, study_id_candidate

    def upload(self):
        for root, dirs, files in os.walk(self.payload_dir):
            # print("root= {}    dirs = {}    files = {}      DEPTH={}".format(root, dirs, files, self.__calc_depth(root)))
            use_root_dir, study_id = self.__use_root_dir(root)
            if use_root_dir:
                print ("study_id = {}    files= {} ".format(study_id, files))

                for file in filter(lambda f: f.endswith('.json'), files):
                    filename = root+os.sep+file
                    self.api.upload_file(root+os.sep+file, is_async_validation=True)
                    file_upload_obj = FileUploadClient(self.api, filename,
                                                       is_async_validation=True, ignore_analysis_id_collisions=False)
                    try:
                        file_upload_obj.upload()
                        file_upload_obj.update_status()
                        file_upload_obj.save()
                        file_upload_obj.publish()
                    except Exception as e:
                        log.error("ERROR --> {}".format(e.message))


@unique
class FileUploadState(Enum):
    UNKNOWN = -2
    SAVE_ERROR =
    VALIDATION_ERROR = -1
    NOT_UPLOADED = 0
    SUBMITTED = 1
    VALIDATED = 2
    SAVED = 3
    PUBLISHED = 4

    def __equals(self, other):
        return self.value == other.value

    def __lt__(self, other):
        utils.check_type(other, self)
        return self.value < other.value

    def __gt__(self, other):
        return other.__lt__(self)

    def __eq__(self, other):
        utils.check_type(other, self)
        return self.__equals(other)

    def __le__(self, other):
        return self.__lt__(other) or self.__equals(other)

    def __ge__(self, other):
        return other.__le__(self)


class FileUploadClient(object):

    def __init__(self, api, filename, is_async_validation=False, ignore_analysis_id_collisions=False):

        # Dependencies
        self.__api = api
        self.filename = filename

        # Config
        self.is_async_validation = is_async_validation
        self.ignore_analysis_id_collisions = ignore_analysis_id_collisions
        self.retry_period_seconds = 0.1

        # State
        self.upload_state = FileUploadState.NOT_UPLOADED
        self.upload_status = None
        self.upload_id = None
        self.upload_errors = None
        self.analysis_id = None

        # Check
        utils.check_type(api, Api)
        utils.check_file(filename)

    def upload(self):
        if self.upload_state > FileUploadState.NOT_UPLOADED:
            log.warn("The file '{}' has already been uploaded".format(self.filename))
        else:
            with open(self.filename, 'r') as file_content:
                json_data = json.load(file_content)  # just to validate the json
            upload_response = self.__api.upload(json_data, is_async_validation=self.is_async_validation)
            utils.check_song_state(upload_response.status == 'ok', 'file.upload.fail',
                                   "The upload for file '{}' was unsuccessful", self.filename)
            self.upload_state = FileUploadState.SUBMITTED
            self.upload_id = upload_response.uploadId

    def update_status(self):
        if self.upload_state == FileUploadState.VALIDATION_ERROR:
            log.error("Validation error for file '{}' with upload_id '{}': {}".format(self.filename,
                                                                                      self.upload_id,
                                                                                      self.upload_errors))
        elif self.upload_state == FileUploadState.NOT_UPLOADED:
            log.warn("Status undefined for file '{}' as it was not uploaded".format(self.filename))
        elif self.upload_state == FileUploadState.SUBMITTED:
            # actually calculate the status
            status_response = self.__api.status(self.upload_id)
            while status_response.state == 'CREATED' or status_response.state == 'UPDATED':
                status_response = self.__api.status(self.upload_id)
                time.sleep(self.retry_period_seconds)
            if status_response.state == 'VALIDATED':
                self.upload_state = FileUploadState.VALIDATED
            elif status_response.state == 'SAVED':
                self.upload_state = FileUploadState.SAVED
            elif status_response.state == 'PUBLISHED':
                self.upload_state = FileUploadState.PUBLISHED
            elif status_response.state == 'VALIDATION_ERROR':
                self.upload_state = FileUploadState.VALIDATION_ERROR
                self.upload_errors = status_response.errors
            else:
                self.upload_state = FileUploadState.UNKNOWN
        elif FileUploadState.VALIDATED < self.upload_state < FileUploadState.PUBLISHED:
            log.info(
                "The file '{}' with upload_id '{}' has already been validated and has state '{}'".format(
                    self.filename, self.upload_id, self.upload_state.__class__.__name__ ))

    def save(self):
        utils.check_state(self.upload_state >= FileUploadState.VALIDATED,
                          "Need to VALIDATE upload_id '{}' for file '{}' before SAVING",
                          self.upload_id, self.filename)
        if self.upload_state >= FileUploadState.SAVED:
            log.warn("The file '{}' with upload_id '{}' was already saved with analysis_id '{}'".format(
                self.filename, self.upload_id, self.analysis_id))
        else:
            save_response = self.__api.save(self.upload_id,
                                            ignore_analysis_id_collisions=self.ignore_analysis_id_collisions)
            utils.check_state(save_response.status == 'ok',
                              "The save for upload_id '{}' for file '{}' was unsuccessfull: {}",
                              self.upload_id, self.filename, utils.to_json_string(save_response) )
            self.upload_state = FileUploadState.SAVED
            self.analysis_id = save_response.analysisId

    def publish(self):
        utils.check_state(self.upload_state >= FileUploadState.SAVED,
                          "Need to SAVE upload_id '{}' for file '{}' before PUBLISHING",
                          self.upload_id, self.filename)
        if self.upload_state >= FileUploadState.PUBLISHED:
            log.warn("The file '{}' with upload_id '{}' was already published with analysis_id '{}'".format(
                self.filename, self.upload_id, self.analysis_id))
        else:
            publish_response = self.__api.publish(self.analysis_id)
            utils.check_state(publish_response.status == 'ok',
                              "The publish for analysis_id '{}' for file '{}' and upload_id '{}' was unsuccessfull: {}",
                              self.analysis_id, self.filename, self.upload_id,  utils.to_json_string(publish_response))
            self.upload_state = FileUploadState.PUBLISHED
