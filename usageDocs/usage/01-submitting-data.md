# Data Submission

Submitted data consists of data files (e.g. sequencing reads or VCFs), as well as any associated file metadata (data that describes your data).  When metadata and the data files are combined, it is called a Song `Analysis`.  An analysis is a trackable unit of data that keeps metadata and a file associated together and is the main entity in a Song database. 

Analysis types are described by Song administrators, who can model the data inside an analysis type by creating <a href="/documentation/song/user-guide/schema" target="_blank" rel="noopener noreferrer">Dynamic Schemas</a>.  An analysis type can contain any information that needs to be recorded about a file type, defined in `JSON` format. 

Song users mainly interact with Song by submitting data against an established analysis type schema or by downloading files associated with an analysis in a bundle (e.g multiple files that are bundled together in one analysis).  

## Data Submission Workflow

Submitted data consists of data files (e.g. sequencing reads or VCFs), as well as any associated file metadata (data that describes the data file). Data is submitted to Song & Score using the Song and Score CLIs (Command Line Clients). The Song and Score clients are used in conjunction to upload raw data files while maintaining file metadata and provenance, which is tracked through Song metadata analysis objects.

### Installing the Song-Client

**Running the song-client docker image**

You must supply environment variables for the `CLIENT_STUDY_ID`, the `CLIENT_SERVER_URL` and your `CLIENT_ACCESS_TOKEN`. The access token is supplied from Ego or your profile page within Stage.

```bash
docker run -d -it --name song-client \
-e CLIENT_ACCESS_TOKEN=${token} \
-e CLIENT_STUDY_ID=ABC123 \
-e CLIENT_SERVER_URL=https://<INSERT-URL> \
--network="host" \
--mount type=bind,source="$(pwd)",target=/output \
ghcr.io/overture-stack/song-client:latest
```

### Installing the Score-Client

**Running the score-client docker image**

You will be required to supply environment variables for the `STORAGE_URL`, the `METADATA_URL` and your `CLIENT_ACCESS_TOKEN`.

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

### Step 1. Prepare a payload

First, a metadata payload must be prepared. The payload must conform to an `analysis_type` registered as a schema. For help with creating or updating schemas please see the <a href="/documentation/song/admin/schemas/" target="_blank" rel="noopener noreferrer">Dynamic Schemas documentation</a>.

### Step 2. Upload the metadata payload file

Once you have formatted the payload correctly, use the song-client `submit` command to upload the payload.

```bash
docker exec song-client sh -c "sing submit -f /output/example-payload.json"
```

If your payload is not formatted correctly, you will receive an error message detailing what is wrong. Please fix any errors and resubmit. If your payload is formatted correctly, you will get an `analysisId` in response:

```json
{
  "analysisId": "a4142a01-1274-45b4-942a-01127465b422",
  "status": "OK"
}
```

At this point, since the payload data has successfully been submitted and accepted by Song, it is now referred to as an analysis. By default, all newly created analyses are set to an `UNPUBLISHED` state.

For more information on analysis states (published, unpublished and suppressed) see our page on [analysis management](./05-analysis-management.md)

### Step 3. Generate a manifest file

Use the returned `analysis_id` to generate a manifest for file upload. This manifest will used by the score-client in the next step.

The manifest establishes a link between the analysis-id that has been submitted and the data file on your local systems that is being uploaded.

Using the song-client `manifest` command, define

- The analysis id using the `-a` parameter
- The location of your input files with the `-d` parameter
- The output file path for the manifest file with the `-f` parameter. **Note**: this is a _file path_ not a directory path

Here is an example of a manifest command:

```bash
docker exec song-client sh -c "sing manifest -a a4142a01-1274-45b4-942a-01127465b422 -f /some/output/dir/manifest.txt  -d /submitting/file/directory"
```

Here is the expected response:

```bash
Wrote manifest file 'manifest.txt' for analysisId 'a4142a01-1274-45b4-942a-01127465b422'
```

The `manifest.txt` file will be written out to a defined output file path. If the output directory does not exist, it will be automatically created.

### Step 4. Upload your data files to cloud storage

Upload all the files associated with the analysis using the score-client `upload` command:

```bash
docker exec score-client sh -c "score-client  upload --manifest manifest.txt"
```

Once the file(s) successfully upload, you will receive an `Upload completed` message.

#### Troubleshooting Upload

- If you receive a connection or internal server error message, have your admin check that Song and Score are configured to talk to each other correctly.

Sometimes, if an upload is stuck, you can reinitiate the upload using the `--force` command.

```bash
docker exec score-client sh -c "score-client  upload --manifest manifest.txt --force "
```

For more information on Score, please see the <a href="/documentation/score" target="_blank" rel="noopener noreferrer">Score documentation page</a>.

### Step 5. Publish the analysis

The final step to submitting molecular data is to set the state of an analysis to `PUBLISHED`. A published analysis signals to the data administrators that this data is ready to be processed by downstream services.

```bash
docker exec song-client sh -c "sing publish -a a4142a01-1274-45b4-942a-01127465b422"
```

Here is the expected response:

```bash
AnalysisId a4142a01-1274-45b4-942a-01127465b422 successfully published
```

A published analysis will now be searchable in Song. In the next section, we will outline how to search for data in Song.

**Integration Tips:** Song is a relational database designed for secure and consistent storage of data. For an optimal data query experience, use Song with a search platform. The Overture components Maestro and Arranger can be used to index and view data from Song from an intuitive search portal linked to a graphQL API.
