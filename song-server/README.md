# SONG

SONG Server - Metadata and Validation system 

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
kj
## Flyway

[Flyway](https://flywaydb.org/) is used to manage database migrations. Usually, you do not need to worry about it. But if you have existing database that does not align with the flyway migrations, please [baseline](https://flywaydb.org/documentation/command/baseline) the database by:

```bash
flyway baseline
```

Once you have the data structure set up, you may want to load test data.

```bash
psql -f song-server/src/main/resources/data.sql DATABASE_NAME
```
