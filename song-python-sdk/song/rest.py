
import logging

import json
import requests

from song.model import SongError
import song.utils as utils


logging.basicConfig(level=logging.INFO)
log = logging.getLogger("song.rest")



def intercept_response(orig_function, debug=False, convert_to_json=False, convert_to_bean=False):

    def new_function(*args, **kwargs):
        response = orig_function(*args, **kwargs)
        if response.ok:
            if convert_to_bean:
                return utils.to_bean(response.json())
            elif convert_to_json:
                return response.json()
            else:
                return response
        else:
            json_data = dict(json.loads(response.content))
            if SongError.is_song_error(json_data):
                song_error = SongError(json_data, response, debug=debug)
                log.error(song_error)
                raise song_error
            else:
                message = "Not a song error. Response Code: {}, Response Message: {}".format(response.status_code, response.content)
                log.error(message)
                raise Exception(message)

    return new_function


class Rest(object):

    def __init__(self, access_token=None, debug=False):
        self.__header_generator = HeaderGenerator(access_token)
        self.debug = debug

    def _intercept(self, method):
        return intercept_response(method,
                                  debug=self.debug,
                                  convert_to_json=False,
                                  convert_to_bean=False)

    def get(self, url):
        return self._intercept(requests.get)\
            (url, headers=self.__header_generator.get_json_header())

    def get_with_params(self, url, **kwargs):
        param_string = '&'.join(self.__convert_params(**kwargs))
        return self.get(url+'?'+param_string)

    def post(self, url, json=None):
        return self._intercept(requests.post)\
            (url, json=json, headers=self.__header_generator.get_json_header())

    def put(self, url):
        return self._intercept(requests.put)\
            (url, headers=self.__header_generator.get_json_header())

    def __convert_params(self, **kwargs):
        param_list = []
        for k,v in kwargs.items():
            param_list.append(k+'='+v)
        return param_list


class JsonRest(Rest):

    def __init__(self, *args, **kwargs):
        Rest.__init__(self,*args, **kwargs)

    def _intercept(self, method):
        return intercept_response(method,
                                  debug=self.debug,
                                  convert_to_json=True,
                                  convert_to_bean=False)


class BeanRest(Rest):

    def __init__(self, *args, **kwargs):
        Rest.__init__(self,*args, **kwargs)

    def _intercept(self, method):
        return intercept_response(method,
                                  debug=self.debug,
                                  convert_to_bean=True)

class HeaderGenerator(object):

    def __init__(self, access_token=None):
        self.__access_token = access_token

    def get_json_header(self):
        headers = self.get_plain_header()
        headers['Content-Type'] = 'application/json'
        return headers

    def get_plain_header(self):
        headers = {}
        if self.__is_auth():
            headers = {'Authorization': 'Bearer '+self.__access_token}
        return headers

    def __is_auth(self):
        return self.__access_token is not None
