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

import hashlib
import unittest

from dataclasses import *

from overture_song.entities import *
from overture_song.model import *
from overture_song.tools import FileUploadState
from overture_song.utils import *
from overture_song.validation import DataField, validation
from overture_song.validation import non_null


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
        self.maxDiff = None #To see all diffs

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

    def load_json_as_dict(self, json_fixture_filename):
        expected_test_file = TestFile(self._get_fixture_filename(json_fixture_filename))
        return json.load(open(expected_test_file.name, 'r'))

    def test_sequencing_read(self):
        expected_dict  = self.load_json_as_dict("expected_sequencing_read.json")

        sr = SequencingRead()
        sr.analysisId = "an1"
        sr.aligned = True
        sr.alignmentTool = "myAlignmentTool"
        sr.pairedEnd = True
        sr.insertSize = 0
        sr.libraryStrategy = "WXS"
        sr.referenceGenome = "GR37"
        sr.set_info("randomField", "someValue")

        actual_dict = json.loads(sr.to_json())
        self.assertDictEqual(actual_dict, expected_dict)

    def test_metadata(self):
        expected_dict  = self.load_json_as_dict("expected_metadata.json")

        metadata = Metadata()
        metadata.add_info({ "someField1" : "someValue1", "someField2" : "someValue2"})
        metadata.set_info("someField3", "someValue3")

        actual_dict = json.loads(metadata.to_json())
        self.assertDictEqual(actual_dict, expected_dict)

    def test_variant_call(self):
        expected_dict  = self.load_json_as_dict("expected_variant_call.json")

        variant_call = VariantCall()
        variant_call.analysisId = "an1"
        variant_call.matchedNormalSampleSubmitterId = "matched1"
        variant_call.variantCallingTool = "smuffin"
        variant_call.set_info("randomField", "someValue")

        actual_dict = json.loads(variant_call.to_json())
        self.assertDictEqual(actual_dict, expected_dict)

    def test_file(self):
        expected_dict  = self.load_json_as_dict("expected_file.json")

        file = File()
        file.analysisId = "an1"
        file.fileName = "myFilename"
        file.studyId = "myStudyId"
        file.fileAccess = "controlled"
        file.fileMd5sum = "myMd5"
        file.fileSize = 123456
        file.fileType = "VCF"
        file.objectId = "myObjectId"
        file.set_info("randomField", "someValue")
        file.sdfsdfsdf = 234243

        actual_dict = json.loads(file.to_json())
        self.assertDictEqual(actual_dict, expected_dict)

    def test_sample(self):
        expected_dict  = self.load_json_as_dict("expected_sample.json")

        sample = Sample()
        sample.sampleId = "sa1"
        sample.sampleSubmitterId = "ssId1"
        sample.sampleType = "RNA"
        sample.specimenId = "sp1"
        sample.set_info("randomField", "someValue")

        actual_dict = json.loads(sample.to_json())
        self.assertDictEqual(actual_dict, expected_dict)

    def test_specimen(self):
        expected_dict  = self.load_json_as_dict("expected_specimen.json")

        sp = Specimen()
        sp.specimenId = "sp1"
        sp.donorId = "DO1"
        sp.specimenClass = "Tumour"
        sp.specimenSubmitterId = "sp_sub_1"
        sp.specimenType = "Normal - EBV immortalized"
        sp.set_info("randomField", "someValue")

        actual_dict = json.loads(sp.to_json())
        self.assertDictEqual(actual_dict, expected_dict)

    def test_donor(self):
        expected_dict  = self.load_json_as_dict("expected_donor.json")

        d = Donor()
        d.donorId = "DO1"
        d.studyId = "S1"
        d.donorGender = "male"
        d.donorSubmitterId = "sub1"
        d.set_info("randomField", "someValue")

        actual_dict = json.loads(d.to_json())
        self.assertDictEqual(actual_dict, expected_dict)

    def test_composite_entity(self):
        expected_dict = self.load_json_as_dict("expected_composite.json")

        d = Donor()
        d.donorId = "DO1"
        d.studyId = "Study1"
        d.donorGender = "male"
        d.donorSubmitterId = "dsId1"
        d.set_info("randomDonorField", "someDonorValue")

        sp = Specimen()
        sp.specimenId = "sp1"
        sp.donorId = "DO1"
        sp.specimenClass = "Tumour"
        sp.specimenSubmitterId = "sp_sub_1"
        sp.specimenType = "Normal - EBV immortalized"
        sp.set_info("randomSpecimenField", "someSpecimenValue")

        sample = Sample()
        sample.sampleId = "sa1"
        sample.sampleSubmitterId = "ssId1"
        sample.sampleType = "RNA"
        sample.specimenId = "sp1"
        sample.set_info("randomSampleField", "someSampleValue")

        c = CompositeEntity.base_on_sample(sample)
        c.specimen = sp
        c.donor = d
        c.set_info("randomCEField", "someCEValue")

        actual_dict = json.loads(c.to_json())
        self.assertDictEqual(actual_dict, expected_dict)

    def test_analysis(self):
        expected_dict = self.load_json_as_dict("expected_analysis.json")

        #Sample 1
        sample1 = Sample()
        sample1.sampleId = "sa1"
        sample1.sampleSubmitterId = "ssId1"
        sample1.sampleType = "RNA"
        sample1.specimenId = "sp1"
        sample1.set_info("randomSample1Field", "someSample1Value")

        #Sample 2
        sample2 = Sample()
        sample2.sampleId = "sa2"
        sample2.sampleSubmitterId = "ssId2"
        sample2.sampleType = "RNA"
        sample2.specimenId = "sp1"
        sample2.set_info("randomSample2Field", "someSample2Value")

        #File 1
        file1 = File()
        file1.analysisId = "an1"
        file1.fileName = "myFilename1.txt"
        file1.studyId = "Study1"
        file1.fileAccess = "controlled"
        file1.fileMd5sum = "myMd51"
        file1.fileSize = 1234561
        file1.fileType = "VCF"
        file1.objectId = "myObjectId1"
        file1.set_info("randomFile1Field", "someFile1Value")

        #File 2
        file2 = File()
        file2.analysisId = "an1"
        file2.fileName = "myFilename2.txt"
        file2.studyId = "Study1"
        file2.fileAccess = "controlled"
        file2.fileMd5sum = "myMd52"
        file2.fileSize = 1234562
        file2.fileType = "VCF"
        file2.objectId = "myObjectId2"
        file2.set_info("randomFile2Field", "someFile2Value")

        a = Analysis()
        a.analysisId = "an1"
        a.study = "Study1"
        a.analysisState = 'UNPUBLISHED'
        a.sample += [sample1, sample2]
        a.file += [file1, file2]

        actual_dict = json.loads(a.to_json())
        self.assertDictEqual(actual_dict, expected_dict)

    def test_sequencing_read_analysis(self):
        expected_dict = self.load_json_as_dict("expected_sequencing_read_analysis.json")

        #Sample 1
        sample1 = Sample()
        sample1.sampleId = "sa1"
        sample1.sampleSubmitterId = "ssId1"
        sample1.sampleType = "RNA"
        sample1.specimenId = "sp1"
        sample1.set_info("randomSample1Field", "someSample1Value")

        #Sample 2
        sample2 = Sample()
        sample2.sampleId = "sa2"
        sample2.sampleSubmitterId = "ssId2"
        sample2.sampleType = "RNA"
        sample2.specimenId = "sp1"
        sample2.set_info("randomSample2Field", "someSample2Value")

        #File 1
        file1 = File()
        file1.analysisId = "an1"
        file1.fileName = "myFilename1.txt"
        file1.studyId = "Study1"
        file1.fileAccess = "controlled"
        file1.fileMd5sum = "myMd51"
        file1.fileSize = 1234561
        file1.fileType = "VCF"
        file1.objectId = "myObjectId1"
        file1.set_info("randomFile1Field", "someFile1Value")

        #File 2
        file2 = File()
        file2.analysisId = "an1"
        file2.fileName = "myFilename2.txt"
        file2.studyId = "Study1"
        file2.fileAccess = "controlled"
        file2.fileMd5sum = "myMd52"
        file2.fileSize = 1234562
        file2.fileType = "VCF"
        file2.objectId = "myObjectId2"
        file2.set_info("randomFile2Field", "someFile2Value")

        #SequencingRead
        sr = SequencingRead()
        sr.analysisId = "an1"
        sr.aligned = True
        sr.alignmentTool = "myAlignmentTool"
        sr.pairedEnd = True
        sr.insertSize = 0
        sr.libraryStrategy = "WXS"
        sr.referenceGenome = "GR37"
        sr.set_info("randomSRField", "someSRValue")

        #SequencingReadAnalysis
        a = SequencingReadAnalysis()
        a.analysisId = "an1"
        a.study = "Study1"
        a.analysisState = 'UNPUBLISHED'
        a.sample += [sample1, sample2]
        a.file += [file1, file2]
        a.experiment = sr

        actual_dict = json.loads(a.to_json())
        self.assertDictEqual(actual_dict, expected_dict)


    def test_variant_call_analysis(self):
        expected_dict = self.load_json_as_dict("expected_variant_call_analysis.json")

        #Sample 1
        sample1 = Sample()
        sample1.sampleId = "sa1"
        sample1.sampleSubmitterId = "ssId1"
        sample1.sampleType = "RNA"
        sample1.specimenId = "sp1"
        sample1.set_info("randomSample1Field", "someSample1Value")

        #Sample 2
        sample2 = Sample()
        sample2.sampleId = "sa2"
        sample2.sampleSubmitterId = "ssId2"
        sample2.sampleType = "RNA"
        sample2.specimenId = "sp1"
        sample2.set_info("randomSample2Field", "someSample2Value")

        #File 1
        file1 = File()
        file1.analysisId = "an1"
        file1.fileName = "myFilename1.txt"
        file1.studyId = "Study1"
        file1.fileAccess = "controlled"
        file1.fileMd5sum = "myMd51"
        file1.fileSize = 1234561
        file1.fileType = "VCF"
        file1.objectId = "myObjectId1"
        file1.set_info("randomFile1Field", "someFile1Value")

        #File 2
        file2 = File()
        file2.analysisId = "an1"
        file2.fileName = "myFilename2.txt"
        file2.studyId = "Study1"
        file2.fileAccess = "controlled"
        file2.fileMd5sum = "myMd52"
        file2.fileSize = 1234562
        file2.fileType = "VCF"
        file2.objectId = "myObjectId2"
        file2.set_info("randomFile2Field", "someFile2Value")

        #VariantCall
        variant_call = VariantCall()
        variant_call.analysisId = "an1"
        variant_call.matchedNormalSampleSubmitterId = "matched1"
        variant_call.variantCallingTool = "smuffin"
        variant_call.set_info("randomVCField", "someVCValue")

        #VariantCallAnalysis
        a = VariantCallAnalysis()
        a.analysisId = "an1"
        a.study = "Study1"
        a.analysisState = 'UNPUBLISHED'
        a.sample += [sample1, sample2]
        a.file += [file1, file2]
        a.experiment = variant_call

        actual_dict = json.loads(a.to_json())
        self.assertDictEqual(actual_dict, expected_dict)

    #################################3
    # @validation Validation Tests
    #################################3

    def test_person_missing_fields(self):
        error = False
        try:
            s = PersonMissingFields()
        except Exception as e:
            print("\nException: {}".format(e))
            error = True
        self.assertTrue(error)

    def test_strict_person_age_is_string(self):
        error = False
        s = StrictPerson()
        try:
            s.firstName = "John"
            s.lastName = "Doe"
            s.age = "sdf"
        except Exception as e:
            print("\nException: {}".format(e))
            error = True
        self.assertTrue(error)

    def test_strict_person_non_defined_prop(self):
        error = False
        s = StrictPerson()
        try:
            s.firstName = "John"
            s.lastName = "Doe"
            s.age = 23
            s.cars = ["mazda"]
            s.non_defined_prop = "something"
        except Exception as e:
            print("\nException: {}".format(e))
            error = True
        self.assertTrue(error)

    def test_lazy_person_non_defined_prop(self):
        p = LazyPerson()
        p.lastName = 234
        p.non_defined_group = "something"

    def test_dataclass_person(self):
        p = DataClassPerson()
        p.firstName = "Rob"
        p.lastName = 3234
        p.age = "something"
        p.cars = ['mazda', 'vw']
        p.non_defined_group = "this aint good..."

        print ("\nconverted to a dictionary automatically: \n{}".format(asdict(p)))




    def test_constructor_person(self):
        p_good = ConstructorStrictPerson("John", "Doe")
        error = False
        try:
            p_wrong_type = ConstructorStrictPerson("John", 230)
        except Exception as e:
            print("\nException: {}".format(e))
            error = True
        self.assertTrue(error)
        p_good_same = ConstructorStrictPerson("John", "Doe")
        self.assertTrue(p_good  is not p_good_same)
        self.assertTrue(p_good  == p_good_same)

    def test_strict_person_as_dict(self):
        s = StrictPerson()
        s.firstName = "John"
        s.lastName = "Doe"
        s.age = 23
        s.cars = ["mazda", "something"]
        print("dict = {}".format(asdict(s)))

    def test_to_dict_recursion(self):
        d = Donor()
        d.donorId = "DO1"
        d.studyId = "Study1"
        d.donorGender = "male"
        d.donorSubmitterId = "dsId1"
        d.set_info("randomDonorField", "someDonorValue")

        sp = Specimen()
        sp.specimenId = "sp1"
        sp.donorId = "DO1"
        sp.specimenClass = "Tumour"
        sp.specimenSubmitterId = "sp_sub_1"
        sp.specimenType = "Normal - EBV immortalized"
        sp.set_info("randomSpecimenField", "someSpecimenValue")

        sample = Sample()
        sample.sampleId = "sa1"
        sample.sampleSubmitterId = "ssId1"
        sample.sampleType = "RNA"
        sample.specimenId = "sp1"
        sample.set_info("randomSampleField", "someSampleValue")

        c = CompositeEntity.base_on_sample(sample)
        c.specimen = sp
        c.donor = d
        c.set_info("randomCEField", "someCEValue")

        error = False
        try :
            non_recursive_dict = json.dumps(c.__dict__)
        except Exception as e:
            error = True
            print("\nException: {}".format(e))

        self.assertTrue(error)
        print("\nRecursive dictionary generation with dataclasses: \n{}".format(json.dumps(c.to_dict(), indent=4)))

    ##################
    # Failing NON_NULL Tests
    ##################
    @non_null(exclude=["c"])
    def function_c(self, a,b,c):
        pass

    @non_null(exclude=["b"])
    def function_b(self, a,b,c):
        pass

    @non_null(exclude=["a","c"])
    def function_a_c(self, a,b,c):
        pass

    def test_non_null_c(self):
        self.function_c("1", "2", "3")
        self.function_c("1", "2", None)
        error_c = False
        try:
            self.function_c("1", None, "sdf")
        except Exception as e:
            print("Exception: {}".format(e))
            error_c = True
        self.assertTrue(error_c)

    def test_non_null_b(self):
        self.function_b("1", "2", "3")
        self.function_b("1", None, "3")
        error = False
        try:
            self.function_b("1", "2", None)
        except Exception as e:
            print("Exception: {}".format(e))
            error = True
        self.assertTrue(error)

    def test_non_null_a_c(self):
        self.function_a_c("1", "2", "3")
        self.function_a_c(None, "2", None)
        error = False
        try:
            self.function_a_c("1", None, "sdf")
        except Exception as e:
            print("Exception: {}".format(e))
            error = True
        self.assertTrue(error)

    def test_objectize(self):
        obj = self.example_of_objectize()
        error = False
        try:
            obj.not_defined_attribute
        except Exception as e:
            print('Exception: {}'.format(e))
            error=True
        self.assertTrue(error)

        print(obj.firstName)
        print(obj.addresses[0])
        print(obj.addresses[0].country)
        print(obj.addresses[0].coordinates.longitude)
        print("dump the object: \n{}".format(obj))

    @objectize
    def example_of_objectize(self):
        return {
            "firstName" : "Rob",
            "last_name" : "T",
            "addresses" : [
                {
                    "country" : "canada",
                    "coordinates": {
                        "longitude" : 23,
                        "latitude" : 9.03
                    }
                },
                {
                    "country" : "serbia",
                    "coordinates": {
                        "longitude" : 32,
                        "latitude" : 73
                    }
                },
                {
                    "country" : "cuba",
                    "coordinates": {
                        "longitude" : 18,
                        "latitude" : 7
                    }
                }
            ]
        }


#####################################
#   Classes to test the @validation
#   decorator and the
#   @dataclass decorator
#####################################


@validation(
    DataField("firstName", str, required=True),
    DataField("age", int, required=False),
    DataField("cars", str, required=False, multiple=True),
    DataField("lastName", str, required=True))
@dataclass(frozen=False)
class PersonMissingFields(object):
    cars: str = None




@validation(
    DataField("firstName", str, required=True),
    DataField("age", int, required=False),
    DataField("cars", str, required=False, multiple=True),
    DataField("lastName", str, required=True))
@dataclass(frozen=False, init=False)
class StrictPerson(object):
    firstName: str = None
    lastName: str = None
    cars: str = None
    age: int = None


@validation(
    DataField("firstName", str, required=True),
    DataField("lastName", str, required=True),
    DataField("age", int, required=False),
    DataField("cars", str, required=False, multiple=True) )
@dataclass(frozen=False, init=True)
class ConstructorStrictPerson(object):
    firstName: str
    lastName: str
    cars: str = None
    age: int = None


class LazyPerson(object):
    firstName: str = None
    lastName: str = None
    cars: str = None
    age: int = None

@dataclass
class DataClassPerson(object):
    firstName: str = None
    lastName: str = None
    cars: str = None
    age: int = None


if __name__ == '__main__':
    unittest.main()
