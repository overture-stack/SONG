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
    donor.gender = "Male"
    donor.submitterDonorId = "dsId1"
    donor.set_info("randomDonorField", "someDonorValue")

    specimen = Specimen()
    specimen.tumourNormalDesignation = "Tumour"
    specimen.submitterSpecimenId = "sp_sub_1"
    specimen.specimenType = "Normal"
    specimen.specimenTissueSource = "Solid tissue"
    specimen.set_info("randomSpecimenField", "someSpecimenValue")

    sample1 = Sample()
    sample1.submitterSampleId = "ssId1"
    sample1.sampleType = "Total RNA"
    sample1.matchedNormalSubmitterSampleId = "sample-x24-11a"
    sample1.set_info("randomSample1Field", "someSample1Value")

    # File 1
    file1 = File()
    file1.fileName = "example.bam"
    file1.studyId = config.study_id
    file1.fileAccess = "controlled"
    file1.fileMd5sum = "73a90acaae2b1ccc0e969709665bc62f"
    file1.fileSize = 1234561
    file1.fileType = "BAM"
    file1.dataType = "a dataType here"
    file1.set_info("randomFile1Field", "someFile1Value")

    # File 2
    file2 = File()
    file2.fileName = "example.bam.bai"
    file2.studyId = config.study_id
    file2.fileAccess = "controlled"
    file2.fileMd5sum = "67a97437c474cc1d95e0a1672b291b70"
    file2.fileSize = 1234562
    file2.fileType = "BAI"
    file2.dataType = "another dataType here"
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

    builder = SimplePayloadBuilder(donor, specimen, [sample1], [file1, file2], sequencing_read_experiment, None, config.study_id)

    print("-----------------------Submit payload-------------------------")
    print("payload:" + str(builder.to_dict()))
    submit_response = api.submit(json_payload=builder.to_dict())
    print("submit_response: \n{}".format(submit_response))
    sleep(4)

    print("\n\n\n-------------------Get UNPUBLISHED analysis----------------------------")
    get_analysis_response = api.get_analysis(submit_response.analysisId)
    print("get_analysis_response: \n{}".format(get_analysis_response))
    sleep(4)

    source_dir = "./"
    manifest_client = ManifestClient(api)
    print("\n\n\n-----------------Creating manifest-------------------------")
    manifest = manifest_client.create_manifest(source_dir, submit_response.analysisId)
    print("manifest_file_contents: \n{}".format(manifest))

    # NOTE:
    # If the files exist, then the following command can used to write the manifest to a file.
    #
    # output_manifest_filepath = "../manifest.txt"
    # manifest_client.write_manifest(save_response.analysisId, source_dir, output_manifest_filepath)


    # TODO: Upload the object files specified in the payload, using the `icgc-storage-client` and the manifest file.

    # TODO: After object files are succesfully uploaded. Publish the analysis using `api.publish(analysisId)`


def main():
    api_config = ApiConfig("http://localhost:8080", 'ABC123', "f69b726d-d40f-4261-b105-1ec7e6bf04d5", debug=True)
    upload(api_config)


if __name__ == '__main__':
    main()
