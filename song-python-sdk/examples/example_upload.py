from time import sleep
from overture_song.client import Api, StudyClient, ManifestClient
from overture_song.entities import Study, Donor, Specimen, Sample, File, SequencingRead

from overture_song.model import ApiConfig
from overture_song.tools import SimplePayloadBuilder
from overture_song.utils import check_state


def upload(config):
    api = Api(config)
    check_state(api.is_alive(), "The server '{}' is not running", config)
    study_client = StudyClient(api)
    if not study_client.has(config.study_id):
        study_client.create(Study.create(config.study_id))
    donor = Donor()
    donor.studyId = config.study_id
    donor.donorGender = "male"
    donor.donorSubmitterId = "dsId1"
    donor.set_info("randomDonorField", "someDonorValue")

    specimen = Specimen()
    specimen.specimenClass = "Tumour"
    specimen.specimenSubmitterId = "sp_sub_1"
    specimen.specimenType = "Normal - EBV immortalized"
    specimen.set_info("randomSpecimenField", "someSpecimenValue")

    sample = Sample()
    sample.sampleSubmitterId = "ssId1"
    sample.sampleType = "RNA"
    sample.set_info("randomSample1Field", "someSample1Value")

    # File 1
    file1 = File()
    file1.fileName = "myFilename1.bam"
    file1.studyId = config.study_id
    file1.fileAccess = "controlled"
    file1.fileMd5sum = "myMd51"
    file1.fileSize = 1234561
    file1.fileType = "VCF"
    file1.set_info("randomFile1Field", "someFile1Value")

    # File 2
    file2 = File()
    file2.fileName = "myFilename2.bam"
    file2.studyId = config.study_id
    file2.fileAccess = "controlled"
    file2.fileMd5sum = "myMd52"
    file2.fileSize = 1234562
    file2.fileType = "VCF"
    file2.set_info("randomFile2Field", "someFile2Value")

    # SequencingRead
    sequencing_read_experiment = SequencingRead()
    sequencing_read_experiment.aligned = True
    sequencing_read_experiment.alignmentTool = "myAlignmentTool"
    sequencing_read_experiment.pairedEnd = True
    sequencing_read_experiment.insertSize = 0
    sequencing_read_experiment.libraryStrategy = "WXS"
    sequencing_read_experiment.referenceGenome = "GR37"
    sequencing_read_experiment.set_info("randomSRField", "someSRValue")

    builder = SimplePayloadBuilder(donor, specimen, sample, [file1, file2], sequencing_read_experiment)

    print("-----------------------Uploading payload-------------------------")
    upload_response = api.upload(json_payload=builder.to_dict(), is_async_validation=False)
    print("upload_response: \n{}".format(upload_response))
    sleep(4)

    print("\n\n\n-------------------Check status----------------------------")
    status_response = api.status(upload_response.uploadId)
    print("status_response: \n{}".format(status_response))
    sleep(4)

    print("\n\n\n----------------------Saving-------------------------------")
    save_response = api.save(status_response.uploadId, ignore_analysis_id_collisions=True)
    print("save_response: \n{}".format(save_response))
    sleep(4)

    manifest_client = ManifestClient(api)
    print("\n\n\n-----------------Creating manifest-------------------------")
    manifest = manifest_client.create_manifest(save_response.analysisId)
    print("manifest_file_contents: \n{}".format(manifest))


def main():
    api_config = ApiConfig("http://my-song-server-host:8080", 'ABC123', "my-access-token", debug=True)
    upload(api_config)

if __name__ == '__main__':
    main()
