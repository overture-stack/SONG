# Setup

There are two ways to set up a song-server in a development environment:
​
- Or in a **[Docker environment](#run-as-a-container)** 
- As a **[Standalone Server](#run-as-a-standalone-server)** (requiring setup of dependent services)

Both will require you to clone the Song repository to your local computer:

```bash
git clone https://github.com/overture-stack/SONG.git
```

## Run as a Container

Several _make_ targets are provided for locally deploying the dependent services using Docker. As the developer, you can replicate a live environment for **song-server** and **song-client**. Using Docker allows you to develop locally, test submissions, create manifests, publish, unpublish and test score uploads/downloads in an isolated environment.

For more information on the different targets, run `make help` or read the comments above each target for a description.

### Prerequisites

- We reccommend Docker (version 4.32.0 or higher)
- You will need an internet connection for the _make_ command, which may take several minutes to build. 

### Starting the Server

#### For M1 Mac Systems

On a Mac M1 you must set the Docker BuildKit environment variable to the legacy builder.
​
```bash
DOCKER_BUILDKIT=0 make clean start-song-server
```

#### For all other Systems

To start song-server and all dependencies, use the following command:
​
```bash
make clean start-song-server
```

### Stopping the Server

To clean everything, including killing all services, maven cleaning, and removing generated files/directories, use the following command:

```bash
make clean
```

**Warning:** Docker for Song is meant to demonstrate the configuration and usage of Song, and is **_not intended for production_**. If you ignore this warning and use this in any public or production environment, please remember to change the passwords, accessKeys, and secretKeys.

## Run as a Standalone Server

### Prerequisites

- [JDK11](https://www.oracle.com/ca-en/java/technologies/downloads/) and [Maven3](https://maven.apache.org/download.cgi) are required to set up this service from source. 

### Building the Server

​To build the song-server run the following command from the Song directory:

```bash
./mvnw clean install -DskipTests
```

### Starting the Server

Before running your song-server, ensure that your local machine is connected and running the following dependent services:
​
- Song database (default localhost:5432)
- Kafka (default localhost:9092). Required only if _kafka_ Spring profile is enabled
- Ego or Keycloak server is required
- Score server is required only if the _score-client-cred_ Spring profile is enabled
  ​
Set the configuration of above dependent services on `song-server/​src/main/resources/application.yml` the following profiles are available to you:

**Profiles**
| Profile | Description |
| - | - |
| `secure` | Required to secure endpoints |
| `noSecurityDev` | To not secure endpoints |
| `kafka` | Required to send messages to Kafka |
| `default` | To not send messages to Kafka |
| `score-client-cred` | Required to set score server credentials |

Run the following command to start the song-server:

```bash
cd song-server/
mvn spring-boot:run -Dspring-boot.run.profiles=noSecurityDev,default,score-client-cred
```

**Warning:** This guide is meant to demonstrate the configuration and usage of SONG for development purposes and is **_not intended for production_**. If you ignore this warning and use this in any public or production environment, please remember to use Spring profiles accordingly. For production, use the following profiles: **Kafka,secure,score-client-cred​**

## Configure with Keycloak

[Keycloak](https://www.keycloak.org/) is an open-source identity and access management solution that can be used to manage users and application permissions. You can find basic information on integrating Score and Keycloak using docker from our user docs [located here](https://www.overture.bio/documentation/song/configuration/authentication/). For a comprehensive guide on installing and configuring Keycloak, refer to the [Keycloak documentation](https://www.keycloak.org/documentation).

### For a standalone server

If you’re building song using the the source code, the following configuration is required in `song-server/src/main/resources/application.yml`

```yaml
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

### For Docker

Run the following compose to spin up dependant services:

```yaml
version: '2.3'
services:
  postgresql:
    image: docker.io/bitnami/postgresql:16
    environment:
      # ALLOW_EMPTY_PASSWORD is recommended only for development.
      - ALLOW_EMPTY_PASSWORD=yes
      - POSTGRESQL_USERNAME=bn_keycloak
      - POSTGRESQL_DATABASE=bitnami_keycloak
    volumes:
      - 'postgresql_data:/bitnami/postgresql'
  keycloak:
    build: .
    depends_on:
      - postgresql
    ports:
      - "80:8080"
      # remote debugging port is recommended only for development
      # - "8787:8787"
    environment:
      # remote debugging is recommended only for development
      # - DEBUG=true
      # - DEBUG_PORT=*:8787
      - KC_DB=postgres
      - KC_DB_URL=jdbc:postgresql://postgresql/bitnami_keycloak
      - KC_DB_USERNAME=bn_keycloak
    volumes:
      - type: bind
        source: ./target/dasniko.keycloak-2fa-sms-authenticator.jar
        target: /opt/bitnami/keycloak/providers/keycloak-sms-auth.jar
      - type: bind
        source: data_import
        target: /opt/bitnami/keycloak/data/import
    volumes:
    postgresql_data:
        driver: local
```

Create a `.env.song` file with the necessary environment variables:

```bash

```
