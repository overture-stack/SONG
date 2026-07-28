# Client Reference

Commands and options supported by the Song client.

## General Commands

### Config

- The `config` command shows the current configuration settings.

- **Usage:** `sing config`

### Ping

- The `ping` command can test the connection to the Song server.

- **Usage:** `sing ping`

## Analysis Management Commands

### Get-Analysis-Type

- Retrieves specific analysis type schema information.

- **Usage:** `sing get-analysis-type [OPTIONS]`

    | Option | Description |
    |--------|-------------|
    | `-n`, `--name` | Name of the analysis type |
    | `-u`, `--unrendered-only` | Retrieve only the unrendered schema |
    | `-v`, `--version` | Version of the analysis type |

### List-Analysis-Types

- Lists all analysis types with filtering and viewing options.

- **Usage:** `sing list-analysis-types [OPTIONS]`

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

- **Usage:** `sing register-analysis-type [OPTIONS]`

    | Option | Description |
    |--------|-------------|
    | `-f`, `--file` | File path for the new analysis type |

### Submit

- Submits a payload to create an analysis.

- **Usage:** `sing submit [OPTIONS]`

    | Option                      | Description                                         |
    |-----------------------------|-----------------------------------------------------|
    | `-f`, `--file`              | File name and directory for the payload             |
    | `-ad`, `--allow-duplicates` | Allows duplicate files identified by their MD5 hash |

    :::info
    For detailed information, see our [documentation on submitting data with Song](/develop/Song/Reference/submitting-metadata).
    :::

### Search

- Searches for analysis objects based on various parameters.

- **Usage:** `sing search [OPTIONS]`

    | Option | Description |
    |--------|-------------|
    | `-a`, `--analysis-id` | Search by analysisId |
    | `-f`, `--file-id` | Search by fileId |

### Manifest

- Generates a manifest file for an analysis.

- **Usage:** `sing manifest [OPTIONS]`

    | Option | Description |
    |--------|-------------|
    | `-a`, `--analysis-id` | Associated analysisId |
    | `-f`, `--file` | Output manifest file name and directory |
    | `-d`, `--input-dir` | Directory containing upload files |

    :::info
    For more information, see our [documentation on submitting data with Song](/develop/Song/Reference/submitting-metadata).
    :::

### Publish

- Publishes an analysis.

- **Usage:** `sing publish [OPTIONS]`

    | Option | Description |
    |--------|-------------|
    | `-a`, `--analysis-id` | Associated analysisId |
    | `-i`, `--ignore-undefined-md5` | Proceed if any file's md5 hash is undefined |

### Unpublish

- Marks data as unavailable to downstream services.

- **Usage:** `sing unpublish [OPTIONS]`

    | Option | Description |
    |--------|-------------|
    | `-a`, `--analysis-id` | Associated analysisId |

### Suppress

- Blocks data from being accessed.

- **Usage:** `sing suppress [OPTIONS]`

    | Option | Description |
    |--------|-------------|
    | `-a`, `--analysis-id` | Associated analysisId |


    :::info
    For more information on analysis management, see our [documentation on Song publication controls](/develop/Song/Reference/publication-controls).
    :::

### Export

- Exports payloads based on various parameters.

- **Usage:** `sing export [OPTIONS]`

    | Option | Description |
    |--------|-------------|
    | `-a`, `--analysis-id` | Export payloads for a specific analysisId |
    | `-f`, `--inputFile` | Path to file with analysisIds (one per line) |
    | `-o`, `--output-dir` | Directory to save the export |
    | `-s`, `--studyId` | Export payloads for a specific studyId |
    | `-t`, `--threads` | Number of concurrent export threads |

### Update-File

- Updates file metadata.

- **Usage:** `sing update-file [OPTIONS]`

    | Option | Description |
    |--------|-------------|
    | `-a`, `--access` | File access (Options: open, controlled) |
    | `-d`, `--datatype` | File datatype (e.g., BAM, VCF) |
    | `-i`, `--info` | Additional file metadata |
    | `-m`, `--md5` | File MD5 hash |
    | `--object-id` | Unique object ID of the file |
    | `-s`, `--size` | File size in bytes |

## Need Help?

If you encounter any issues or have questions, please don't hesitate to reach out through our [**support page**](/community/support) or our [**discussion forum**](https://github.com/overture-stack/docs/discussions?discussions_q=).