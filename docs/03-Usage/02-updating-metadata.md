# Updating Metadata

Updating metadata for analyses in Songs database using various methods.

## Using Swagger UI

Update an individual analysis through the `PATCH` **PatchUpdateAnalysis** endpoint in the Analysis dropdown on the Swagger UI.

![Patch Endpoint in Swagger UI](../assets/swagger_patch.png 'Patch Endpoint')

Required inputs for the **PatchUpdateAnalysis** endpoint:
- Authorization token
- Analysis ID
- Updated Analysis content
- Study ID

## Programmatic Updates

The following examples demonstrate how to update analyses using the Song API programmatically. 

:::info Support
For technical support or specific use cases, please don't hesitate to reach out through our relevant [**community support channels**](/community/support).
:::

### Updating a Single Analysis (Example)

This Python script is made to update a specific analysis in a given study:

```python
import requests

# Define variables
analysis_id = "ANALYSIS-ABC123-SAMPLEA"
url = "https://song.virusseq-dataportal.ca"
study = "ABC123"
api_token = "YOUR_API_TOKEN"

# PATCH endpoint
endpoint = f"{url}/studies/{study}/analysis/{analysis_id}"
headers = {
    "accept": "*/*",
    "Authorization": f"Bearer {api_token}",
    "Content-Type": "application/json"
}
payload = {'experiment': {'sequencing_instrument': 'new_value'}}

patch_response = requests.patch(endpoint, json=payload, headers=headers)
if patch_response.status_code != 200:
    print(f"Error calling patch endpoint: {endpoint}, Status code: {patch_response.status_code}")
```

### Updating Multiple Analyses (Example)

This script updates multiple analyses associated with specific samples within a study:

```python
import requests

# Define variables
samples_to_mod = ["SAMPLE_A", "SAMPLE_B", "SAMPLE_C", "SAMPLE_D", "SAMPLE_E", "SAMPLE_F"]
url = "https://song.virusseq-dataportal.ca"
study = "ABC123"
api_token = "YOUR_API_TOKEN"

for sample in samples_to_mod:
    # Retrieve analyses associated with samples
    endpoint = f"{url}/studies/{study}/analysis/search/id?submitterDonorId={sample}"
    headers = {"accept": "*/*"}

    get_response = requests.get(endpoint, headers=headers)
    
    if get_response.status_code != 200:
        print(f"Error: {endpoint}")
        break

    published_analyses = [analysis for analysis in get_response.json() if analysis['analysisState'] == 'PUBLISHED']
    
    if len(published_analyses) == 0:
        print(f"No published analysis detected: {endpoint}")
        continue
    if len(published_analyses) > 1:
        print(f"Multiple analyses detected: {endpoint}")
        continue
    
    analysis = published_analyses[0]
    analysis_id = analysis['analysisId']
    old_value = analysis['experiment']['sequencing_instrument']
    new_value = "Illumina MiSeq"
    
    # PATCH endpoint
    patch_endpoint = f"{url}/studies/{study}/analysis/{analysis_id}"
    patch_headers = {
        "accept": "*/*",
        "Authorization": f"Bearer {api_token}",
        "Content-Type": "application/json"
    }
    payload = {'experiment': {'sequencing_instrument': new_value}}

    patch_response = requests.patch(patch_endpoint, json=payload, headers=patch_headers)
    if patch_response.status_code != 200:
        print(f"Error calling patch endpoint: {patch_endpoint}, Status code: {patch_response.status_code}")
        break
```

## Important Note on Song-assigned IDs

Song-assigned IDs (donor, sample, specimen, analysis, and object IDs) and those specified in the [base schema](https://github.com/overture-stack/SONG/blob/develop/song-server/src/main/resources/schemas/analysis/analysisBase.json) are immutable and cannot be altered. If you need to change any of these values, it is recommended to UNPUBLISH and SUPPRESS the analysis, then resubmit it with the new information.