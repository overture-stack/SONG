import os
import json


def convert_to_url_param_list(delimeter='=', **kwargs):
    param_list = []
    for k,v in kwargs.items():
        param_list.append(k+delimeter+v)
    return param_list


def setup_output_file_path(file_path):
    parent_dir = os.path.dirname(file_path)
    os.makedirs(parent_dir, exist_ok=True)


def to_json_string(obj):
    return json.dumps(to_dict(obj), default=lambda o: o.__dict__)


def to_dict(obj):
    return obj.__dict__


def to_pretty_json_string(json_data_string):
    return json.dumps(json.loads(json_data_string), indent=4, sort_keys=True)
