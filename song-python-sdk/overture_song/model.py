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

from dataclasses import dataclass, fields, field, asdict
from overture_song.utils import check_state
from overture_song.entities import Entity

from overture_song.utils import default_value, to_pretty_json_string, write_object, \
    get_required_field

logging.basicConfig(level=logging.INFO)
log = logging.getLogger("song.model")


################################
#  Models
################################

@dataclass(frozen=True)
class SongError(Exception, Entity):
    errorId : str
    httpStatusName : str
    httpStatusCode : int
    message : str
    requestUrl : str
    debugMessage : str
    timestamp : str
    stackTrace : tuple = field(default_factory=tuple)

    @classmethod
    def create_song_error(cls, data):
        check_state(isinstance(data, dict), "input data must be of type dict")
        check_state(SongError.is_song_error(data),
                    "The input data fields \n'{}'\n are not the same as the data fields in SongError \n'{}'\n",
                    data.keys(), SongError.get_field_names())
        args = []
        for field in SongError.get_field_names():
            result = data[field]
            if isinstance(result, list):
                args.append(tuple(result))
            else:
                args.append(result)
        out = SongError(*args)
        return out

    @classmethod
    def is_song_error(cls, data):
        for field in SongError.get_field_names():
            if field not in data:
                return False
        return True

    @classmethod
    def get_field_names(cls):
        return list(fields(SongError).keys())


@dataclass(frozen=True)
class ApiConfig(object):
    """
    Configuration object for the SONG :py:class:`Api <overture_song.client.Api>`

    :param str server_url: URL of a running song-server
    :param str study_id: StudyId to interact with
    :param str access_token: access token used to authorize the song-server api
    :keyword bool debug: Enable debug mode

    """
    server_url: str
    study_id: str
    access_token: str
    debug: bool = False


@dataclass(frozen=True)
class ManifestEntry(object):
    """
    Represents a line in the manifest file pertaining to a file.
    The string representation of this object is the TSV of the 3 field values

    :param str fileId: ObjectId of the file
    :param str fileName: name of the file. Should not include directories
    :param str md5sum: MD5 checksum of the file

    """
    fileId: str
    fileName: str
    md5sum: str

    @classmethod
    def create_manifest_entry(cls, data):
        """
        Creates a :class:`ManifestEntry <overture_song.model.ManifestEntry>` object

        :param data: Any object with members named 'objectId', 'fileName', 'fileMd5sum'.
        :return: :class:`ManifestEntry <overture_song.model.ManifestEntry>` object
        """
        d = data.__dict__
        file_id = get_required_field(d, 'objectId')
        file_name = get_required_field(d, 'fileName')
        md5sum = get_required_field(d, 'fileMd5sum')
        return ManifestEntry(file_id, file_name, md5sum)

    def __str__(self):
        return "{}\t{}\t{}".format(self.fileId, self.fileName, self.md5sum)


class Manifest(object):
    """
    Object representing the contents of a manifest file

    :param str analysis_id: analysisId associated with a collection of :py:class:`ManifestEntry <overture_song.model.ManifestEntry>`

    """
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
