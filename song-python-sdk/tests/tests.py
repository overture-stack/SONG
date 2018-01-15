
import unittest

from song.model import Study, SongError, ApiConfig, ManifestEntry, Manifest
from song.rest import BeanRest, Rest, JsonRest
from song.client import Api, StudyClient, UploadClient
from song.client import ManifestClient
import song.utils as utils
import os, time
import hashlib


class TestFile(object):

    def __init__(self, filename):
        self.name =  filename
        self.m = hashlib.md5()

    def is_exists(self):
        return os.path.exists(self.name)

    def get_md5(self):
        if not self.is_exists():
            raise Exception("File '{}' DNE and cannot calculate md5".format(self.name))
        m = hashlib.md5()
        with open(self.name, 'r') as f:
            for chunk in iter(lambda : f.read(4096), b""):
                m.update(chunk)
        return m.hexdigest()

    def md5_compare(self, other_testFile):
        if not isinstance(other_testFile, TestFile):
            raise Exception("The input argument must be an instance of TestFile")
        return self.get_md5() == other_testFile.get_md5()

    def delete(self):
        if self.is_exists():
            os.remove(self.name)


class TempFileStorage(object):

    def __init__(self, test_dir):
        self.test_dir = test_dir
        utils.create_dir(test_dir)
        self.file_map = {}
        self.file_prefix = "tmp_{}_".format(int(time.time()))
        self.count = 0

    def add(self, file_path_name):
        self.file_map[file_path_name] = TestFile(file_path_name)

    def __generate_file(self):
        filename = self.test_dir+os.sep+self.file_prefix+str(self.count)
        self.count += 1
        return TestFile(filename)

    def get_test_file(self):
        found = False
        max_rep = 40
        rep_count = 0
        test_file = None
        while not found:
            test_file = self.__generate_file()
            if not test_file.is_exists():
                found = True
            if rep_count >= max_rep:
                raise Exception("Exceeded retry count of {}".format(max_rep))
            rep_count += 1
        self.add(test_file.name)
        return test_file

    def clean(self):
        [test_file.delete() for test_file in self.file_map.values()]


class SongTests(unittest.TestCase):

    def __init__(self, methodName='runTest'):
        super(SongTests, self).__init__(methodName)
        self.fixture_dir = "./fixtures"
        self.file_storage = TempFileStorage("./tempTestFiles")

    def _get_fixture_filename(self, filename):
        return self.fixture_dir+os.sep+filename

    def test_manifest_generation(self):
        expected_test_file = TestFile(self._get_fixture_filename("expected_test_manifest_generation.txt"))
        actual_test_file = self.file_storage.get_test_file()
        analysis_id = "AN1"
        manifest_entry_1 = ManifestEntry("fileId_1","myFileName1.txt", "12345qwert_1")
        manifest_entry_2 = ManifestEntry("fileId_2","myFileName2.txt", "12345qwert_2")
        manifest_entry_3 = ManifestEntry("fileId_3","myFileName3.txt", "12345qwert_3")
        manifest = Manifest(analysis_id, [manifest_entry_1, manifest_entry_2, manifest_entry_3])
        manifest.write(actual_test_file.name, overwrite=True)
        self.assertTrue(actual_test_file.md5_compare(expected_test_file))
        self.file_storage.clean()

    def test_example(self):
        url = 'http://rtisma-server:8080'
        study_id = 'BRCA-EU'
        access_token = 'token'
        debug = False

        config = ApiConfig(url, study_id, access_token, debug=debug)
        api = Api(config)

        self.assertEquals(api.is_alive(), True)

        study_client = StudyClient(api)
        upload_client = UploadClient(api)
        sequencing_read_filename = '/Users/rtisma/Documents/workspace/song_overture/src/test/resources/fixtures/sequencingRead.json'
        if not study_client.has(study_id):
            study_client.create(Study(study_id, "none", "ICGC", "Soemthing"))

        upload_response = upload_client.upload_file(sequencing_read_filename, is_async_validation=False)
        if upload_response.status == 'ok':
            status = upload_client.check_upload_status(upload_response.uploadId)

        save_response = upload_client.save(status.uploadId)

        upload_client.publish(save_response.analysisId)

        print("sdfsdfsf")






if __name__ == '__main__':
    unittest.main()
