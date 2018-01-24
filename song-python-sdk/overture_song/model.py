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
from abc import abstractmethod

import json
from dataclasses import dataclass, field

from overture_song import utils
from overture_song.utils import check_state
from overture_song.utils import SongClientException, Builder
from typing import *

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


class Validatable(object):
    @abstractmethod
    def validate(self):
        pass

    @classmethod
    def validate_string(cls, value: str):
        utils.check_type(value, str)

    @classmethod
    def validate_int(cls, value: int):
        utils.check_type(value, int)

    @classmethod
    def validate_float(cls, value: float):
        utils.check_type(value, float)

    @classmethod
    def validate_not_none(cls, value: float):
        check_state(value is not None, "The input value cannot be None")

    @classmethod
    def validate_string_not_empty_or_none(cls, value: str):
        Validatable.validate_string(value)
        Validatable.validate_not_none(value)
        check_state(value, "The input string value cannot be empty")

    @classmethod
    def validate_required_string(cls, value: str):
        Validatable.validate_string_not_empty_or_none(value)

    def required(cls, original_function):
        def new_function(value):
            out = original_function(value)
            check_state(out is not None, "The input value cannot be None")

        return new_function

    def string(cls, original_function):
        def new_function(value):
            out = original_function(value)
            check_state(out is not None, "The input value cannot be None")

        return new_function


class Entity(object):

    def to_json(self):
        return json.dumps(self.__dict__)


class DataField(object):
    def __init__(self, name, *types, required=True, multiple=False):
        self.types = types
        self.name = name
        self.required = required
        self.multiple = multiple

    def validate(self, value):
        if self.multiple:
            self._validate_single(list, value)
            if self.required:
                check_state(len(value) > 0,
                            "The required list datafield '{}' was supplied an empty array",
                            self.name)
            for t in self.types:
                for item in value:
                    self._validate_single(t, item)
        else:
            for t in self.types:
                self._validate_single(t, value)

    def _validate_single(self, t, value):
        if self.required:
            check_state(value is not None, "The datafield '{}' is required", self.name)

        if value is not None:
            if self.required:
                if t is str:
                    check_state(value, "The required string datafield '{}' was supplied an empty string ",
                                self.name)
            check_state(isinstance(value, t),
                        "The datafield '{}' is of '{}' type, but was supplied a value of type '{}' with value '{}'",
                        self.name, t, type(value), value)


"""
    Validation decorator that intercepts "setting" of properties/attributes defined by the @dataclass decorator. 
    Once intercepted, it validates the input value to set against predefined rules, encapsulated in a DataField class
"""

#TODO: Update implementation to process dataclass definition. Should extract properties, their types, if they are a
# list or not, if they are required or optional
class validation(object):
    def __init__(self, *datafields):
        self.datafields = list(datafields)
        utils.check_type(self.datafields, list)
        check_state(len(self.datafields) > 0, "Must define atleast one datafield")
        self.name_type_map = {}
        for datafield in self.datafields:
            utils.check_type(datafield, DataField)
            if datafield.name not in self.name_type_map:
                self.name_type_map[datafield.name] = {}
            for t in datafield.types:
                t_name = t.__name__

                if t_name in self.name_type_map[datafield.name]:
                    raise Exception(
                        "Collision: The datafield definition '{}' already exists as '{}' for the type '{}'".format(
                            datafield, self.name_type_map[datafield.name][t_name], t_name))
                self.name_type_map[datafield.name][t_name] = datafield

    def __call__(self, Cls):
        name_type_map = self.name_type_map
        datafields = self.datafields

        class Validator(object):

            SPECIAL_FIELD = '__dataclass_fields__'

            def __init__(self, *args, **kwargs):

                self._instance = Cls(*args, **kwargs)
                check_state(Validator.SPECIAL_FIELD in Cls.__dict__,
                            "Decorator can only process dataclasses")
                self._internal_dict = Cls.__dict__[Validator.SPECIAL_FIELD]
                self._check_validator()

            def _check_validator(self):
                available_fields = self._internal_dict.keys()
                undefined_set = set()

                for d in datafields:
                    if d.name not in available_fields:
                        undefined_set.add(d.name)
                num_undefined = len(undefined_set)
                check_state(num_undefined == 0,
                            "The {} datafields [{}] do not exist in the class definition for '{}'",
                            num_undefined,
                            ",".join(undefined_set),
                            Cls.__name__)

            def __setattr__(self, datafield_name, value):
                if datafield_name in name_type_map:
                    for t, d in name_type_map[datafield_name].items():
                        d.validate(value)
                if datafield_name.startswith('_'):
                    object.__setattr__(self, datafield_name, value)
                else:
                    if datafield_name not in Cls.__dict__.keys():
                        raise AttributeError(
                            "The field '{}' is not an attribute of '{}'".format(datafield_name, Cls.__name__))
                    instance = object.__getattribute__(self, '_instance')
                    object.__setattr__(instance, datafield_name, value)



            def __str__(self):
                return str(self._instance)

        return Validator


@validation(
    DataField("analysisId", str),
    DataField("file", str, multiple=True))
@dataclass(frozen=False)
class Analysis(Entity, Validatable):
    analysisId: str = None
    study: str = None
    analysisState: str = None

    # TODO: add typing to this. should be a list of type Sample
    sample: list = field(repr=True, default=[])

    # TODO: add typing to this. should be a list of type File
    file: list = field(repr=True, default=[])

    @classmethod
    def builder(cls):
        return Builder(Analysis)

    @classmethod
    def from_json(cls, json_string):
        pass

    def validate(self):
        pass


class Metadata(object):
    def __init__(self):
        self._info = {}

    def set_info(self, key: str, value: Any):
        self._info[key] = value

    def add_info(self, data: dict):
        if utils.is_none_or_empty(data):
            return
        self._info.update(data)

    @property
    def info(self):
        return self._info


class Experiment(Metadata):
    pass


@dataclass(frozen=False)
class VariantCall(Experiment, Validatable):
    analysisId: str = None
    variantCallingTool: str = None
    matchedNormalSampleSubmitterId: str = None

    @classmethod
    def builder(cls):
        return Builder(Analysis)

    def validate(self):
        pass


@dataclass(frozen=False)
class SequencingRead(Experiment, Validatable):
    analysisId: str = None
    aligned: bool = None
    alignmentTool: str = None
    insertSize: int = None
    libraryStrategy: str = None
    pairedEnd: bool = None
    referenceGenome: str = None

    @classmethod
    def builder(cls):
        return Builder(Analysis)

    def validate(self):
        pass


@dataclass(frozen=False)
class SequencingReadAnalysis(Analysis):
    # TODO: add typing to this. should be a list of type File
    experiment: SequencingRead = None
