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

import json
import os
import time
from enum import Enum


def convert_to_url_param_list(delimeter='=', **kwargs):
    param_list = []
    for k,v in kwargs.items():
        param_list.append(k+delimeter+v)
    return param_list


def setup_output_file_path(file_path):
    parent_dir = os.path.dirname(file_path)
    if not os.path.exists(parent_dir):
        os.makedirs(parent_dir)


def create_dir(dir_path):
    if not os.path.exists(dir_path):
        return os.makedirs(dir_path)


def to_json_string(obj):
    return json.dumps(to_dict(obj), default=lambda o: o.__dict__)


def write_object(obj, output_file_path, overwrite=False):
    setup_output_file_path(output_file_path)
    if os.path.exists(output_file_path):
        if os.path.isfile(output_file_path):
            if overwrite:
                os.remove(output_file_path)
            else:
                raise SongClientException(
                    'existing.file',
                    "The file '{}' already exists and overwriting is disabled".format(output_file_path))
        else:
            raise SongClientException(
                'not.a.file',
                "The path '{}' is not a file".format(output_file_path))

    with open(output_file_path, 'w') as fh:
        fh.write(str(obj))


def to_json(obj):
    return json.loads(to_dict(obj))


def to_dict(obj):
    return obj.__dict__


def to_pretty_json_string(json_data_string):
    return json.dumps(json.loads(json_data_string), indent=4, sort_keys=True)


def repeat(value, repeat_number):
    out = ""
    for i in range(0, repeat_number):
        out += value
    return out


def tab_repeat(repeat_number):
    return repeat("\t", repeat_number)


def whitespace_repeat(repeat_number):
    return repeat(" ", repeat_number)


class Stack:
    def __init__(self):
        self.items = []

    def isEmpty(self):
        return self.items == []

    def push(self, item):
        self.items.append(item)

    def pop(self):
        return self.items.pop()

    def peek(self):
        return self.items[len(self.items) - 1]

    def size(self):
        return len(self.items)


class BeanType(type):

    def __call__(self, *args, **kwds):
        out = super().__call__(*args, **kwds)
        type_name = args[0]
        self.__class__.__name__ = type_name
        return out


class DataType(Enum):
    DICT = 0,
    LIST = 1,
    STRING = 3,
    NUMBER = 4


    @classmethod
    def resolve(cls, raw_data):
        if isinstance(raw_data, dict):
            return DataType.DICT
        elif isinstance(raw_data, list):
            return DataType.LIST
        elif isinstance(raw_data, "".__class__):
            return DataType.STRING
        elif raw_data >= 0 or raw_data < 0:
            return DataType.NUMBER
        else:
            raise Exception("should not be here: {}".format(raw_data))


def to_bean(type_name, object):
    """
    Convert a dictionary to an object (recursive).
    """
    def convert(type_name, item):
        if isinstance(item, dict):
            obj = BeanType(type_name, (), {k: convert(create_type_name(k), v) for k, v in item.items()})
            return obj
        if isinstance(item, list):
            def yield_convert(item):
                for index, value in enumerate(item):
                    yield convert(type_name+"_"+str(index), value)
            return list(yield_convert(item))
        else:
            return item

    def create_type_name(key_text):
        return "_".join( word[0].upper() + word[1:] for word in key_text.split())

    return convert(type_name, object)


def check_song_state(expression, error_id, formatted_message, *args):

    if not expression:
        raise SongClientException(error_id, formatted_message.format(*args))


def check_state(expression, formatted_message, *args):

    if not expression:
        raise Exception(formatted_message.format(*args))


def check_file(filename):
    check_state(os.path.exists(filename), "The path {}' does not exist", filename)
    check_state(os.path.isfile(filename), "The path '{}' is not a file ", filename)


def check_dir(dirname):
    check_state(os.path.exists(dirname), "The path {}' does not exist", dirname)
    check_state(os.path.isdir(dirname), "The path '{}' is not a directory ", dirname)


def check_type(instance, class_type):
    check_state(isinstance(class_type, type), "The right input argument must be of class type")
    check_state(isinstance(instance, class_type),
                "The input instance is not of type '{}'",
                class_type.__class__.__name__)


class SongClientException(Exception):

    def __init__(self, error_id, message):
        self.message = message
        self.error_id = error_id
        self.timestamp = int(time.time())

    def __str__(self):
        return "[SONG_CLIENT_EXCEPTION {} @ {}]: {}".format(self.error_id, self.timestamp, self.message)