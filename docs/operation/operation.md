
**Navigation**

- [Background](../index.md)
- [Contribution](../contribution/contribution.md) 

---

# Operational Docs

This page provides detailed steps and configurations required to set up your development environment with Song, either through a standalone setup or using Docker. Additionally, it includes instructions for integrating Keycloak for authentication and authorization.

## On this page

- [Setting up the Development Environment](#setting-up-the-development-environment)
  - [Standalone score-server](#standalone-song-server)
    - [Clone the Score Repository](#clone-the-song-repository)
    - [Build](#build)
    - [Start the Server](#start-the-server)
  - [Docker for Song](#docker-for-song)
    - [Start Song-server and all dependencies](#start-song-server-and-all-dependencies)
    - [Start the Song-server (Mac M1 Users)](#start-the-song-server-mac-m1-users)
    - [Stop Song-server and clean up](#stop-song-server-and-clean-up)
- [Integrating Keycloak](#integrating-keycloak)
  - [Standalone](#standalone)

# Developer Setup

## Setting up the development environment

There are two ways to set up a song-server in a development environment:
​
- As a **[Standalone Server](#standalone-song-server)** (requires dependent services)
- Or in a **[Docker environment](#docker-for-song)**

## Standalone song-server

### Clone the Song Repository

Clone the Song repository to your local computer:

```bash
git clone https://github.com/overture-stack/SONG.git
```

### Build

[JDK11](https://www.oracle.com/ca-en/java/technologies/downloads/) and [Maven3](https://maven.apache.org/download.cgi) are required to set up this service.
​
To build the song-server run the following command from the Song directory:

```bash
./mvnw clean install -DskipTests
```

### Start the server

Before running your song-server, ensure that your local machine is connected and running the following dependent services:
​

- Song database (default localhost:5432)
- Kafka (default localhost:9092). Required only if _kafka_ Spring profile is enabled
- Ego or Keycloak server is required
- Score server is required only if the _score-client-cred_ Spring profile is enabled
  ​

Set the configuration of above dependent services on **song-server/​src/main/resources/application.yml** and make sure to use the profiles acording your needs.

**Profiles**
| Profile | Description |
| - | - |
| _secure_ | Required to secure endpoints |
| _noSecurityDev_ | To not secure endpoints |
| _kafka_ | Required to send messages to Kafka |
| _default_ | To not send messages to Kafka |
| _score-client-cred_ | Required to set score server credentials |

Run the following command to start the song-server:
​

```bash
cd song-server/
mvn spring-boot:run -Dspring-boot.run.profiles=noSecurityDev,default,score-client-cred
```

> **Warning:**
> This guide is meant to demonstrate the configuration and usage of SONG for development purposes and is **_not intended for production_**. If you ignore this warning and use this in any public or production environment, please remember to use Spring profiles accordingly. For production, use the following profiles: **Kafka,secure,score-client-cred**.

### Docker for Song

Several _make_ targets are provided for locally deploying the dependent services using Docker. As the developer, you can replicate a live environment for **song-server** and **song-client**. Using Docker allows you to develop locally, test submissions, create manifests, publish, unpublish and test score uploads/downloads in an isolated environment.
​
For more information on the different targets, run `make help` or read the comments above each target for a description.
​

> Note:
> We will need an internet connection for the _make_ command, which may take several minutes to build. No external services are required for the _make_ command.

### Start song-server and all dependencies.

To start song-server and all dependencies, use the following command:
​

```bash
make clean start-song-server
```

### Start the song-server (Mac M1 Users)

On a Mac M1 you must set the Docker BuildKit environment variable to the legacy builder.
​

```bash
DOCKER_BUILDKIT=0 make clean start-song-server
```

### Stop song-server and clean up

To clean everything, including killing all services, maven cleaning, and removing generated files/directories, use the following command:
​

```bash
make clean
```

> **Warning**
> Docker for Song is meant to demonstrate the configuration and usage of Song, and is **_not intended for production_**. If you ignore this warning and use this in any public or production environment, please remember to change the passwords, accessKeys, and secretKeys.

## Integrating Keycloak

[Keycloak](https://www.keycloak.org/) is an open-source identity and access management solution that can be used to manage users and application permissions. You can find basic information on integrating Score and Keycloak using docker from our user docs [located here](https://www.overture.bio/documentation/song/configuration/authentication/). For a comprehensive guide on installing and configuring Keycloak, refer to the [Keycloak documentation](https://www.keycloak.org/documentation).

### Standalone:

If you’re building song using the the source code, the following configuration is required in _song-server/src/main/resources/application.yml_

```bash
auth:
	server:
		# check API Key endpoint
		url: http://localhost/realms/myrealm/apikey/check_api_key/
		tokenName: apiKey
		clientID: song
		clientSecret: songsecret
		provider: keycloak
		# Keycloak config
		keycloak:
			host: http://localhost
			realm: "myrealm"

spring:
    security:
        oauth2:
            resourceserver:
                jwt:
                    # EGO public key
                    #public-key-location: "http://localhost:9082/oauth/token/public_key"
                    # Keycloak JWK
                    jwk-set-uri: http://localhost/realms/myrealm/protocol/openid-connect/certs
```

---

**Navigation**

- [Background](../index.md)
- [Contribution](../contribution/contribution.md) 

---