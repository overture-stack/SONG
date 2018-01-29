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

from dataclasses import dataclass

from overture_song.utils import default_value, to_pretty_json_string, write_object, \
    get_required_field

logging.basicConfig(level=logging.INFO)
log = logging.getLogger("song.model")


################################
#  Models
################################
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
            return to_pretty_json_string(self.response.content)
        else:
            return "[SONG_SERVER_ERROR] {} @ {}: {}".format(self.errorId, self.timestamp, self.message)


@dataclass(frozen=True)
class ApiConfig(object):
    server_url: str
    study_id: str
    access_token: str
    debug: bool = False


@dataclass(frozen=True)
class ManifestEntry(object):
    fileId: str
    fileName: str
    md5sum: str

    @classmethod
    def create_manifest_entry(cls, data):
        d = data.__dict__
        file_id = get_required_field(d, 'objectId')
        file_name = get_required_field(d, 'fileName')
        md5sum = get_required_field(d, 'fileMd5sum')
        return ManifestEntry(file_id, file_name, md5sum)

    def __str__(self):
        return "{}\t{}\t{}".format(self.fileId, self.fileName, self.md5sum)


class Manifest(object):
    def __init__(self, analysis_id, manifest_entries=None):
        self.analysis_id = analysis_id
        self.entries = default_value(manifest_entries, [])

    def add_entry(self, manifest_entry):
        self.entries.append(manifest_entry)

    def write(self, output_file_path, overwrite=False):
        write_object(self, output_file_path, overwrite=overwrite)

    def __str__(self):
        return "{}\t\t\n".format(self.analysis_id) + \
               "\n".join(map(lambda x: str(x), self.entries))
