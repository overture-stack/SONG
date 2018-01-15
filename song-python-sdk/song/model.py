import logging

from song import utils
from song.utils import SongClientException

logging.basicConfig(level=logging.INFO)
log = logging.getLogger("song.model")


###############################
# Utility Functions for Models
###############################

def get_optional_field(dic, field):
    return dic[field] if dic.has_key(field) else None


def get_required_field(dic, field):
    if dic.has_key(field):
        return dic[field]
    else:
        raise SongClientException("study.id.dne", "The field '{}' does not exist in {}".format(field, str(dic)))

################################
#  Models
################################


class Study(object):

    def __init__(self, studyId=None, name=None, organization=None, description=None):
        self.studyId = studyId
        self.name = name
        self.organization = organization
        self.description = description

    def __str__(self):
        return utils.to_json_string(self)

    @classmethod
    def create_from_raw(cls, study_obj):
        study_dict = study_obj.__dict__
        study_id = get_required_field(study_dict, 'studyId')
        study_name = get_optional_field(study_dict, 'name')
        study_description = get_optional_field(study_dict, 'description')
        study_organization = get_optional_field(study_dict, 'organization')
        return Study(study_id, study_name, study_organization, study_description)


class SongError(Exception):

    FIELDS = ['stackTrace',
              'errorId',
              'httpStatusName',
              'httpStatusCode',
              'message',
              'requestUrl',
              'debugMessage',
              'timestamp']

    def __init__(self, data, response, debug=False):
        self.stackTrace = data[SongError.FIELDS[0]]
        self.errorId = data[SongError.FIELDS[1]]
        self.httpStatusName = data[SongError.FIELDS[2]]
        self.httpStatusCode = data[SongError.FIELDS[3]]
        self.message = data[SongError.FIELDS[4]]
        self.requestUrl = data[SongError.FIELDS[5]]
        self.debugMessage = data[SongError.FIELDS[6]]
        self.timestamp = data[SongError.FIELDS[7]]
        self.response = response
        self.__debug = debug

    @classmethod
    def is_song_error(cls, data):
        for field in SongError.FIELDS:
            if not data.has_key(field):
                return False
        return True

    def __str__(self):
        if self.__debug:
            return utils.to_pretty_json_string(self.response.content)
        else:
            return "[SONG_SERVER_ERROR] {} @ {}: {}".format(self.errorId, self.timestamp, self.message)


class ApiConfig(object):

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


class ManifestEntry(object):

    def __init__(self, fileId, fileName, md5sum):
        self.fileId = fileId
        self.fileName = fileName
        self.md5sum = md5sum

    @classmethod
    def create_manifest_entry(cls, data):
        d = data.__dict__
        fileId = get_required_field(d, 'objectId')
        fileName = get_required_field(d, 'fileName')
        md5sum = get_required_field(d, 'fileMd5sum')
        return ManifestEntry(fileId, fileName, md5sum)

    def __str__(self):
        return "{}\t{}\t{}".format(self.fileId, self.fileName, self.md5sum)


class Manifest(object):

    def __init__(self, analysis_id, manifest_entries=[]):
        self.__analysis_id = analysis_id
        self.__entries = manifest_entries

    def add_entry(self, manifest_entry):
        self.__entries.append(manifest_entry)

    def write(self, output_file_path, overwrite=False):
        utils.write_object(self, output_file_path, overwrite=overwrite)

    def __str__(self):
        return "{}\t\t\n".format(self.__analysis_id) + \
               "\n".join(map(lambda x: str(x), self.__entries))