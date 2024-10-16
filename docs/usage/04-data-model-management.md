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

1. **Base Schema**: A minimal set of essential fields required for all analyses, including:
   - Basic patient data
   - Submitter IDs
   - File details

2. **Dynamic schema**: A flexible component that Song administrators can configure and upload to define specific analysis types.

This two-part schema structure ensures:
- Consistent core information across all analyses
- Flexibility to accommodate various data structures
- Accurate and thorough metadata validation 

### Base Schema

The **base schema** defines the minimal data set required for a schema. It includes non-identifiable primary keys and basic descriptors for patient and cancer sample data:

- Identifiers: Donor ID, Specimen ID, and Sample ID
- Essential cancer sample characteristics

You can view the current base schema in the [Song repository](https://github.com/overture-stack/SONG/blob/develop/song-server/src/main/resources/schemas/analysis/analysisBase.json).

:::info Future Updates to our Submission System
As part of our work on the [Pan-Canadian Genome Library](https://oicr.on.ca/first-ever-national-library-of-genomic-data-will-help-personalize-cancer-treatment-in-canada-and-around-the-world/), we are improving our [**data submission system**](/docs/under-development/). This system will better support tabular (clinical) data and reduce the constraints of Song's base schema, ultimately enhancing the flexibility and robustness of our data management and storage system. For more information [**see our under development section**](/docs/under-development/).
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
    For a detailed guide on building JSON Schemas for Song see our [**administration guide on updating data models**](/guides/administration-guides/updating-the-data-model)
    :::

## Registering Analysis Types

These steps apply both for registering new schemas and updating existing ones.

### Using the Swagger UI

1. **Locate the Endpoint**
   - From the schema dropdown, find the `POST` **RegisterAnalysisType** endpoint.

     ![Register new schema](../assets/swagger_register_schema(s).png 'Register new schema')

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
curl -X POST "https://song.virusseq-dataportal.ca/schemas" \
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
url = "https://song.virusseq-dataportal.ca"

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
For technical support or specific use cases, please don't hesitate to reach out through our relevant [**community support channels**](/community/support).
:::
