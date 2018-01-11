
import requests


class Rest(object):

    def __init__(self, access_token=None):
        self.__header_generator = HeaderGenerator(access_token)

    def get(self, url):
        return requests.get(url, headers=self.__header_generator.get_json_header())

    def get_with_params(self, url, **kwargs):
        param_string = '&'.join(self.__convert_params(**kwargs))
        return self.get(url+'?'+param_string)

    def post(self, url, json_data):
        return requests.post(url, json=json_data, headers=self.__header_generator.get_json_header())

    def post(self, url, **kwargs):
        return requests.post(url, json=kwargs, headers=self.__header_generator.get_json_header())

    def put(self, url):
        return requests.put(url, headers=self.__header_generator.get_json_header())

    def __convert_params(self, **kwargs):
        param_list = []
        for k,v in kwargs.items():
            param_list.append(k+'='+v)
        return param_list


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
