import os,sys
from jq import jq
import json

def checkDir(path):
    if not os.path.exists(path) or not os.path.isdir(path):
        raise Exception("The dirpath '%s' does not exist" % path)

def checkFile(path):
    if not os.path.exists(path) or not os.path.isfile(path):
        raise Exception("The filepath '%s' does not exist" % path)

def find(path, file_compare_callback):
    out=[]
    checkDir(path)
    for root, dirs, files in os.walk(path):
        for file in files:
            if file_compare_callback(file):
                out.append(root+os.sep+file)
    return out
def read_file(file):
    checkFile(file)
    with open(file, 'r') as myfile:
        data = myfile.read()
    return data

def read_json(json_filepath):
    checkFile(json_filepath)
    with open(json_filepath, 'r') as myfile:
        data = json.load(myfile)
    return data

def write_json(json_data, json_output_filepath):
    with open(json_output_filepath, 'w') as outfile:
        data = json.dump(json_data, outfile, indent=2)

def write_to_file(file, data):
    with open(file, "w") as text_file:
        text_file.write(data)

"""
Transformations

- delete specimenClass
- delete specimenType
- add specimenTissueSource: Solid tissue
- add tumourNormalDesignation: Normal
"""
def transform_in_place(data):
    result = False
    if 'samples' in data:
        for sample in data['samples']:
            specimen = sample['specimen']
            if 'specimenClass' in specimen:
                if 'tumour' in specimen['specimenClass'].lower():
                    specimen['tumourNormalDesignation'] = 'Primary tumour'
                else:
                    specimen['tumourNormalDesignation'] = 'Normal'
                del specimen['specimenClass']
                result = True

            if 'specimenTissueSource' not in specimen:
                specimen['specimenTissueSource'] = 'Solid tissue'
                result = True
    return result

def process_file(filepath):
    try:
        data = read_json(filepath)
        result = transform_in_place(data)
        if result:
            write_json(data, filepath)
    except Exception as e:
        print ("[ERROR]: error processing "+filepath)

def main():
    dirpaths = sys.argv[1:]
    for dirpath in dirpaths:
        files = find(dirpath, lambda f: f.endswith(".json"))
        [process_file(f) for f in files]


def test():
    for root, dirs, files in os.walk("."):
        path = root.split(os.sep)
        print((len(path) - 1) * '---', os.path.basename(root))
        for file in files:
            print(len(path) * '---', file)

if __name__ == '__main__':
    main()


