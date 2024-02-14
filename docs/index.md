# Song Developer Documentation

---

**Navigation**
- [Operation](./operation/operation.md)
- [Contribution](./contribution/contribution.md) 

---

# Background

Song is a metadata validation and tracking tool designed to streamline the management of genomics data across multiple cloud storage systems. With Song, users can create high-quality and reliable metadata repositories with minimal human intervention. As a metadata management system, Song does not handle file transfer and object storage. Song interacts with a required companion application, <a href="https://github.com/overture-stack/score" target="_blank" rel="noopener noreferrer">Score</a>, which manages file transfers and object storage.

## Data Submission

**Analysis Files:** An analysis is a description of a set of one or more files plus the metadata describing that collection of files.

**Metadata Validation:** Analyses get validated against the administrator's Dynamic Schema. That defines the vocabulary and structure of the analysis document. 

**Tracking of Metadata to File Data:** Once validated, the analysis document is stored in the Song repository and given an automated analysis ID. The analysis ID is then used when uploading all associated file data through Score. Analysis IDs associate the metadata stored in Song with the file data being transferred by score and stored in the cloud.

## Data Administration

**Dynamic Schemas:** The data administrator creates the Dynamic Schema, which again, provides the vocabulary for the structural validation of JSON formatted data (Analysis documents), for example, ensuring that required fields are present or that the contents of a field match the desired data type or allowed values.

**Data Lifecycle Management:** Analyses uploaded to a Song repository are `UNPUBLISHED` by default. When data is ready for search and download, administrators can make it available by updating it to a `PUBLISHED` state. If data is no longer relevant, the data administrators can set it to a `SUPPRESSED` state, making it unavailable for search and download through downstream services. 

<Note title="The Song Client">We created the `song-client` command line tool to streamline interactions with Songs REST API endpoints. For more information on what the `song-client` can do, see our [Song client command reference documentation](/documentation/song/reference/commands/).</Note>

## Integrations

As part of the larger Overture.bio software suite, Song can be optionally used with additional integrations, including:

- **Event Streaming:** Built-in support for <a href="https://kafka.apache.org/" target="_blank" rel="noopener noreferrer">Apache Kafka</a> event streaming allows other services to respond when analyses are registered and published.


- **Maestro Indexing:** Song is built to natively integrate with <a href="https://github.com/overture-stack/maestro" target="_blank" rel="noopener noreferrer">Maestro</a>, which will easily index data into a configurable Elasticsearch index, to be used for convenient searching of data. 

---

**Navigation**

- [Operation](./operation/operation.md)
- [Contribution](./contribution/contribution.md) 

---