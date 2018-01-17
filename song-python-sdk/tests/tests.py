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

import unittest

import os
import time
import hashlib
from overture_song.tools import EGAUploader, FileUploadState
from overture_song.model import ApiConfig, ManifestEntry, Manifest
from overture_song.client import Api
import overture_song.utils as utils


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
        with open(self.name, 'rb') as f:
            while True:
                data = f.read(4096)
                if not data:
                    break
                m.update(data)
        return m.digest()

    def get_md5_old(self):
        if not self.is_exists():
            raise Exception("File '{}' DNE and cannot calculate md5".format(self.name))
        m = hashlib.md5()
        with open(self.name, 'r') as f:
            for chunk in iter(lambda: f.read(4096), b""):
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

    def test_upload_state_comparisons(self):

        def verif_compare(lower_state, higher_state):
            self.assertGreater(higher_state, lower_state)
            self.assertGreaterEqual(higher_state, lower_state)
            self.assertLess(lower_state, higher_state)
            self.assertLessEqual(lower_state, higher_state)
            self.assertNotEqual(lower_state, higher_state)

        verif_compare(FileUploadState.UPLOAD_ERROR, FileUploadState.STATUS_ERROR)
        verif_compare(FileUploadState.STATUS_ERROR, FileUploadState.VALIDATION_ERROR)
        verif_compare(FileUploadState.VALIDATION_ERROR, FileUploadState.SAVE_ERROR)
        verif_compare(FileUploadState.SAVE_ERROR, FileUploadState.PUBLISH_ERROR)
        verif_compare(FileUploadState.PUBLISH_ERROR, FileUploadState.UNKNOWN_ERROR)
        verif_compare(FileUploadState.UNKNOWN_ERROR, FileUploadState.NOT_UPLOADED)
        verif_compare(FileUploadState.NOT_UPLOADED, FileUploadState.SUBMITTED)
        verif_compare(FileUploadState.SUBMITTED, FileUploadState.SAVED)
        verif_compare(FileUploadState.VALIDATED, FileUploadState.SAVED)
        verif_compare(FileUploadState.SAVED, FileUploadState.PUBLISHED)

        self.assertLessEqual(FileUploadState.VALIDATED, FileUploadState.PUBLISHED)
        self.assertLess(FileUploadState.VALIDATED, FileUploadState.PUBLISHED)
        self.assertGreaterEqual(FileUploadState.PUBLISHED, FileUploadState.VALIDATED)
        self.assertGreater(FileUploadState.PUBLISHED, FileUploadState.VALIDATED)

    """
    def test_ega_upload(self):
        ega_data_dir = './myPayloadData'

        url = 'https://song-server:8080'
        study_id = 'ABC123'
        access_token = 'token'
        debug = True

        config = ApiConfig(url, study_id, access_token, debug=debug)
        api = Api(config)

        uploader = EGAUploader(url, access_token, ega_data_dir, debug=debug)
        print("Running.....")
        uploader.upload_all()
        uploader.status_all()
        uploader.save_all()
        # uploader.publish_all()
        uploader.print_upload_states()
    """


if __name__ == '__main__':
    unittest.main()
