# Metadata Submission

Submitting new metadata entries (Analyses) to Song.

## Overview

A Song `Analysis` consists of:
- Data files (e.g., sequencing reads or VCFs)
- Associated file metadata

Analysis types are defined by Song administrators using [Dynamic Schemas](../documentation/song/user-guide/schema).

## Data Submission Workflow

### Prerequisites

1. Install Song-Client:

   ```bash
   docker run -d -it --name song-client \
   -e CLIENT_ACCESS_TOKEN=${token} \
   -e CLIENT_STUDY_ID=ABC123 \
   -e CLIENT_SERVER_URL=https://<INSERT-URL> \
   --network="host" \
   --mount type=bind,source="$(pwd)",target=/output \
   ghcr.io/overture-stack/song-client:latest
   ```

2. Install Score-Client:

   ```bash
   docker run -d -it \
   --name score-client \
   -e CLIENT_ACCESS_TOKEN=${token} \
   -e STORAGE_URL=http://<INSERT-URL> \
   -e METADATA_URL=http://<INSERT-URL> \
   --network="host" \
   --mount type=bind,source="$(pwd)",target=/output \
   ghcr.io/overture-stack/score:latest
   ```

### Step 1: Prepare a Payload

Create a metadata payload conforming to a registered `analysis_type` schema. For schema help, see the [Dynamic Schemas documentation](../documentation/song/admin/schemas/).

### Step 2: Upload the Metadata Payload

Use the song-client `submit` command:

```bash
docker exec song-client sh -c "sing submit -f /output/example-payload.json"
```

Successful submission returns an `analysisId`:

```json
{
  "analysisId": "a4142a01-1274-45b4-942a-01127465b422",
  "status": "OK"
}
```

### Step 3: Generate a Manifest File

Use the `analysisId` to create a manifest for file upload:

```bash
docker exec song-client sh -c "sing manifest -a a4142a01-1274-45b4-942a-01127465b422 -f /some/output/dir/manifest.txt -d /submitting/file/directory"
```

### Step 4: Upload Data Files

Use the score-client `upload` command:

```bash
docker exec score-client sh -c "score-client upload --manifest manifest.txt"
```

For upload issues, try the `--force` option:

```bash
docker exec score-client sh -c "score-client upload --manifest manifest.txt --force"
```

For more on Score, see the [Score documentation page](../documentation/score).

### Step 5: Publish the Analysis

Set the analysis state to `PUBLISHED`:

```bash
docker exec song-client sh -c "sing publish -a a4142a01-1274-45b4-942a-01127465b422"
```

:::info Search & Exploration
For optimal data querying, use Song with our search & exploration services Maestro, Arranger and Stage. With these components you can index and query and explore Song data through an intuitive search portal.
:::

## Troubleshooting

- If you encounter connection or internal server errors, have your admin verify that the Song and Score servers are correctly configured.
 
:::info Support
For technical support, please don't hesitate to reach out through our relevant [**community support channels**](/community/support).
:::