# Command Reference

The following is provided as a reference to all the commands and command options currently supported by the Song client.

## Config

The `config` command shows the current configuraion settings.

## Submit

The `submit` command is used to submit a payload to create an analysis. For more information, see our <a href="/documentation/song/user/submit/" target="_blank" rel="noopener noreferrer">documentation on submitting data with Song</a>.

## Ping

The `ping` command can ping (test) the Song server.

## Get-Analysis-Type

The `get-analysis-types` command is used to retrieve specific analysis type schema information based on the given parameters:

| Option | Description |
|--|--|
|`-n`, `--name`||
|`-u`, `--unrendered-only`||
|`-v`, `--version`||

## List-Analysis-Types

The `list-analysis-types` command is used to list all analysis types with filtering and viewing options based on the provided parameters:

| Option | Description |
|--|--|
|`-hs`, `--hide-schema`|Hide the schema. Default is false|
|`-l`, `--limit`|Query limit|
|`-n`, `--names`|Filter analysisTypes by names|
|`-o`, `--offset`|Query offset|
|`-sd`, `--sort-direction`|Sorting direction. Default is DESC, possible values are DESC or ASC|
|`-so`, `--sort-order`|AnalysisType fields to sort on|
|`-u`, `--unrendered-only`|Only retrieve the unrenedered schema that was initially registered. Default is false|
|`-v`, `--versions`|Filter analysisTypes by versions|

## Register-Analysis-Type

The `register-analysis-type` command is used to register a new analysis-type schema based on the provided file:

| Option | Description |
|--|--|
|`-f`,`--file`| Supply the file path for the new analysis type |

## Search

The `search` command searches for analysis objects based on various input parameters. Unless specified by one of the following options, the `search` command will search for analysis within the current studyId.

| Option | Description |
|--|--|
|`-a`, `--analysis-id`|Search by a given analysisId|
|`-d`, `--donor-id`|Search by a given donorId|
|`-f`, `--file-id`|Search by a given fileId|
|`-sa`, `--sample-id`|Search by a given sampleId|
|`-sp`, `--specimen-id`|Search by a given specimenId|


## Manifest

The `manifest` command generates a manifest file for an analysis with an associated analysisId.

| Option | Description |
|--|--|
|`-a`, `--analysis-id`| Associated analysisId |
|`-f`, `--file`| Name and directory for outputted manifest file |
|`-d`, `---input-dir`| Directory containing the files used for upload |

For more information, see our <a href="/documentation/song/user/submit/" target="_blank" rel="noopener noreferrer">documentation on submitting data with Song</a>.

## Publish

The `publish` command is used to publish an analysis based on it's analysis Id:
| Option | Description |
|--|--|
|`-a`, `--analysis-id`| Associated analysisId |
|`-i`, `--ignore-undefined-md5`| If set, the publishing process will proceed even if the md5 hash of any file is not defined. |

 For more information, see our <a href="/documentation/song/admin/analysismanagement/" target="_blank" rel="noopener noreferrer">documentation on analysis management with Song</a>.

## Unpublish

The `unpublish` command is used to mark data as unavailable to downstream services:

| Option | Description |
|--|--|
|`-a`, `--analysis-id`| Associated analysisId |

For more information, see our <a href="/documentation/song/admin/analysismanagement/" target="_blank" rel="noopener noreferrer">documentation on analysis management with Song</a>.

## Suppress

The `suppress` command is used to block data from being accessed:

| Option | Description |
|--|--|
|`-a`, `--analysis-id`| Associated analysisId |

 For more information, see our <a href="/documentation/song/admin/analysismanagement/" target="_blank" rel="noopener noreferrer">documentation on analysis management with Song</a>.

## Export

The `export` command is used to export a payload based on a variety of input parameters:

| Option | Description |
|--|--|
|`-a`, `--analysis-id`| Export payloads under a specified analysisId |
|`-f`, `--inputFile`|Path of input file containing a single column of analysisIds on each new line|
|`-o`, `--output-dir`|Directory to save the export to|
|`-s`, `--studyId`| Export payloads under a specified studyId|
|`-t`, `--threads`| Number of concurrent threads to use during the export process. |

## Update-File

The `update-file` command is used to update file metadata:

| Option | Description |
|--|--|
|`-a`, `--access`| Options: [open, controlled], Possible Values: [open, controlled] |
|`-d`, `--datatype`| The datatype of the file, e.g., BAM, VCF. |
|`-i`, `--info`| Additional metadata or information about the file. |
|`-m`, `--md5`| MD5 hash of the file. |
|`--object-id`| Unique object ID representing the file. |
|`-s`, `--size`| Size of the file in bytes. |
