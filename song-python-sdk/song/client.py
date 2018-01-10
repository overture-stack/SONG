

class Client(object):

    def __init__(self, serverUrl, studyId, accessToken, debug=False):
        self.__serverUrl = serverUrl
        self.__studyId = studyId
        self.__accessToken = accessToken
        self.__debug = debug

    def get_server_url(self):
        return self.__serverUrl

    def upload_file(self, file_path,  is_async=False):
        pass

    def upload_json(self, json_payload, is_async=False):
        pass
