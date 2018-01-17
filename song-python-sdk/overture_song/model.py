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

from overture_song import utils
from overture_song.utils import SongClientException

logging.basicConfig(level=logging.INFO)
log = logging.getLogger("song.model")


###############################
# Utility Functions for Models
###############################

def get_optional_field(dic, field):
    return dic[field] if field in dic else None


def get_required_field(dic, field):
    if field in dic:
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
            if field not in data:
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
        self.analysis_id = analysis_id
        self.entries = manifest_entries

    def add_entry(self, manifest_entry):
        self.entries.append(manifest_entry)

    def write(self, output_file_path, overwrite=False):
        utils.write_object(self, output_file_path, overwrite=overwrite)

    def __str__(self):
        return "{}\t\t\n".format(self.analysis_id) + \
               "\n".join(map(lambda x: str(x), self.entries))