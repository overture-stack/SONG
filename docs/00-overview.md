
# Overview

Song functions as a file catalog system, tracking files and managing their metadata. To manage file transfers to and from object storage Song interacts with its required companion application, [Score](https://github.com/overture-stack/score).

## System Architecture

Metadata saved as a JSON file is uploaded via the Song Client for validation with schemas defined by Song's administrators. Successful submissions receive an auto-generated analysis ID. This analysis ID is used when uploading file data using Score, and on successful upload Song will be updated to track the file's storage location.

![Song Arch](./assets/songDev.svg 'Song Architecture Diagram')

As part of the larger Overture.bio software suite, Song is typically used with additional integrations, including:

- **Event Streaming:** Built-in support for [Apache Kafka](https://kafka.apache.org/) event streaming allows other services to respond when analyses are registered and published.
- **Maestro Indexing:** Song is built to natively integrate with [Maestro](https://github.com/overture-stack/maestro), which will easily index published data into a configurable Elasticsearch index. Once indexed the data can be linked to a front-end portal search interface using the Arranger and Stage services.

## Key Features

- **Admin Defined Schemas:** Customly define input fields with definable rules, logic and syntax
- **Metadata Validations:** Validate metadata on submission with clear and concise error handling.
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
└── /song-servers
```

[Click here to view the Song respository on GitHub ](https://github.com/overture-stack/song)

### song-core

- Shared code used in the Song client and server
- Establishes Song's data model and common interfaces

### song-client

- CLI tools to interact with a Song server
- Published as [docker container](https://github.com/overture-stack/SONG/pkgs/container/song-client)
- Can be compiled into Java application to run locally outside of docker
- Used by system administrators to manage stored analysis data and to configure dynamic schemas
- Used by data submitters to create analysis, upload file metadata, and to publish/unpublish analyses

### song-server

- Main Song server application
- Spring-boot application that runs a web server and connects to Postgres DB for data storage

### Unsupported Packages

Song has several additional packages in its code base which are currently unsupported and may be out of date with the latest version of Song. Feel free to use or explore these but be aware that they are deprecated and may have undocumented issues.

These packages are:
- **song-docker-demo**: Example all-in-one deployment of Song with Score with other external project Auth and ID services.
- **song-docs**: Deprecated `readthedocs` documentation site
- **song-go-client**: Alternate cli imlementation, written in GoLang.
- **song-java-sdk**: Code library for java applications to interact with a Song server programatically.
- **song-python-sdk**: Code library for java applications to interact with a Song server programatically.


