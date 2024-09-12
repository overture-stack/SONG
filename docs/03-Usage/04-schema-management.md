# Schema Management 

Song uses <a href="https://json-schema.org/" target="_blank" rel="noopener noreferrer">JSON Schema</a> to describe the structure of metadata that will be stored for each analysis. Data is submitted to Song in JSON format and undergoes validation against the data model schema. This schema ensures the presence of required fields and validates the contents of each field, ensuring adherence to the desired data type and allowed values. This validation process preserves the integrity and quality of the metadata within Song.

## Analysis Schemas

In Song, metadata is captured and submitted as **analyses**. An analysis represents a collection of one or more files and includes a complete metadata record describing those files.

When you submit an analysis to Song, you'll choose an 'analysis type' to dictate the data model used for validation. This is defined in your analysis file using the `analysis type` field.

The schema associated with the analysis type consists of two parts:

1. A minimal, **base schema** containing the essential fields for all analyses, including basic patient data, submitter IDs, and file details.

2. A flexible **dynamic schema** that the Song administrator can configure and upload to define specific analysis types.

These schema components ensure accurate and consistent metadata validation within Song.

## The Song Base Schema

The **base schema** is a minimal data set needed for a schema. The base schema data includes basic non-identifiable primary keys of patient data, including:

- Donor ID, Specimen ID, and Sample ID
- Basic cancer sample descriptors

The base schema can be seen in the code block shown below: 

```json
{
  "studyId": "EXAMPLE",
  "analysisType": {
    "name": "sequencing_experiment"
  },
  "samples": [
    {
      "submitterSampleId": "exammple-sample-id",
      "matchedNormalSubmitterSampleId": null,
      "sampleType": "Amplified DNA",
      "specimen": {
        "submitterSpecimenId": "exammple-specimen-id",
        "specimenType": "Normal",
        "tumourNormalDesignation": "Normal",
        "specimenTissueSource": "Blood derived"
      },
      "donor": {
        "submitterDonorId": "exammple-donor-id",
        "gender": "Male"
      }
    }
  ]
}
```

The base schema and the allowed values for all fields are defined by the Song **base meta-schema**, which is referenced below.

<details>
  <summary>Song base schema as JSON Schema</summary>
  

```json
{
    "name": "variant_calling_test",
    "version": 1,
    "createdAt": "2021-03-04T23:22:42.025146",
    "schema": {
        "$schema": "http://json-schema.org/draft-07/schema#",
        "id": "analysisPayload",
        "type": "object",
        "definitions": {
            "common": {
                "md5": {
                    "type": "string",
                    "pattern": "^[a-fA-F0-9]{32}$"
                },
                "submitterId": {
                    "type": "string",
                    "pattern": "^[A-Za-z0-9\\-\\._]{1,64}$"
                },
                "info": {
                    "type": "object"
                }
            },
            "file": {
                "fileType": {
                    "type": "string",
                    "enum": [
                        "FASTA",
                        "FAI",
                        "FASTQ",
                        "BAM",
                        "BAI",
                        "VCF",
                        "TBI",
                        "IDX",
                        "XML",
                        "TGZ",
                        "CRAM",
                        "CRAI"
                    ]
                },
                "fileData": {
                    "type": "object",
                    "required": [
                        "dataType",
                        "fileName",
                        "fileSize",
                        "fileType",
                        "fileAccess",
                        "fileMd5sum"
                    ],
                    "properties": {
                        "dataType": {
                            "type": "string"
                        },
                        "fileName": {
                            "type": "string",
                            "pattern": "^[A-Za-z0-9_\\.\\-\\[\\]\\(\\)]+$"
                        },
                        "fileSize": {
                            "type": "integer",
                            "min": 0
                        },
                        "fileAccess": {
                            "type": "string",
                            "enum": [
                                "open",
                                "controlled"
                            ]
                        },
                        "fileType": {
                            "$ref": "#/definitions/file/fileType"
                        },
                        "fileMd5sum": {
                            "$ref": "#/definitions/common/md5"
                        },
                        "info": {
                            "$ref": "#/definitions/common/info"
                        }
                    }
                }
            },
            "donor": {
                "gender": {
                    "type": "string",
                    "enum": [
                        "Male",
                        "Female",
                        "Other"
                    ]
                },
                "donorData": {
                    "type": "object",
                    "required": [
                        "submitterDonorId",
                        "gender"
                    ],
                    "properties": {
                        "submitterDonorId": {
                            "$ref": "#/definitions/common/submitterId"
                        },
                        "gender": {
                            "$ref": "#/definitions/donor/gender"
                        },
                        "info": {
                            "$ref": "#/definitions/common/info"
                        }
                    }
                }
            },
            "specimen": {
                "specimenTissueSource": {
                    "type": "string",
                    "enum": [
                        "Blood derived",
                        "Blood derived - bone marrow",
                        "Blood derived - peripheral blood",
                        "Bone marrow",
                        "Buccal cell",
                        "Lymph node",
                        "Solid tissue",
                        "Plasma",
                        "Serum",
                        "Urine",
                        "Cerebrospinal fluid",
                        "Sputum",
                        "Other",
                        "Pleural effusion",
                        "Mononuclear cells from bone marrow",
                        "Saliva",
                        "Skin",
                        "Intestine",
                        "Buffy coat",
                        "Stomach",
                        "Esophagus",
                        "Tonsil",
                        "Spleen",
                        "Bone",
                        "Cerebellum",
                        "Endometrium"
                    ]
                },
                "specimenType": {
                    "type": "string",
                    "enum": [
                        "Normal",
                        "Normal - tissue adjacent to primary tumour",
                        "Primary tumour",
                        "Primary tumour - adjacent to normal",
                        "Primary tumour - additional new primary",
                        "Recurrent tumour",
                        "Metastatic tumour",
                        "Metastatic tumour - metastasis local to lymph node",
                        "Metastatic tumour - metastasis to distant location",
                        "Metastatic tumour - additional metastatic",
                        "Xenograft - derived from primary tumour",
                        "Xenograft - derived from tumour cell line",
                        "Cell line - derived from xenograft tumour",
                        "Cell line - derived from tumour",
                        "Cell line - derived from normal"
                    ]
                },
                "tumourNormalDesignation": {
                    "type": "string",
                    "enum": [
                        "Normal",
                        "Tumour"
                    ]
                },
                "specimenData": {
                    "type": "object",
                    "required": [
                        "submitterSpecimenId",
                        "specimenTissueSource",
                        "tumourNormalDesignation",
                        "specimenType"
                    ],
                    "properties": {
                        "submitterSpecimenId": {
                            "$ref": "#/definitions/common/submitterId"
                        },
                        "specimenTissueSource": {
                            "$ref": "#/definitions/specimen/specimenTissueSource"
                        },
                        "tumourNormalDesignation": {
                            "$ref": "#/definitions/specimen/tumourNormalDesignation"
                        },
                        "specimenType": {
                            "$ref": "#/definitions/specimen/specimenType"
                        },
                        "specimenClass": {
                            "not": {}
                        },
                        "info": {
                            "$ref": "#/definitions/common/info"
                        }
                    }
                }
            },
            "analysisType": {
                "type": "object",
                "required": [
                    "name"
                ],
                "properties": {
                    "name": {
                        "type": "string"
                    },
                    "version": {
                        "type": [
                            "integer",
                            "null"
                        ]
                    }
                }
            },
            "sample": {
                "sampleTypes": {
                    "type": "string",
                    "enum": [
                        "Total DNA",
                        "Amplified DNA",
                        "ctDNA",
                        "Other DNA enrichments",
                        "Total RNA",
                        "Ribo-Zero RNA",
                        "polyA+ RNA",
                        "Other RNA fractions"
                    ]
                },
                "sampleData": {
                    "type": "object",
                    "required": [
                        "submitterSampleId",
                        "sampleType"
                    ],
                    "properties": {
                        "submitterSampleId": {
                            "$ref": "#/definitions/common/submitterId"
                        },
                        "sampleType": {
                            "$ref": "#/definitions/sample/sampleTypes"
                        },
                        "info": {
                            "$ref": "#/definitions/common/info"
                        }
                    }
                }
            }
        },
        "required": [
            "studyId",
            "analysisType",
            "samples",
            "files",
            "experiment"
        ],
        "properties": {
            "analysisId": {
                "not": {}
            },
            "studyId": {
                "type": "string",
                "minLength": 1
            },
            "analysisType": {
                "allOf": [
                    {
                        "$ref": "#/definitions/analysisType"
                    }
                ]
            },
            "samples": {
                "type": "array",
                "minItems": 1,
                "items": {
                    "type": "object",
                    "allOf": [
                        {
                            "$ref": "#/definitions/sample/sampleData"
                        }
                    ],
                    "required": [
                        "specimen",
                        "donor"
                    ],
                    "properties": {
                        "specimen": {
                            "$ref": "#/definitions/specimen/specimenData"
                        },
                        "donor": {
                            "$ref": "#/definitions/donor/donorData"
                        }
                    },
                    "if": {
                        "properties": {
                            "specimen": {
                                "properties": {
                                    "tumourNormalDesignation": {
                                        "const": "Tumour"
                                    }
                                }
                            }
                        }
                    },
                    "then": {
                        "properties": {
                            "matchedNormalSubmitterSampleId": {
                                "$ref": "#/definitions/common/submitterId"
                            }
                        },
                        "required": [
                            "matchedNormalSubmitterSampleId"
                        ]
                    },
                    "else": {
                        "properties": {
                            "matchedNormalSubmitterSampleId": {
                                "const": null
                            }
                        },
                        "required": [
                            "matchedNormalSubmitterSampleId"
                        ]
                    }
                }
            },
            "files": {
                "type": "array",
                "minItems": 1,
                "items": {
                    "$ref": "#/definitions/file/fileData"
                }
            }
        }
    }
}

```

</details>

## Making Schemas

The basic portion of a dynamic schema requires at a minimum:

- an `analysis_type`
- an `experiment` object

```json

This template can be used to start your dynamic schema:
{
 "name": "variant_calling_example",
 "schema":{
	 "type": "object",
	 "required":[
		 "experiment"
	 ],
	 "properties":{
		"experiment":{}
	 }
 }
}
```

Here are some resources to help with the creation of new schemas for your projects:

- <a href="https://json-schema.org/understanding-json-schema" target="_blank" rel="noopener noreferrer">Understanding JSON Schema guide</a>: This guide provides detailed information on JSON Schema formatting, offering a comprehensive resource for understanding and working with JSON schemas.

- <a href="https://raw.githubusercontent.com/cancogen-virus-seq/metadata-schemas/main/schemas/consensus_sequence.json" target="_blank" rel="noopener noreferrer">Example schema</a>: If you're looking for a sample schema, you can refer to this example schema used for the CanCOGeN's VirusSeq Portal. It can serve as a reference or starting point for creating your own schemas.

- <a href="https://github.com/overture-stack/SONG/blob/develop/song-server/src/main/resources/schemas/analysis/analysisBase.json" target="_blank" rel="noopener noreferrer">Base schema reference</a>: Song utilizes a base schema that is combined with all user schemas. When creating your schemas, it's important to reference the base schema to avoid specifying conflicting properties and ensure compatibility with Song's schema structure.

These resources aim to provide guidance and references for schema creation, ensuring the consistency and compatibility of your schemas within the Song metadata framework.

**Tip:** There's no need to write your own JSON Schema by hand. There are many existing libraries to help you format your data.  For basic schemas, a good resource is https://jsonschema.net or https://www.liquid-technologies.com/online-json-to-schema-converter, where you can convert JSON to JSON Schema.

## Registering new Schemas

The same steps will apply when updating a previous schema.

### Using the Swagger UI

1. **Locate your endpoint of interest**

From the schema dropdown, find the `POST` **RegisterAnalysisType** endpoint.

![Entity](../assets/swagger_register_schema(s).png 'register new schema')

2. **Select *Try it out* and input your desired values** 

Enter your authorization token in the authorization field and your new schema inside the request field.

3. **Select Execute** 

Expected responses as well as response codes and descriptions, are conviently documented within Swagger-UI. 

**Verifying Schemas:** To verify your schema has successfully been added, you can use the `GET` **ListAnalysisTypes** endpoint found under the Schema dropdown. If updating a pre-existing schema, use the `GET` **GetAnalysisTypeVersion** endpoint.


### Using a Curl command

The following curl command makes a POST request with the required authorization tokens, headers and data:

```bash
curl -X POST "https://song.virusseq-dataportal.ca/schemas" -H "accept: */*" -H "Authorization: AUTHORIZATION" -H "Content-Type: application/json" -d "{ \"name
\":\"example_demo\", \"schema\": { \"type\":\"object\", \"required\":[ \"experiment\" ], \"properties\":{ \"experiment\": { \"type\": \"object\", \"required\": [\"experiment_type\"], \"propertyNames\": { \"experiment_type\":{ \"type\":\"string\", \"enum\": [\"WGS\",\"RNA-Seq\"] }, } } } }}"
```

### Using Python

The following script imports the necessary libraries and sends a POST request to a specified URL endpoint with a new schema. It includes authorization using a JWT or API token. If the request fails (status code is not 200), it prints an error message.

```python
import json
import requests
### Verify your SONG URL either through the swagger portal or hosting terminal
url="https://song.virusseq-dataportal.ca"
### Set endpoint
endpoint="%s/schemas" % (url)
### Supply authorized JWT or API token
api_token="AUTHORIZATION"
### Format contents
headers={"accept":"*/*",
         "Authorization":"Bearer %s" % (api_token),
         "Content-Type": "application/json"
        }
### Supply schema as a json either by reading a local file or through a request
payload = new_schema
### Run POST
post_response=requests.post(endpoint, json=payload,headers=headers)
### Complain only when an attempt fails
if post_response.status_code!=200:
print("error calling patch endpoint",endpoint,post_response.status_code)
```

# Managing Schemas

## Versioning

Newly registered schemas that represent a new `analysis_type` are assigned as `Version 1` by default. All future schemas registered under the same `analysis_type` will be auto-incremented during registration.

## Listing Schemas

To retrieve a list of all schemas registered in Song, you can use the `ListAnalysisTypes` endpoint. The following parameters help manage schemas:

- **hideSchema**: When set to `true`, the schemas will not be returned in the list.
- **unrenderedOnly**: Can be set to `true` or `false`. If `hideSchema` is set to `false`, the schema will be returned. This parameter controls whether the Song base schema is included in the request. For users updating dynamic schemas, it is helpful to set this to `true` so they can focus on editing the dynamic portion for easier future schema registration.

Example: Basic listing of all schemas.

```bash
curl --location --request GET 'https://song-url.example.com/schemas?hideSchema=true&limit=50&offset=0&unrenderedOnly=true' \
--header 'Authorization: Bearer 02ad07ea-2982-43b4-8aa3-1d64689050f0'
```

Example: List of all schemas, with only the dynamic portion rendered:

```bash
curl --location --request GET 'https://song-url.example.com/schemas?hideSchema=false&limit=50&offset=0&unrenderedOnly=true' \
--header 'Authorization: Bearer 02ad07ea-2982-43b4-8aa3-1d64689050f0'
```

## Get Schemas

You can also request individual schemas using the `GetAnalysisTypeVersion` endpoint. The following parameters are useful for management needs:

- **version**: If provided, a specific schema version is returned.  Otherwise, all versions of an `analysis_type` schema are returned. 
- **unrenderedOnly**: Can be set to `true` or `false`. This parameter controls whether the Song base schema is included in the request. For users updating dynamic schemas, it is helpful to set this to `true` so they can focus on editing the dynamic portion for easier future schema registration.

```bash
curl --location --request GET 'https://song-url.example.com/schemas/sequencing_experiment?unrenderedOnly=true' \
--header 'Authorization: Bearer 02ad07ea-2982-43b4-8aa3-1d64689050f0'
```
