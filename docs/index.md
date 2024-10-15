# Song Developer Documentation

---

**Navigation**
- [Operation](./operation/operation.md)
- [Contribution](./contribution/contribution.md) 

---

# Background

Song is a metadata validation and tracking tool designed to streamline the management of genomics data across multiple cloud storage systems. It functions as a file catalog tracking files and managing their metadata, without handling file transfer and object storage itself. Song interacts with a required companion application, [Score](https://github.com/overture-stack/score), which manages file transfers and object storage.

## Data Submission

**Analyses:** An analysis is a collection of one or more files, along with the metadata that describes this collection.

**Metadata Validation:** Analyses get validated against a metadata schema that defines the vocabulary and structure of the analysis document. Song allows administrators to define custom schemas that describe the Analyses they intend to manage.

**Tracking of Metadata to File Data:** Once validated, the analysis document is stored in the Song repository and assigned an automated analysis ID. This ID is then used when uploading all associated file data through Score. The analysis ID links the metadata stored in Song with the file data being transferred by Score and stored in the cloud.

## Data Administration

**Dynamic Schemas:** With Song, data administrators can create Dynamic Schemas for multiple types of analyses. These schemas define the vocabulary for the structural validation of JSON formatted data.

This ensures that:

- All required fields are included upon submission.
- The contents of each field match the expected data type.
- Only allowed values (enums) are used.

**Data Lifecycle Management:** Analyses uploaded to a Song repository are `UNPUBLISHED` by default. To make data available for search and download, administrators update it to a `PUBLISHED` state. If data is no longer relevant, it can be set to a `SUPPRESSED` state, making it unavailable for search and download through downstream services.

**Note on File Availability:** An analysis cannot be published unless all its associated files have been uploaded to and are available with Score. This ensures that all published analyses have their files available for download through Score.

<Note title="The Song Client">We created the `song-client` command line tool to streamline interactions with Songs REST API endpoints. For more information on what the `song-client` can do, see our [Song client command reference documentation](/documentation/song/reference/commands/).</Note>

## Integrations

As part of the larger Overture.bio software suite, Song can be optionally used with additional integrations, including:

- **Event Streaming:** Built-in support for [Apache Kafka](https://kafka.apache.org/) event streaming allows other services to respond when analyses are registered and published.

- **Maestro Indexing:** Song is built to natively integrate with [Maestro](https://github.com/overture-stack/maestro), which will easily index data into a configurable Elasticsearch index, to be used for convenient searching of data.

---

**Navigation**
- [Operation](./operation/operation.md)
- [Contribution](./contribution/contribution.md) 

---