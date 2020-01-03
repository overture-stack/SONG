import os,sys
import json
import collections

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
        d = collections.OrderedDict()
    return data


def read_json(json_filepath):
    checkFile(json_filepath)
    with open(json_filepath, 'r') as myfile:
        data = json.load(myfile, object_pairs_hook=collections.OrderedDict)
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
def transform_in_place_old(data):
    result = False
    if 'samples' in data:
        variantCallMNSSId = None
        if 'experiment' in data:
            experiment = data['experiment']
            if 'matchedNormalSampleSubmitterId' in experiment:
                variantCallMNSSId = experiment['matchedNormalSampleSubmitterId']
        for sample in data['samples']:
            specimen = sample['specimen']
            if 'tumourNormalDesignation' in specimen:
                if specimen['tumourNormalDesignation'] == 'Primary tumour':
                    specimen['tumourNormalDesignation'] = 'Tumour'
                    specimen['specimenType'] = 'Primary tumour'
                    if variantCallMNSSId is None:
                        sample['matchedNormalSampleSubmitterId'] = 'MNSS01'
                    else:
                        sample['matchedNormalSampleSubmitterId'] = variantCallMNSSId
                else:
                    specimen['tumourNormalDesignation'] = 'Normal'
                    specimen['specimenType'] = 'Normal'
                result = True
    return result

def transform_in_place(data):
    result = False
    if 'samples' in data:
        for sample in data['samples']:
            if 'matchedNormalSampleSubmitterId' in sample:
                sample['matchedNormalSubmitterSampleId'] = sample['matchedNormalSampleSubmitterId']
                del sample['matchedNormalSampleSubmitterId']
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


