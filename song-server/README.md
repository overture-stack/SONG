# SONG

## SONG Server - Metadata and Validation system

This module of SONG implements the following functions:

- distributed entity ID generation
- endpoints to accept and validate metadata JSON documents
- CRUD functions for metadata entities

Contains code adapted from Corey Hulen https://github.com/coreyhulen/earnstone-id
Licensed under Apache License 2.0

## Build

```bash
mvn clean package
```

Note: inorder to prevent the spring-boot-maven-plugin from overwriting the original jar with the uberjar, the `<classifier>exec</classifier>` attribute was added to the configuration to suffix the uberjar with __exec__

## Flyway

Database migrations and versioning is managed by flyway [Flyway](https://flywaydb.org/).

To see current database's migration info:

```bash
./mvnw -pl song-server flyway:info -Dflyway.url='jdbc:postgresql://localhost:5432/test_db?user=postgres'
```

Migrate database to the latest version:

Note: Due to a postgres jdbc driver issue, make sure to include parameter `stringtype=unspecified` in the jdbc connection url. 
```bash
./mvnw -pl song-server flyway:migrate -Dflyway.url='jdbc:postgresql://localhost:5432/song?stringtype=unspecified' -Dflyway.locations=classpath:db.migration
```

If you have existing database that does not align with the flyway migrations, please [baseline](https://flywaydb.org/documentation/command/baseline) the database by:

```bash
./mvnw -pl song-server flyway:baseline
```

To see the migration [naming convention](https://flywaydb.org/documentation/migrations#naming).

Once you have the data structure set up, you may want to load test data.

```bash
psql -f song-server/src/main/resources/data.sql DATABASE_NAME
```

### Setting Up Local Development
- Run song-server in these spring profiles for local development: `dev,noSecurityDev`.
- If you need to work with kafka, add `kafka` to spring profiles.
- Comment out line 73 - 76 in `SecurityConfig.java`: https://github.com/overture-stack/SONG/blob/develop/song-server/src/main/java/bio/overture/song/server/config/SecurityConfig.java#L73

#### Quick Start 
- Here is an example on how to quickly set up a testing schema and create analyses for local development.
    - Create a simple schema using `POST /schema` endpoint, the schema can be as simple as: 
    ```json
    {
      "name": "Simple",
      "schema": {
        "type": "object",
        "properties": { "experiment": { "type": "string" } }
      }
    }
    ```
    - Create an analysis using `POST /submit/{studyId}` endpoint, the `json_payload` must contain the basic analysis information:
```json
       {
          "name": "Simple",
          "schema": {
            "studyId": "TEST-CA",
            "analysisType": { "name": "Simple", "version": 1 },
            "samples": [
              {
                "submitterSampleId": "subSA123",
                "matchedNormalSubmitterSampleId": null,
                "sampleType": "Total DNA",
                "donor": {
                  "studyId": "TEST-CA",
                  "submitterDonorId": "subDO123",
                  "gender": "Male"
                },
                "specimen": {
                  "donorId": "50cae385-92a7-58b0-ab24-59dd1da8c5d6",
                  "submitterSpecimenId": "subSP123",
                  "tumourNormalDesignation": "Normal",
                  "specimenTissueSource": "Bone",
                  "specimenType": "Normal"
                }
              }
            ],
            "files": [
              {
                "objectId": "c30b3e9e-c6f3-4524-bba5-04dc5ade1ea5",
                "studyId": "TEST-CA",
                "fileName": "test1.tsv",
                "fileSize": 123,
                "fileType": "BAM",
                "fileMd5sum": "12345678901234567890123456789012",
                "fileAccess": "open",
                "dataType": "tsv"
              }
            ]
          }
        }
```

## Swagger API
To access swagger api: http://localhost:8080/swagger-ui.html