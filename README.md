# SONG
SONG - Metadata and Validation system 

[![Build Status](https://travis-ci.org/icgc-dcc/SONG.svg?branch=develop)](https://travis-ci.org/icgc-dcc/SONG)

Contains code adapted from Corey Hulen https://github.com/coreyhulen/earnstone-id
Licensed under Apache License 2.0
## Build

```bash
mvn clean package
```

## Running

Both the server and client components build uber jars which can be run easily from the command line.

```bash
java -jar song-server-0.1.1-SNAPSHOT.jar  --spring.profiles.active=dev,test
```

### Notes

When running with the secure profile enabled, an oauth2 server is needed. 