import os,sys
from jq import jq

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

def write_to_file(file, data):
    with open(file, "w") as text_file:
        text_file.write(data)

def main():
    dirpaths = sys.argv[1:]
    jq_delete_command = "del(.samples[].specimen.specimenType)"
    # jq_delete_command = "."
    for dirpath in dirpaths:
        files = find(dirpath, lambda f: f.endswith(".json"))
        for file in files:
            input_data = read_file(file)
            os.system("cat % \| jq '%s' \| tee %s > /dev/null" % (file, jq_delete_command, file))
            # output_data = jq(jq_delete_command).transform(text=input_data, text_output=True)
            # write_to_file(output_data)

def test():
    for root, dirs, files in os.walk("."):
        path = root.split(os.sep)
        print((len(path) - 1) * '---', os.path.basename(root))
        for file in files:
            print(len(path) * '---', file)

if __name__ == '__main__':
    main()


