# Data Model Management

Creating & updating the data model using JSON schema.

## Song Schema

Song uses [JSON Schema](https://json-schema.org/) to define the desired structure of metadata stored in its database. This approach offers several advantages:

1. **Rigorous Validation**: Submitted data undergoes validation against a predefined data model schema (analysis type).

2. **Data Integrity**: The schema-based validation process ensures:
   - All required fields are present
   - Field contents adhere to specified syntax and data types
   - Values fall within allowed ranges or sets

3. **Quality Assurance**: ultimately, this structured approach preserves the integrity and quality of metadata within Song.

## Analysis Types

In Song, metadata is organized and submitted as **analyses**. An analysis represents a collection of one or more files along with a comprehensive metadata record describing those files.

When submitting an analysis to Song, you must specify an 'analysis type' in your submission. This type determines the data model used for validation and is defined in your analysis file using the `analysis type` field.

The schema for each analysis type consists of two components:

1. **Base Schema**: A minimal set of essential fields required for all analyses:
   - `studyId`: the study the analysis belongs to
   - `analysisType`: the analysis type used to validate the submission
   - `files`: the file(s) the analysis describes

2. **Dynamic schema**: A flexible component that Song administrators can configure and upload to define specific analysis types.

This two-part schema structure ensures:
- Consistent core information across all analyses
- Flexibility to accommodate various data structures
- Accurate and thorough metadata validation 

### Base Schema

The **base schema** defines the minimal data set required for every analysis. It requires only three top-level fields:

- `studyId`: identifies the study the analysis belongs to
- `analysisType`: the name (and optional version) of the analysis type used for validation
- `files`: an array describing at least one file, including its data type, name, size, access level, type, and MD5 checksum

You can view the current base schema in the [Song repository](https://github.com/overture-stack/SONG/blob/develop/song-server/src/main/resources/schemas/analysis/analysisBase.json).

:::note Base schema change in Song 5.3.0
Prior to Song 5.3.0, the base schema also required donor, specimen, and sample entities, which were stored across multiple related tables. As of 5.3.0 these are no longer required, and all analysis data is stored in a single consolidated table. Existing deployments must migrate their data; see [**Database Migration**](./11-database-migration.md).
:::

:::info Future Updates to our Submission System
As part of our work on the [Pan-Canadian Genome Library](https://oicr.on.ca/first-ever-national-library-of-genomic-data-will-help-personalize-cancer-treatment-in-canada-and-around-the-world/), we are improving our [**data submission system**](https://docs.overture.bio/develop/Lyric/overview). This system will better support tabular (clinical) data and reduce the constraints of Song's base schema, ultimately enhancing the flexibility and robustness of our data management and storage system. For more information [**see the Lyric documentation**](https://docs.overture.bio/develop/Lyric/overview).
:::

### Dynamic schema

The basic portion of a dynamic schema requires at a minimum:

- a defined `analysis_type`
- an `experiment` object

    ```json
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

    :::info Building JSON Schemas
    For a detailed guide on building JSON Schemas for Song see our [**administration guide on building Song schemas**](https://docs.overture.bio/use/administration/building-song-schemas)
    :::

## Schema Options

The `options` property defines extra validations for an analysis schema, such as restrictions on file types and checks against an external service. The `options` property is not required, and each property within it is also optional. If no value is provided for an `options` property, a default configuration is used for the analysis. When updating an existing analysis type, you can omit any option and its value is maintained from the previous version.

```json
{
  "options": {
    "fileTypes": ["bam", "cram"],
    "externalValidations": [
      {
        "url": "http://localhost:8099/",
        "jsonPath": "experiment.someId"
      }
    ]
  }
}
```

To remove the previous value of an option so that its validation is no longer required, for instance removing the restriction on file types so that any file type is allowed, provide an empty list for that option. In the example below, both `fileTypes` and `externalValidations` are set to empty arrays, so these validations are not applied to submitted analyses:

```json
{
  "options": {
    "fileTypes": [],
    "externalValidations": []
  }
}
```

### File Types

`options.fileTypes` accepts an array of strings representing the file types (file extensions) allowed for this type of analysis.

If an empty array is provided, any file type is allowed. If an array of file types is provided, an analysis is invalid if it contains files of a type not listed.

```json
{
  "options": {
    "fileTypes": ["bam", "cram"]
  }
}
```

### External Validation

External validations configure Song to check a value in the analysis against an external service by sending an HTTP GET request to a configurable URL. The service should respond with a `2XX` status to indicate the value is valid.

For example, if a project's clinical data is managed in a separate service, you can add an external validation on the donor ID field of your custom schema. This sends the donor ID to the external service, which can confirm that the donor was previously registered:

```json
{
  "url": "http://example.com/{study}/donor/{value}",
  "jsonPath": "experiment.donorId"
}
```

The URL is a template with two variables that are replaced during validation. Song replaces the `{value}` token with the value read from the analysis at the property defined by `jsonPath`, and replaces the `{study}` token with the study ID for the analysis.

Continuing the example above, if the following analysis was submitted:

```json
{
  "studyId": "ABC123",
  "analysisType": {
    "name": "minimalExample"
  },
  "files": [
    {
      "dataType": "text",
      "fileName": "file1.txt",
      "fileSize": 123,
      "fileType": "txt",
      "fileAccess": "open",
      "fileMd5sum": "595f44fec1e92a71d3e9e77456ba80d1"
    }
  ],
  "experiment": {
    "donorId": "id01"
  }
}
```

Song would validate the `donorId` by sending a request to `http://example.com/ABC123/donor/id01`.

The URL parsing allows using either the `{study}` or `{value}` placeholder multiple times (for example, `http://example.com/{study}-{value}/{value}`); each instance is interpolated accordingly.

:::warning
The URL may cause errors in Song if it contains any tokens matching the `{word}` format other than `{study}` and `{value}`.
:::

#### Accessing values within arrays

The `jsonPath` can address values inside arrays using standard bracket notation.

To access a value at a specific index, include the index in brackets:

```json
{
	"url": "http://example.com/{study}/donor/{value}",
	"jsonPath": "donors[0].donorId"
}
```

This extracts `donorId` from the first element of the `donors` array and performs one validation call.

To validate every element in an array, use the `[*]` wildcard:

```json
{
	"url": "http://example.com/{study}/donor/{value}",
	"jsonPath": "donors[*].donorId"
}
```

This extracts `donorId` from each element of the `donors` array and performs one validation call per value. If any value fails validation, the entire analysis submission is rejected. If the array is empty, no validation calls are made and the submission proceeds.

Only string values are validated. If the property at the resolved path is not a string, it is silently skipped.

## Registering Analysis Types

These steps apply both for registering new schemas and updating existing ones.

### Using the Swagger UI

1. **Locate the Endpoint**
   - From the schema dropdown, find the `POST` **RegisterAnalysisType** endpoint.

     ![Register new schema](../assets/swagger_register_schemas.png 'Register new schema')

2. **Input Your Data**
   - Click *Try it out* & enter your authorization token in the authorization field
        - Format: Bearer APIkey (replace APIkey with your actual API key)
   - Input your new schema in the request field

3. **Execute the Request**
   - Click *Execute*, expected responses, response codes, and descriptions are conveniently documented within Swagger-UI

**Verifying Schemas:** 
- To confirm your schema has been added, use the `GET` **ListAnalysisTypes** endpoint in the Schema dropdown
- If updating a previously existing schemas, use the `GET` **GetAnalysisTypeVersion** endpoint

### Using a Curl Command

Use the following curl command to make a POST request with the required authorization tokens, headers, and data:

```bash
curl -X POST "https://<YOUR-SONG-URL>/schemas" \
    -H "accept: */*" \
    -H "Authorization: AUTHORIZATION" \
    -H "Content-Type: application/json" \
    -d '{ 
        "name": "example_demo", 
        "schema": { 
            "type": "object", 
            "required": ["experiment"], 
            "properties": {
                "experiment": { 
                    "type": "object", 
                    "required": ["experiment_type"], 
                    "properties": {
                        "experiment_type": {
                            "type": "string", 
                            "enum": ["WGS", "RNA-Seq"]
                        }
                    }
                }
            }
        }
    }'
```

### Using Python

This Python script sends a POST request to register a new schema:

```python
import requests

# Verify your SONG URL either through the swagger portal or hosting terminal
url = "https://<YOUR-SONG-URL>"

# Set endpoint
endpoint = f"{url}/schemas"

# Supply authorized JWT or API token
api_token = "AUTHORIZATION"

# Format headers
headers = {
    "accept": "*/*",
    "Authorization": f"Bearer {api_token}",
    "Content-Type": "application/json"
}

# Supply schema as a JSON (either by reading a local file or through a request)
payload = new_schema

# Send POST request
response = requests.post(endpoint, json=payload, headers=headers)

# Check for errors
if response.status_code != 200:
    print(f"Error calling POST endpoint {endpoint}: {response.status_code}")
    print(f"Response: {response.text}")
else:
    print("Schema registered successfully")
```

## Dynamic schema Management

### Versioning of Schemas

- New schemas representing a new `analysis_type` are automatically assigned `Version 1`.
- Subsequent schemas registered under the same `analysis_type` will have their version numbers auto-incremented.

### Listing Schemas

To retrieve a list of all schemas registered in Song, use the `ListAnalysisTypes` endpoint. Key parameters:

- **hideSchema**: 
  - `true`: Schemas are not returned in the list.
  - `false`: Schemas are included in the list.
- **unrenderedOnly**: 
  - Controls inclusion of the Song base schema.
  - Useful for users updating dynamic schemas.
  - Set to `true` to focus on editing the dynamic (admin inputed) portion for easier future schema registration.

Example: Basic listing of all schemas

```bash
curl --location --request GET 'https://song-url.example.com/schemas?hideSchema=true&limit=50&offset=0&unrenderedOnly=true' \
--header 'Authorization: Bearer YOUR_API_KEY'
```

Example: List all schemas, showing only the dynamic (admin inputed) portion

```bash
curl --location --request GET 'https://song-url.example.com/schemas?hideSchema=false&limit=50&offset=0&unrenderedOnly=true' \
--header 'Authorization: Bearer YOUR_API_KEY'
```

### Retrieving Individual Schemas

Use the `GetAnalysisTypeVersion` endpoint to request specific schemas. Key parameters:

- **version**: 
  - If provided, returns a specific schema version.
  - If omitted, returns all versions of an `analysis_type` schema.
- **unrenderedOnly**: 
  - Controls inclusion of the Song base schema.
  - Set to `true` to focus on the dynamic (admin inputed) portion for easier editing and future registration.

Example: Retrieve a specific schema's dynamic (admin inputed) portion

```bash
curl --location --request GET 'https://song-url.example.com/schemas/sequencing_experiment?unrenderedOnly=true' \
--header 'Authorization: Bearer YOUR_API_KEY'
```

:::info Support
For technical support or specific use cases, please don't hesitate to reach out through our [**support page**](https://docs.overture.bio/community/support) or our [**discussion forum**](https://github.com/overture-stack/docs/discussions?discussions_q=).
:::
