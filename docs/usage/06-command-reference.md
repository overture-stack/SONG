# Client Reference

Commands and options supported by the Song client.

## General Commands

### Config

- The `config` command shows the current configuration settings.

- **Usage:** `song-client config`

### Ping

- The `ping` command can test the connection to the Song server.

- **Usage:** `song-client ping`

## Analysis Management Commands

### Get-Analysis-Type

- Retrieves specific analysis type schema information.

- **Usage:** `song-client get-analysis-type [OPTIONS]`

    | Option | Description |
    |--------|-------------|
    | `-n`, `--name` | Name of the analysis type |
    | `-u`, `--unrendered-only` | Retrieve only the unrendered schema |
    | `-v`, `--version` | Version of the analysis type |

### List-Analysis-Types

- Lists all analysis types with filtering and viewing options.

- **Usage:** `song-client list-analysis-types [OPTIONS]`

    | Option | Description |
    |--------|-------------|
    | `-hs`, `--hide-schema` | Hide the schema (Default: false) |
    | `-l`, `--limit` | Query limit |
    | `-n`, `--names` | Filter analysis types by names |
    | `-o`, `--offset` | Query offset |
    | `-sd`, `--sort-direction` | Sorting direction (Default: DESC, Options: DESC or ASC) |
    | `-so`, `--sort-order` | Analysis type fields to sort on |
    | `-u`, `--unrendered-only` | Only retrieve the unrendered schema (Default: false) |
    | `-v`, `--versions` | Filter analysis types by versions |

### Register-Analysis-Type

- Registers a new analysis-type schema.

- **Usage:** `song-client register-analysis-type [OPTIONS]`

    | Option | Description |
    |--------|-------------|
    | `-f`, `--file` | File path for the new analysis type |

### Submit

- Submits a payload to create an analysis.

- **Usage:** `song-client submit [OPTIONS]`

    :::info
    For detailed information, see the [documentation on submitting data with Song](/).
    :::

### Search

- Searches for analysis objects based on various parameters.

- **Usage:** `song-client search [OPTIONS]`

    | Option | Description |
    |--------|-------------|
    | `-a`, `--analysis-id` | Search by analysisId |
    | `-d`, `--donor-id` | Search by donorId |
    | `-f`, `--file-id` | Search by fileId |
    | `-sa`, `--sample-id` | Search by sampleId |
    | `-sp`, `--specimen-id` | Search by specimenId |

### Manifest

- Generates a manifest file for an analysis.

- **Usage:** `song-client manifest [OPTIONS]`

    | Option | Description |
    |--------|-------------|
    | `-a`, `--analysis-id` | Associated analysisId |
    | `-f`, `--file` | Output manifest file name and directory |
    | `-d`, `--input-dir` | Directory containing upload files |

    :::info
    For more information, see the [documentation on submitting data with Song](/).
    :::

### Publish

- Publishes an analysis.

- **Usage:** `song-client publish [OPTIONS]`

    | Option | Description |
    |--------|-------------|
    | `-a`, `--analysis-id` | Associated analysisId |
    | `-i`, `--ignore-undefined-md5` | Proceed if any file's md5 hash is undefined |

### Unpublish

- Marks data as unavailable to downstream services.

- **Usage:** `song-client unpublish [OPTIONS]`

    | Option | Description |
    |--------|-------------|
    | `-a`, `--analysis-id` | Associated analysisId |

### Suppress

- Blocks data from being accessed.

- **Usage:** `song-client suppress [OPTIONS]`

    | Option | Description |
    |--------|-------------|
    | `-a`, `--analysis-id` | Associated analysisId |


    :::info
    For more information on analysis management, see the [documentation on analysis management with Song](/).
    :::

### Export

- Exports payloads based on various parameters.

- **Usage:** `song-client export [OPTIONS]`

    | Option | Description |
    |--------|-------------|
    | `-a`, `--analysis-id` | Export payloads for a specific analysisId |
    | `-f`, `--inputFile` | Path to file with analysisIds (one per line) |
    | `-o`, `--output-dir` | Directory to save the export |
    | `-s`, `--studyId` | Export payloads for a specific studyId |
    | `-t`, `--threads` | Number of concurrent export threads |

### Update-File

- Updates file metadata.

- **Usage:** `song-client update-file [OPTIONS]`

    | Option | Description |
    |--------|-------------|
    | `-a`, `--access` | File access (Options: open, controlled) |
    | `-d`, `--datatype` | File datatype (e.g., BAM, VCF) |
    | `-i`, `--info` | Additional file metadata |
    | `-m`, `--md5` | File MD5 hash |
    | `--object-id` | Unique object ID of the file |
    | `-s`, `--size` | File size in bytes |

## Need Help?

If you encounter any issues or have questions about our API, please don't hesitate to reach out through our relevant[community support channels](/community/support)