import json
import os
import time


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
    if not overwrite and os.path.exists(output_file_path):
        raise SongClientException('existing.file',
                                  "The file '{}' already exists and overwriting is disabled".format(output_file_path))
    with open(output_file_path, 'w') as fh:
        fh.write(str(obj))


def to_json(obj):
    return json.loads(to_dict(obj))


def to_dict(obj):
    return obj.__dict__


def to_pretty_json_string(json_data_string):
    return json.dumps(json.loads(json_data_string), indent=4, sort_keys=True)


def to_bean(item):
    """
    Convert a dictionary to an object (recursive).
    """
    def convert(item):
        if isinstance(item, dict):
            return type('bean', (), {k: convert(v) for k, v in item.items()})
        if isinstance(item, list):
            def yield_convert(item):
                for index, value in enumerate(item):
                    yield convert(value)
            return list(yield_convert(item))
        else:
            return item

    return convert(item)

def check_song_state(expression, id, formatted_message, *args):

    if not expression:
        raise SongClientException(id, formatted_message.format(*args))

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

    def __init__(self, id, message):
        self.message = message
        self.id = id
        self.timestamp = int(time.time())

    def __str__(self):
        return "[SONG_CLIENT_EXCEPTION {} @ {}]: {}".format(self.id, self.timestamp, self.message)