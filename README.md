<h1 align="center">Song</h1>

<p align="center">Quickly and reliably track genome metadata scattered across multiple Cloud storage systems.</p>

<p align="center"><a href="http://www.overture.bio/products/song" target="_blank"><img alt="General Availability" title="General Availability" src="http://www.overture.bio/img/progress-horizontal-GA.svg" width="320" /></a></p>

[![Documentation Status](http://readthedocs.org/projects/song-docs/badge/?version=develop)](https://song-docs.readthedocs.io/en/develop/introduction.html)
[![Slack](http://slack.overture.bio/badge.svg)](http://slack.overture.bio)

Project containing both the SONG microservice and CLI client.
Both are written using JAVA 8 and Spring Boot.

## Documentation

Explore documentation with the Song [Read the Docs](https://song-docs.readthedocs.io/en/develop/introduction.html).

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

Both the server and client when compiled and built produce uber jars which can be run easily from the command line.

```bash
$ java -jar song-server-0.1.1-SNAPSHOT.jar  --spring.profiles.active=dev,test
```

## Docker Based Developement

Several `make` targets are provided for locally deploying dependent services using docker.
By using this, the developer will be able to replicate a live environment for song-server and song-client.
It allows the user to develop locally, and test submissions, manifest creation, publishing, unpublishing and score uploads/downloads in an isolated environment.

There are 2 modes:

### 1. Developement Mode

The purpose of this mode is to decrease the wait time between building and testing against dependent services.
This mode will run a `mvn package` if the distribution files are missing and copy them into a container for them to be run.
This method allows for fast developement, since the `mvn package` step is handled on the **Docker host**.
In addition, the debug ports `5005` and `5006` are exposed for both `song-client` and `song-server`, respectively, allowing developers to debug the docker containers.
This mode can be enabled using the `DEMO_MODE=0` override. This is the default behaviour if the variable `DEMO_MODE` is not defined.

#### Debugging the song-client with IntelliJ

Since the JVM debug port is exposed by the `song-client` docker container, IntelliJ can **remotely debug** a running docker container.
To do this, a **docker image run profile** must be created with the configuration outputted by the `make intellij-song-client-config` command, which will output a basic upload command, however it can be modified to be any song-client command.
Then, a **remote debug profile** must be created, with the following config:

```
Host: localhost
Port: 5005
Use module classpath: song-client
```

and in the `Before launch: Activate tool window` section, click the `+` sign, and select `Launch docker before debug`.
Then ensure the `Docker configuration` field is set to the name of the previously created **docker image run profile** and that `Custom Options` is set to `-p 5005:5005`. In order for the debugger to bind to the debug port in time,
a delay needs to be introduced after starting the container. To do this, click the `+` sign again, and select `Launch docker before debug`, and select `Run External Tool` and a window will pop-up. Input the following:

```
Name:      Sleep for 5 seconds
Program:   /usr/bin/sleep
Arguments: 5
```

and click `OK`.

Finally, start debugging by simply running the **remote debug profile** and it will call the **docker image run profile** before launch.

#### Executing the dockerized song-client in developement mode

The script `./docker/tools/song-client-dev` takes the arguments runs the `song-client` service entry specified in the `docker-compose.yml` with them. For example, to ping the song server, run `./docker/tools/song-client ping`

#### Debugging the song-server with IntelliJ

Since the `song-server` is a server and exposes the 5006 debug port, configuration is much easier. First, start the server with `make clean start-song-server`. Then, create a **remote debug profile** in Intellij with the following configuration:

```
Host: localhost
Port: 5006
Use module classpath: song-server
```

and then run it in debug mode.

### 2. Demo Mode

The purpose of this mode is to demo the current `song-server` and `song-client` code by building it in **inside the Docker image**,
as opposed to the **Docker host** as is done in Developement mode and then running the containers.
This mode will not run `mvn package` on the Docker host, but instead inside the Docker container.
This method is very slow, since maven will download dependencies every time a build is triggered, however creates a completely isolated environment for testing.
This mode can be enabled using the `DEMO_MODE=1` make variable override. For example, to start the song-server, the following command would be run:

#### Executing the dockerized song-client in demo mode

The script `./docker/tools/song-client-demo` takes the arguments runs the `song-client` service entry specified in the `docker-compose.yml` with them. For example, to ping the song server, run `./docker/tools/song-client ping`

```bash
make start-song-server DEMO_MODE=1
```

For more information on the different targets, run `make help` or read the comments above each target for a description

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

### Docker Song Client

The `song-client` is a CLI tool used for communicating with a `song-server`.

#### Building

Simply running `mvn clean package` will package the client into a `-dist.tar.gz` file.

#### Configuration

After unarchiving the distribution, it can be configured via the `./conf/application.yml` file. Alternatively, the client can be configured through environment variables, which take presedence over the `application.yml` config.
For example, to run the `song-client config` command using environment variables with the same values as the `application.yml` configuration below:

```yaml
client:
  serverUrl: http://localhost:8080
  studyId: ABC123-CA
  programName: sing
  debug: true
  accessToken: myAccessToken
```

could be done via:

```bash
CLIENT_SERVER_URL=http://localhost:8080 \
CLIENT_STUDY_ID=ABC123-CA \
CLIENT_PROGRAM_NAME=sing \
CLIENT_DEBUG=true \
CLIENT_ACCESS_TOKEN=myAccessToken \
./bin/sing config
```

#### Running the client locally

The `song-client` can be run using the `./bin/sing` script.

#### Running the client using Docker

Alternatively, the `song-client` can be run using docker. To run the dockerized client with the configurations above, the following command could be executed:

```bash
docker run --rm \
  -e 'CLIENT_SERVER_URL=http://localhost:8080' \
  -e 'CLIENT_STUDY_ID=ABC123-CA' \
  -e 'CLIENT_PROGRAM_NAME=sing' \
  -e 'CLIENT_DEBUG=true' \
  -e 'CLIENT_ACCESS_TOKEN=myAccessToken' \
  overture/song-client:latest \
  sing config
```

By default, the `song-client` is run as the root user. To run as a non-root user, add the switch `-u song` which will run the command as a predefined `song` user:

```bash
docker run --rm \
  -u song \
  -e 'CLIENT_SERVER_URL=http://localhost:8080' \
  -e 'CLIENT_STUDY_ID=ABC123-CA' \
  -e 'CLIENT_PROGRAM_NAME=sing' \
  -e 'CLIENT_DEBUG=true' \
  -e 'CLIENT_ACCESS_TOKEN=myAccessToken' \
  overture/song-client:latest \
  sing config
```

or run it as your current user:

```bash
docker run --rm \
  -u $(id -u):$(id -g) \
  -e 'CLIENT_SERVER_URL=http://localhost:8080' \
  -e 'CLIENT_STUDY_ID=ABC123-CA' \
  -e 'CLIENT_PROGRAM_NAME=sing' \
  -e 'CLIENT_DEBUG=true' \
  -e 'CLIENT_ACCESS_TOKEN=myAccessToken' \
  overture/song-client:latest \
  sing config
```

Running as the host user is useful when the `song-client` needs to write to a mounted volume

#### Outputting data from the song-client via Docker

Some song-client commands (such as `sing manifest` and `sing export`) output a file to a path.
When running the docker container, it maybe preferable to output the file to the docker host's filesystem, instead of the containers file system.
To do this, a directory from the docker host must be mounted into the song-client docker container.

For example, the following command will generate a manifest file called `output-manifest.txt` in the directory `./mydir`:

```bash

# Ensure the current user owns mydir inorder to write to it from within the container
mkdir -p ./mydir

docker run --rm \
  -u $(id -u):$(id -g) \
  -v $PWD/mydir:/data \
  -e 'CLIENT_SERVER_URL=http://localhost:8080' \
  -e 'CLIENT_STUDY_ID=ABC123-CA' \
  -e 'CLIENT_PROGRAM_NAME=sing' \
  -e 'CLIENT_DEBUG=true' \
  -e 'CLIENT_ACCESS_TOKEN=myAccessToken' \
  overture/song-client:latest \
  sing manifest -a someAnalysisId -d /data -f /data/output-manifest.txt
```

### Notes

When running with the secure profile enabled, an oauth2 server is needed.

Test
