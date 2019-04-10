<h1 align="center">SONG</h1>

<p align="center">Quickly and reliably track genome metadata scattered across multiple Cloud storage systems.</p>

<p align="center"><a href="http://www.overture.bio/products/song" target="_blank"><img alt="General Availability" title="General Availability" src="http://www.overture.bio/img/progress-horizontal-GA.svg" width="320" /></a></p>

[![Build Status](https://travis-ci.org/overture-stack/SONG.svg?branch=develop)](https://travis-ci.org/overture-stack/SONG)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/c3515fa60c114da1a7a4be8d46674eca)](https://www.codacy.com/app/overture-stack/SONG?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=overture-stack/SONG&amp;utm_campaign=Badge_Grade)
[![CircleCI](https://circleci.com/gh/overture-stack/SONG/tree/develop.svg?style=svg)](https://circleci.com/gh/overture-stack/SONG/tree/develop)
[![Documentation Status](http://readthedocs.org/projects/song-docs/badge/?version=develop)](https://song-docs.readthedocs.io/en/develop/introduction.html)
[![Slack](http://slack.overture.bio/badge.svg)](http://slack.overture.bio)


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
$ mvn spring-boot:run -Dspring-boot.run.profiles=dev,test
```

Both the server and client when compiled and built produce  uber jars which can be run easily from the command line.

```bash
$ java -jar song-server-0.1.1-SNAPSHOT.jar  --spring.profiles.active=dev,test
```

## API

The server provides swagger docs documenting the API. 

When running locally they can be accessed here: http://localhost:8080/swagger-ui.html

## Dockerhub Configuration
1. Edit build configurations by selecting the `Builds` tab at the top, then click `Build Configuration`
2. Create a new build rule by clicking the `+` sign beside the `BUILD RULES` text
3. Edit the configuration as follows:
   Source Type: `branch`
   Source: `develop`
   Docker Tag: `develop`
   Dockerfile Location: `Dockerfile`
   Build Context: `/`
   `Autobuild` is set to the ON position
   `Build Caching` is set to the ON position
4. Then save the configuration

### Notes

When running with the secure profile enabled, an oauth2 server is needed. 

Test
