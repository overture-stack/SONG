==============
Song Tutorial
==============

Step 1 - Getting Set Up
=========================

Getting DACO Access
---------------------
SONG servers use the `auth.icgc.org <https://auth.icgc.org>`_ OAuth2 authorization service to authorize secure API requests.
In order to create the neccessary access tokens to interact with the song-python-sdk and the SONG server,
the user **must** have DACO access. For more information about obtaining DACO access, please visit the instructions for
`DACO Cloud Access <http://docs.icgc.org/cloud/guide/#daco-cloud-access>`_.

.. _access-token-ref:

Creating an Access Token
-------------------------
With proper DACO access, the user can create an access token, using
the `Access Tokens <http://docs.icgc.org/cloud/guide/#access-tokens>`_
and `Token Manager <http://docs.icgc.org/cloud/guide/#token-manager>`_ instructions.

For each cloud environment, there is a specific authorization scope that is needed:

* For the **Collaboratory - Toronto** SONG Server (https://song.cancercollaboratory.org), the required authorization scope needed is **collab.upload**.
* For the **AWS - Virginia** SONG Server (https://virginia.song.icgc.org), the required authorization scope needed is **aws.upload**.

Installing Song
----------------
The official SONG Python SDK is publically hosted on `PyPi <https://pypi.python.org/pypi/overture-song>`_. To install it, just run the command below:

.. code-block:: bash

    pip install overture-song


.. warning::
    Python ``3.6`` or higher is **required**.


Installing and Configuring the icgc-storage-client
-----------------------------------------------------

The `icgc-storage-client <http://docs.icgc.org/cloud/guide/#storage-client-usage>`_ must be download and configured. The client
is used to upload files to an authorized storage server. There are a few storage servers available for DACO users to use:

* In **Collaboratory - Toronto** , the https://storage.cancercollaboratory.org storage servers are used and require ``collab.upload`` scope for uploading files.

* In **AWS - Virginia** , the https://virginia.cloud.icgc.org storage servers are used and require ``aws.upload`` scope for uploading files.

For installation, please see `Installing icgc-storage-client from Tarball <http://docs.icgc.org/cloud/guide/#install-from-tarball>`_ instructions.


For configuration, after un-archiving the tarball, modify the ``./conf/application.properties`` by adding the line:

.. code-block:: bash

    accessToken=<my_access_token>

where the accessToken has the appropriate scope.

Step 2 - Example Usage
=======================
This section demonstrates example usage of the ``overture-song`` sdk.
After completing this example, you will have uploaded your first SONG metadata payload\!

For the impatient, the code used below can be
found in `examples/example_upload.py <https://github.com/overture-stack/SONG/blob/develop/song-python-sdk/examples/example_upload.py>`_.

Configuration
---------------

Create an :class:`ApiConfig <overture_song.model.ApiConfig>` object. This object contains the ``serverUrl``, ``accessToken``, and ``studyId``
that will be used to interact with the SONG API. In this example we will use https://song.cancercollaboratory.org for
the serverUrl and 'ABC123' for the studyId. For the access token, please refer to :ref:`access-token-ref`.

.. code-block:: python

    from overture_song.model import ApiConfig
    api_config = ApiConfig('https://song.cancercollaboratory.org', 'ABC123', <my_access_token>)


Next the main API client needs to be instantiated in order to interact with the SONG server.

.. code-block:: python

    from overture_song.client import Api
    api = Api(api_config)

As a sanity check, ensure that the server is running. If the response is ``True``, then you may proceed with the next
section, otherwise the server is not running.

    >>> api.is_alive()
    True


Create a Study
-----------------

If the studyId 'ABC123' does not exist, then the :class:`StudyClient <overture_song.client.StudyClient>` must be
instantiated in order to read and create studies.

First create a study client,

.. code-block:: python

    from overture_song.client import StudyClient
    study_client = StudyClient(api)


If the study associated with the payload does not exist, then create
a :class:`Study <overture_song.entities.Study>` entity,

.. code-block:: python

   from overture_song.entities import Study
   if not study_client.has(api_config.study_id):
        study = Study.create(api_config.study_id, "myStudyName", "myStudyDescription", "myStudyOrganization")
        study_client.create(study)


Create a Simple Payload
--------------------------
Now that the study exists, you can create your first payload\!
In this example, a :class:`SequencingReadAnalysis <overture_song.entities.SequencingRead>` will be created.
It follows the
`SequencingRead JsonSchema <https://github.com/overture-stack/SONG/tree/develop/song-server/src/main/resources/schemas/sequencingRead.json>`_.

.. seealso::
    Similarly, for the :class:`VariantCallAnalysis <overture_song.entities.VariantCallAnalysis>`, refer to the
    `VariantCall JsonSchema <https://github.com/overture-stack/SONG/tree/develop/song-server/src/main/resources/schemas/variantCall.json>`_.

Firstly, import all the entities to minimize the import statements.

.. code-block:: python

    from overture_song.entities import *

Next, create an example :class:`Donor <overture_song.entities.Donor>` entity:

.. code-block:: python

    donor = Donor()
    donor.studyId = api_config.study_id
    donor.donorGender = "male"
    donor.donorSubmitterId = "dsId1"
    donor.set_info("randomDonorField", "someDonorValue")

Create an example :class:`Specimen <overture_song.entities.Specimen>` entity:

.. code-block:: python

    specimen = Specimen()
    specimen.specimenClass = "Tumour"
    specimen.specimenSubmitterId = "sp_sub_1"
    specimen.specimenType = "Normal - EBV immortalized"
    specimen.set_info("randomSpecimenField", "someSpecimenValue")


Create an example :class:`Sample <overture_song.entities.Sample>` entity:

.. code-block:: python

    sample = Sample()
    sample.sampleSubmitterId = "ssId1"
    sample.sampleType = "RNA"
    sample.set_info("randomSample1Field", "someSample1Value")


Create 1 or more example :class:`File <overture_song.entities.File>` entities:

.. code-block:: python

    # File 1
    file1 = File()
    file1.fileName = "myFilename1.bam"
    file1.studyId = api_config.study_id
    file1.fileAccess = "controlled"
    file1.fileMd5sum = "myMd51"
    file1.fileSize = 1234561
    file1.fileType = "VCF"
    file1.set_info("randomFile1Field", "someFile1Value")

    # File 2
    file2 = File()
    file2.fileName = "myFilename2.bam"
    file2.studyId = api_config.study_id
    file2.fileAccess = "controlled"
    file2.fileMd5sum = "myMd52"
    file2.fileSize = 1234562
    file2.fileType = "VCF"
    file2.set_info("randomFile2Field", "someFile2Value")

Create an example :class:`SequencingRead <overture_song.entities.SequencingRead>` experiment entity:

.. code-block:: python

    # SequencingRead
    sequencing_read_experiment = SequencingRead()
    sequencing_read_experiment.aligned = True
    sequencing_read_experiment.alignmentTool = "myAlignmentTool"
    sequencing_read_experiment.pairedEnd = True
    sequencing_read_experiment.insertSize = 0
    sequencing_read_experiment.libraryStrategy = "WXS"
    sequencing_read_experiment.referenceGenome = "GR37"
    sequencing_read_experiment.set_info("randomSRField", "someSRValue")

Finally, use the :class:`SimplePayloadBuilder <overture_song.tools.SimplePayloadBuilder>` class along with the previously
create entities to create a payload.

.. code-block:: python

    from overture_song.tools import SimplePayloadBuilder
    builder = SimplePayloadBuilder(donor, specimen, sample, [file1, file2], sequencing_read_experiment)
    payload = builder.to_dict()

Use a Custom AnalysisId
--------------------------
In some situations, the user may prefer to use a custom ``analysisId``. If not specified in the payload, it is
automatically generated by the SONG server during the :ref:`save-the-analysis-ref` step.
Although this tutorial uses the ``analysisId`` generated by the SONG server, a custom ``analysisId`` can be set
as follows:

.. code-block:: python

    payload['analysisId'] = 'my_custom_analysis_id'


Upload the Payload
-------------------
With the payload built, the data can now be uploaded to the SONG server for validation. There are 2 modes for validation:

a. **Synchronous** - uploads are validated SYNCHRONOUSLY. Although this is the default mode, it can be selected by setting the kwarg ``is_async_validation`` to ``False`` from the :func:`upload <overture_song.client.Api.upload>` method.
b. **Asynchronously** - uploads are validated ASYNCHRONOUSLY. This allows the user to upload a batch of payloads. This mode can be selected by setting ``is_async_validation`` to ``True``.

After calling the :func:`upload <overture_song.client.Api.upload>` method, the payload will be sent to the SONG server for validation, and a response will be returned:

.. code-block:: python

    >>> api.upload(json_payload=payload, is_async_validation=False)
    {
        "status": "ok",
        "uploadId": "UP-c49742d0-1fc8-4b45-9a1c-ea58d282ac58"
    }

If the ``status`` field from the response is ``ok``, this means the payload was successfully submitted to the SONG server for validation, and returned a randomly generated ``uploadId``, which is a receipt for the upload request.

Check the Status of the Upload
-------------------------------

Before continuing, the previous upload's status must be checked using the
:func:`status <overture_song.client.Api.status>`
method, in order to ensure the payload was successfully validated.
Using the previous ``uploadId``, the status of the upload can be requested and will return the following response:

.. code-block:: python

    >>> api.status('UP-c49742d0-1fc8-4b45-9a1c-ea58d282ac58')
    {
        "analysisId": "",
        "uploadId": "UP-c49742d0-1fc8-4b45-9a1c-ea58d282ac58",
        "studyId": "ABC123",
        "state": "VALIDATED",
        "createdAt": [
            2019,
            2,
            16,
            0,
            54,
            31,
            73774000
        ],
        "updatedAt": [
            2019,
            2,
            16,
            0,
            54,
            31,
            75476000
        ],
        "errors": [
            ""
        ],
        "payload": {
            "analysisState": "UNPUBLISHED",
            "sample": [
                {
                    "info": {
                        "randomSample1Field": "someSample1Value"
                    },
                    "sampleSubmitterId": "ssId1",
                    "sampleType": "RNA",
                    "specimen": {
                        "info": {
                            "randomSpecimenField": "someSpecimenValue"
                        },
                        "specimenSubmitterId": "sp_sub_1",
                        "specimenClass": "Tumour",
                        "specimenType": "Normal - EBV immortalized"
                    },
                    "donor": {
                        "info": {
                            "randomDonorField": "someDonorValue"
                        },
                        "donorSubmitterId": "dsId1",
                        "studyId": "Study1",
                        "donorGender": "male"
                    }
                }
            ],
            "file": [
                {
                    "info": {
                        "randomFile1Field": "someFile1Value"
                    },
                    "fileName": "myFilename1.bam",
                    "studyId": "Study1",
                    "fileSize": 1234561,
                    "fileType": "VCF",
                    "fileMd5sum": "myMd51",
                    "fileAccess": "controlled"
                },
                {
                    "info": {
                        "randomFile2Field": "someFile2Value"
                    },
                    "fileName": "myFilename2.bam",
                    "studyId": "Study1",
                    "fileSize": 1234562,
                    "fileType": "VCF",
                    "fileMd5sum": "myMd52",
                    "fileAccess": "controlled"
                }
            ],
            "analysisType": "sequencingRead",
            "experiment": {
                "info": {
                    "randomSRField": "someSRValue"
                },
                "aligned": true,
                "alignmentTool": "myAlignmentTool",
                "insertSize": 0,
                "libraryStrategy": "WXS",
                "pairedEnd": true,
                "referenceGenome": "GR37"
            }
        }
    }


In order to continue with the next section, the ``state`` field **MUST** have the value ``VALIDATED``, which indicates
the upload was validated and there were no errors. If there were errors, the ``state`` field would have the value
``VALIDATION_ERROR``, and the field ``errors`` would contains details of the validation issues. If there is an error,
the user can simply correct the payload, re-upload and check the status again.


.. _save-the-analysis-ref:

Save the Analysis
------------------
Once the upload is successfully validated, the upload must be saved using the
:func:`save <overture_song.client.Api.save>`
method.  This generates the following response:


.. code-block:: python

    >>> api.save(status_response.uploadId, ignore_analysis_id_collisions=False)
    {
        "analysisId": "23c61f55-12b4-11e8-b46b-23a48c7b1324",
        "status": "ok"
    }



The value of ``ok`` in the ``status`` field of the response indicates that an analysis was successfully created. The analysis
will contain the same data as the payload, with the addition of server-side generated ids, which are generated by an
centralized id server. By default, the request **DOES NOT IGNORE** analysisId
collisions, however by setting the save method parameter ``ignore_analysis_id_collisions`` to ``True``, collisions will
be ignored. This mechanism is considered an override and is heavily discouraged, however it is necessary considering the
complexities associated with managing genomic data.

Observe the UNPUBLISHED Analysis
---------------------------------
Verify the analysis is **unpublished** by observing the value of the ``analysisState`` field in the response for the
:func:`get_analysis <overture_song.client.Api.get_analysis>` call. The value should be ``UNPUBLISHED``. Also, observe that
the SONG server generated an unique sampleId, specimenId, analysisId and objectId:

.. code-block:: python

    >>> api.get_analysis('23c61f55-12b4-11e8-b46b-23a48c7b1324')
    {
        "analysisType": "sequencingRead",
        "info": {},
        "analysisId": "23c61f55-12b4-11e8-b46b-23a48c7b1324",
        "study": "ABC123",
        "analysisState": "UNPUBLISHED",
        "sample": [
            {
                "info": {
                    "randomSample1Field": "someSample1Value"
                },
                "sampleId": "SA599347",
                "specimenId": "SP196154",
                "sampleSubmitterId": "ssId1",
                "sampleType": "RNA",
                "specimen": {
                    "info": {
                        "randomSpecimenField": "someSpecimenValue"
                    },
                    "specimenId": "SP196154",
                    "donorId": "DO229595",
                    "specimenSubmitterId": "sp_sub_1",
                    "specimenClass": "Tumour",
                    "specimenType": "Normal - EBV immortalized"
                },
                "donor": {
                    "donorId": "DO229595",
                    "donorSubmitterId": "dsId1",
                    "studyId": "ABC123",
                    "donorGender": "male",
                    "info": {}
                }
            }
        ],
        "file": [
            {
                "info": {
                    "randomFile1Field": "someFile1Value"
                },
                "objectId": "f553bbe8-876b-5a9c-a436-ff47ceef53fb",
                "analysisId": "23c61f55-12b4-11e8-b46b-23a48c7b1324",
                "fileName": "myFilename1.bam",
                "studyId": "ABC123",
                "fileSize": 1234561,
                "fileType": "VCF",
                "fileMd5sum": "myMd51                          ",
                "fileAccess": "controlled"
            },
            {
                "info": {
                    "randomFile2Field": "someFile2Value"
                },
                "objectId": "6e2ee06b-e95d-536a-86b5-f2af9594185f",
                "analysisId": "23c61f55-12b4-11e8-b46b-23a48c7b1324",
                "fileName": "myFilename2.bam",
                "studyId": "ABC123",
                "fileSize": 1234562,
                "fileType": "VCF",
                "fileMd5sum": "myMd52                          ",
                "fileAccess": "controlled"
            }
        ],
        "experiment": {
            "analysisId": "23c61f55-12b4-11e8-b46b-23a48c7b1324",
            "aligned": true,
            "alignmentTool": "myAlignmentTool",
            "insertSize": 0,
            "libraryStrategy": "WXS",
            "pairedEnd": true,
            "referenceGenome": "GR37",
            "info": {
                "randomSRField": "someSRValue"
            }
        }
    }

Generate the Manifest
----------------------
With an analysis created, a manifest file must be generated using the
:class:`ManifestClient <overture_song.client.ManifestClient>`
, the analysisId from the previously generated analysis, and an output file path. By calling the
:func:`write_manifest <overture_song.client.ManifestClient.write_manifest>` method, a
:class:`Manifest <overture_song.model.Manifest>` object is generated and then written to a file.
This step is required for the next section involving the upload of the object files to the storage server.

.. code-block:: python

    from overture_song.client import ManifestClient
    manifest_client = ManifestClient(api)
    manifest_file_path = './manifest.txt'
    manifest_client.write_manifest('23c61f55-12b4-11e8-b46b-23a48c7b1324', manifest_file_path)

After successful execution, a ``manifest.txt`` file will be generated and will have the following contents:

.. code-block:: bash

    23c61f55-12b4-11e8-b46b-23a48c7b1324
    f553bbe8-876b-5a9c-a436-ff47ceef53fb    myFilename1.bam    myMd51
    6e2ee06b-e95d-536a-86b5-f2af9594185f    myFilename2.bam    myMd52


Upload the Object Files
-------------------------

Upload the object files specified in the payload, using the `icgc-storage-client` and the manifest file.
This will upload the files specified in the ``manifest.txt`` file, which should all be located in the same directory.

For **Collaboratory - Toronto**:

.. code-block:: bash

    ./bin/icgc-storage-client --profile collab   upload --manifest ./manifest.txt

For **AWS - Virginia**:

.. code-block:: bash

    ./bin/icgc-storage-client --profile aws   upload --manifest ./manifest.txt

.. seealso::

    For more information about the **icgc-storage-client** usage, visit the `usage guide <http://docs.icgc.org/cloud/guide/#storage-client-usage>`_.

Publish the Analysis
---------------------
Using the same ``analysisId`` as before, publish it.
Essentially, this is the handshake between the metadata stored in the SONG server (via the analysisIds) and the object
files stored in the storage server (the files described by the ``analysisId``)

.. code-block:: python

    >>> api.publish('23c61f55-12b4-11e8-b46b-23a48c7b1324')
    AnalysisId 23c61f55-12b4-11e8-b46b-23a48c7b1324 successfully published


Observe the PUBLISHED Analysis
---------------------------------
Finally, verify the analysis is published by observing the value of the ``analysisState`` field in the response for the
:func:`get_analysis <overture_song.client.Api.get_analysis>` call. If the value is ``PUBLISHED``, then **congratulations on your first metadata upload\!\!**

.. code-block:: python

    >>> api.get_analysis('23c61f55-12b4-11e8-b46b-23a48c7b1324')
    {
        "analysisType": "sequencingRead",
        "info": {},
        "analysisId": "23c61f55-12b4-11e8-b46b-23a48c7b1324",
        "study": "ABC123",
        "analysisState": "PUBLISHED",
        "sample": [
            {
                "info": {
                    "randomSample1Field": "someSample1Value"
                },
                "sampleId": "SA599347",
                "specimenId": "SP196154",
                "sampleSubmitterId": "ssId1",
                "sampleType": "RNA",
                "specimen": {
                    "info": {
                        "randomSpecimenField": "someSpecimenValue"
                    },
                    "specimenId": "SP196154",
                    "donorId": "DO229595",
                    "specimenSubmitterId": "sp_sub_1",
                    "specimenClass": "Tumour",
                    "specimenType": "Normal - EBV immortalized"
                },
                "donor": {
                    "donorId": "DO229595",
                    "donorSubmitterId": "dsId1",
                    "studyId": "ABC123",
                    "donorGender": "male",
                    "info": {}
                }
            }
        ],
        "file": [
            {
                "info": {
                    "randomFile1Field": "someFile1Value"
                },
                "objectId": "f553bbe8-876b-5a9c-a436-ff47ceef53fb",
                "analysisId": "23c61f55-12b4-11e8-b46b-23a48c7b1324",
                "fileName": "myFilename1.bam",
                "studyId": "ABC123",
                "fileSize": 1234561,
                "fileType": "VCF",
                "fileMd5sum": "myMd51                          ",
                "fileAccess": "controlled"
            },
            {
                "info": {
                    "randomFile2Field": "someFile2Value"
                },
                "objectId": "6e2ee06b-e95d-536a-86b5-f2af9594185f",
                "analysisId": "23c61f55-12b4-11e8-b46b-23a48c7b1324",
                "fileName": "myFilename2.bam",
                "studyId": "ABC123",
                "fileSize": 1234562,
                "fileType": "VCF",
                "fileMd5sum": "myMd52                          ",
                "fileAccess": "controlled"
            }
        ],
        "experiment": {
            "analysisId": "23c61f55-12b4-11e8-b46b-23a48c7b1324",
            "aligned": true,
            "alignmentTool": "myAlignmentTool",
            "insertSize": 0,
            "libraryStrategy": "WXS",
            "pairedEnd": true,
            "referenceGenome": "GR37",
            "info": {
                "randomSRField": "someSRValue"
            }
        }
    }

