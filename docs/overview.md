
# Overview

Song functions as a file catalog system, tracking files and managing their metadata. To manage file transfers to and from object storage Song interacts with its required companion application, [Score](https://github.com/overture-stack/score).

## System Architecture

Metadata saved as a JSON file us uploaded via the Song Client for validation against Songs admin defined Song Schema. Successful submissions receive an auto-generated analysis ID. File data is then uploaded using Song and Score clients, generating a file manifest linked to the metadata. \

![Song Arch](./assets/songArch.svg 'Song Architecture Diagram')

As part of the larger Overture.bio software suite, Song is typically used with additional integrations, including:

- **Event Streaming:** Built-in support for [Apache Kafka](https://kafka.apache.org/) event streaming allows other services to respond when analyses are registered and published.

- **Maestro Indexing:** Song is built to natively integrate with [Maestro](https://github.com/overture-stack/maestro), which will easily index published data into a configurable Elasticsearch index. Once indexed the data can be linked to a front-end portal search interface using the Arranger and Stage services.

## Key Features

- **Admin Defined Schemas:** Customly define input fields with definable rules, logic and syntax
- **Metadata Validations:**  on submission with clear and concise error handling.
- **Automated Identifiers:** Song generates automated identifiers (Analysis IDs) linking metadata with object data handled by Score
- **OAuth2 Security:** ACL security using OAuth2 and scopes based on study codes for enhanced data protection.
- **Flexible Metadata Fields:** Optional schema-less JSON info fields for user-specific metadata, allowing customization.
- **Song Mirroring Support:** Export payloads for SONG mirroring, facilitating data synchronization.
- **Interactive API Documentation:** Built-in Swagger UI for easy API interaction and exploration.


## Repository Structure

```
.
├── /song-client
├── /song-core
├── /song-go-client
└── /song-servers
```

#### song-clients

[Explaination]

#### song-core

[Explaination]

#### song-go-client

[Explaination]

#### song-server

[Explaination]


