# SONG
SONG - Metadata and Validation system 

[![Build Status](https://travis-ci.org/overture-stack/SONG.svg?branch=develop)](https://travis-ci.org/overture-stack/SONG)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/c3515fa60c114da1a7a4be8d46674eca)](https://www.codacy.com/app/overture-stack/SONG?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=overture-stack/SONG&amp;utm_campaign=Badge_Grade)
[![CircleCI](https://circleci.com/gh/overture-stack/SONG/tree/develop.svg?style=svg)](https://circleci.com/gh/overture-stack/SONG/tree/develop)

Project containing both the SONG microservice and CLI client. 
Both are written using JAVA 8 and Spring Boot. 

## Build

```bash
$ mvn clean package
```

## Running


#### Command-line

The source can be built and run using maven.

```bash
$ git clone git@github.com:icgc-dcc/SONG.git
$ cd SONG/song-server
$ mvn spring-boot:run -Drun.profiles=dev,test
```

Both the server and client when compiled and built produce  uber jars which can be run easily from the command line.

```bash
$ java -jar song-server-0.1.1-SNAPSHOT.jar  --spring.profiles.active=dev,test
```

## API

The server provides swagger docs documenting the API. 

When running locally they can be accessed here: http://localhost:8080/swagger-ui.html



### Notes

When running with the secure profile enabled, an oauth2 server is needed. 

Test
