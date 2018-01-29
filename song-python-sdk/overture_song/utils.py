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
from collections import OrderedDict


def convert_to_url_param_list(delimeter='=', **kwargs):
    param_list = []
    for k, v in kwargs.items():
        param_list.append(k+delimeter+v)
    return param_list


def setup_output_file_path(file_path):
    parent_dir = os.path.dirname(file_path)
    if not os.path.exists(parent_dir):
        os.makedirs(parent_dir)


def create_dir(dir_path):
    if not os.path.exists(dir_path):
        return os.makedirs(dir_path)


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


"""
    ###############
    ## Description
    ###############
    Builder class that takes a class decorated with the @dataclass decorator, and automatically
    generates 'with_<member_variable>(self, value)' setter methods. The construction of the input class type
    (the dataclass) is constructed and returned when the 'build()' method is called.

    ###############
    ## Issues
    ###############
    Since the methods are generated dynamically and after PyCharm indexes class attributes (i.e method names),
    the intellisense feature in PyCharm (and possibly any other IDE) will not work for Builder object. For instance,
    the builder for a simple dataclass such as  Person(first_name:str, last_name:str), would not have intellisense
    available for Builder(Person).with_first_name("my first_name"), however would run as expected.
    Instead, the developer would have to reference the Person class definition and deduce the with_ setter name.
"""


class Builder(object):

    def __init__(self, class_type: type):
        self.__members = OrderedDict()
        self.__class_type = class_type
        if '__dataclass_fields__' in class_type.__dict__:
            dd = class_type.__dict__['__dataclass_fields__']
            self.__generate_with_methods(dd)

    def __generate_docs(self, dataclass_fields_dict):
        a = ", ".join(list(map(lambda x: "with_"+x.name+":"+x.type.__name__, dataclass_fields_dict.values())))
        self.__doc__ = "Builder({})".format(a)

    def __generate_with_methods(self, dataclass_fields_dict):
        for k, v in dataclass_fields_dict.items():
            method_name = "with_"+k
            self.__members[k] = None

            def new_func(key):
                def setter(value):
                    self.__members[key] = value
                    return self
                return setter
            setattr(self, method_name, new_func(k))

    def build(self):
        return self.__class_type(*(list(self.__members.values())))


class Stack:
    def __init__(self):
        self.items = []

    def is_empty(self):
        return self.items == []

    def push(self, item):
        self.items.append(item)

    def pop(self):
        return self.items.pop()

    def peek(self):
        return self.items[len(self.items) - 1]

    def size(self):
        return len(self.items)


class GenericObjectType(type):

    def __call__(cls, *args, **kwargs):
        out = super().__call__(*args, **kwargs)
        type_name = args[0]
        cls.__class__.__name__ = type_name
        return out

    def to_dict(self):
        out = {}
        for k, v in self.__dict__.items():
            if not k.startswith("__"):
                out[k] = self._process(v)
        return out

    def _process(self, item):
        if isinstance(item, GenericObjectType):
            return item.to_dict()
        elif isinstance(item, list):
            out = []
            for x in item:
                out.append(self._process(x))
            return out
        else:
            return item

    def __getattribute__(self, name: str):
        try:
            return object.__getattribute__(self, name)
        except:
            return None

    def __repr__(self):
        return self.to_pretty_string()

    def __str__(self):
        return self.to_pretty_string()

    def to_pretty_string(self):
        return json.dumps(self.to_dict(), indent=4)

    def display(self):
        print(self.to_pretty_string())


def to_generic_object(type_name, input_object):
    """
    Convert a dictionary to an object (recursive).
    """
    def convert(type_name_arg, item):
        if isinstance(item, dict):
            obj = GenericObjectType(type_name_arg, (), {k: convert(create_type_name(k), v) for k, v in item.items()})
            return obj
        if isinstance(item, list):
            def yield_convert(item_arg):
                for index, value in enumerate(item_arg):
                    yield convert(type_name_arg+"_"+str(index), value)
            return list(yield_convert(item))
        else:
            return item

    def create_type_name(key_text):
        return "_".join(word[0].upper() + word[1:] for word in key_text.split())

    return convert(type_name, input_object)


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


def default_value(value, init):
    return init if value is None else value


class SongClientException(Exception):

    def __init__(self, error_id, message):
        self.message = message
        self.error_id = error_id
        self.timestamp = int(time.time())

    def __str__(self):
        return "[SONG_CLIENT_EXCEPTION {} @ {}]: {}".format(self.error_id, self.timestamp, self.message)


def _objectize2(name):
    def wrap(original_function):
        def new_function(*args, **kwargs):
            response = original_function(*args, **kwargs)
            return to_generic_object(name, response)
        return new_function
    return wrap


def objectize(original_function):
    name = GenericObjectType.__name__

    def new_function(*args, **kwargs):
        response = original_function(*args, **kwargs)
        return to_generic_object(name, response)
    return new_function


def get_optional_field(dic, field):
    return dic[field] if field in dic else None


def get_required_field(dic, field):
    if field in dic:
        return dic[field]
    else:
        raise SongClientException("study.id.dne", "The field '{}' does not exist in {}".format(field, str(dic)))


