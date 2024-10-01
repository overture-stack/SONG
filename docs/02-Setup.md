# Setup

## Prerequisites

Before you begin, ensure you have the following installed on your system:
s
- [JDK11](https://www.oracle.com/ca-en/java/technologies/downloads/)
- [Maven3](https://maven.apache.org/download.cgi)

## Installation

This guide will walk you through setting up a complete development environment, including Song and its complementary services.

### 1. Set up complementary services

We'll use our Conductor service, a flexible Docker Compose setup, to spin up Songs complementary services.

```bash
git clone https://github.com/overture-stack/conductor.git
cd conductor
```

Next, run the appropriate start command for your operating system:

| Operating System | Command |
|------------------|---------|
| Unix/macOS       | `make songDev` |
| Windows          | `make.bat songDev` |

This command will set up all complementary services for Song development.

### 2. Clone and set up Song

Now, let's set up Song itself as a standalone server:

```bash
git clone https://github.com/overture-stack/song.git
cd song
```

### 3. Configure Song for Keycloak integration

If you’re building song using the the source code, the following configuration is required in the `song-server/src/main/resources/application.yml`

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

### 4. Build the Server

​To build the song-server run the following command from the Song directory:

```bash
./mvnw clean install -DskipTests
```

### 5. Start the Server

Run the following command to start the song-server:

```bash
cd song-server/
mvn spring-boot:run -Dspring-boot.run.profiles=noSecurityDev,default,score-client-cred
```

**Warning:** Docker for Song is meant to demonstrate the configuration and usage of Song, and is **_not intended for production_**. If you ignore this warning and use this in any public or production environment, please remember to change the passwords, accessKeys, and secretKeys.

## Troubleshooting

If you encounter any issues during setup:

1. Ensure all prerequisites are correctly installed and at the specified versions.
2. Check that all services in the Docker Compose setup are running correctly.
3. If you're having network issues, ensure that the ports specified in the configuration are not being used by other services.

For further assistance, feel free to [open an issue through GitHub here](https://github.com/overture-stack/stage/issues/new?assignees=&labels=&projects=&template=Feature_Requests.md).
