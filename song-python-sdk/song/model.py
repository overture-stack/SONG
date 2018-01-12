from song import utils
import logging

logging.basicConfig(level=logging.INFO)
log = logging.getLogger("song.model")


class Study(object):

    def __init__(self, studyId=None, name=None, organization=None, description=None):
        self.studyId = studyId
        self.name = name
        self.organization = organization
        self.description = description


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

        # self.stackTrace = data['stackTrace']
        # self.errorId = data['errorId']
        # self.httpStatusName = data['httpStatusName']
        # self.httpStatusCode = data['httpStatusCode']
        # self.message = data['message']
        # self.requestUrl = data['requestUrl']
        # self.debugMessage = data['debugMessage']
        # self.timestamp = data['timestamp']
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