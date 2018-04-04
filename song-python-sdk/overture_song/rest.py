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

import logging

import json
import requests

from overture_song.model import SongError
from overture_song.utils import to_generic_object

logging.basicConfig(level=logging.INFO)
log = logging.getLogger("song.rest")


def intercept_response(orig_function, debug=False, convert_to_json=False, convert_to_generic_object=False):
    def new_function(*args, **kwargs):
        response = orig_function(*args, **kwargs)
        if response.ok:
            if not convert_to_generic_object and not convert_to_json:
                return response
            else:
                try:
                    json_response = response.json()
                    if convert_to_generic_object:
                        return to_generic_object('RestResponse', json_response)
                    elif convert_to_json:
                        return json_response
                except:
                    return str(response.text)
        else:
            json_data = dict(json.loads(response.content))
            if SongError.is_song_error(json_data):
                song_error = SongError.create_song_error(json_data)
                if debug:
                    log.error(song_error.to_json())
                raise song_error
            else:
                message = "[UNKNOWN_REST_ERROR] Not a song error. Response Code: {}, Response Message: {}".format(response.status_code,
                                                                                             response.content)
                if debug:
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
                                  convert_to_generic_object=False)

    def get(self, url):
        return self._intercept(requests.get)(url, headers=self.__header_generator.get_json_header())

    def get_with_params(self, url, **kwargs):
        param_string = '&'.join(Rest.__convert_params(**kwargs))
        return self.get(url+'?'+param_string)

    def post(self, url, dict_data=None):
        return self._intercept(requests.post)(url, json=dict_data, headers=self.__header_generator.get_json_header())

    def put(self, url):
        return self._intercept(requests.put)(url, headers=self.__header_generator.get_json_header())

    @classmethod
    def __convert_params(cls, **kwargs):
        param_list = []
        for k, v in kwargs.items():
            param_list.append(k+'='+v)
        return param_list


class ObjectRest(Rest):
    def __init__(self, *args, **kwargs):
        Rest.__init__(self, *args, **kwargs)

    def _intercept(self, method):
        return intercept_response(method,
                                  debug=self.debug,
                                  convert_to_generic_object=True)


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
