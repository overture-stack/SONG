# Setup

## Prerequisites

Before you begin, ensure you have the following installed on your system:

- [JDK11](https://www.oracle.com/ca-en/java/technologies/downloads/)
- [Docker](https://www.docker.com/products/docker-desktop/) (v4.39.0 or higher)

## Song-Server Development Setup

This guide will walk you through setting up a complete development environment, including Song and its complementary services.

### Setting up supporting services

The Song repository ships its own `docker-compose.yml` and `Makefile`, which together start every service Song depends on. No other repository is required.

1. Clone Song and move into its directory:

   ```bash
   git clone https://github.com/overture-stack/song.git
   cd song
   ```

2. Start Song's dependencies:

   ```bash
   make start-deps
   ```

    <details>
    <summary>**Click here for a detailed breakdown**</summary>

   `make start-deps` packages the project and then brings up Keycloak, Score, and object storage from the repository's `docker-compose.yml`:

   | Service     | Port   | Description                                     | Purpose in Song Development                            |
   | ----------- | ------ | ----------------------------------------------- | ------------------------------------------------------ |
   | Keycloak    | `9082` | Authorization and authentication service        | Provides OAuth2 authentication for Song                |
   | Keycloak-db | `9444` | Database for Keycloak                           | Stores Keycloak data for authentication                |
   | Score       | `8087` | File Transfer service                           | Handles file uploads, downloads, and storage operation |
   | Minio       | `8085` | Object storage provider                         | Simulates S3-compatible storage for Score              |

   Keycloak starts with the `myrealm` realm imported from `docker/keycloak-init/data_import`, and downloads the `keycloak-apikeys` provider on start-up so it can issue API keys.

   To bring up Song itself along with its database and all of the above, use `make start-song-server` instead. That adds:

   | Service     | Port           | Description           | Purpose in Song Development      |
   | ----------- | -------------- | --------------------- | -------------------------------- |
   | Song-db     | `8432`         | Database for Song     | Stores metadata managed by Song  |
   | Song-server | `8080`, `5006` | The Song server       | The service under development; `5006` is the JVM debug port |

   - Ensure these ports are free on your system before starting the environment.
   - You may need to adjust the ports in the `docker-compose.yml` file if you have conflicts with existing services.
   - `make clean` tears the stack down and removes the build output; `make log-song-server` tails the server's logs.

   :::note

   These targets build the project with the bundled Maven wrapper and drive Docker Compose, so a JDK is required even when you only want the supporting services. See the prerequisites above.

   :::

    </details>

### Running the Development Server

Use these steps to run Song on your host, against the supporting services started above. To run Song in a container instead, `make start-song-server` covers both.

1.  Build the application locally:

    ```bash
    ./mvnw clean install -DskipTests
    ```

     <details>
     <summary>**Click here for an explaination of command above**</summary>

    - `./mvnw`: This is the Maven wrapper script, which ensures you're using the correct version of Maven.
    - `clean`: This removes any previously compiled files.
    - `install`: This compiles the project, runs tests, and installs the package into your local Maven repository.
    - `-DskipTests`: This flag skips running tests during the build process to speed things up.

     </details>

    :::tip
    Ensure you are running JDK11. To check, you can run `java --version`. You should see something similar to the following:

    ```bash
    openjdk version "11.0.18" 2023-01-17 LTS
    OpenJDK Runtime Environment Corretto-11.0.18.10.1 (build 11.0.18+10-LTS)
    OpenJDK 64-Bit Server VM Corretto-11.0.18.10.1 (build 11.0.18+10-LTS, mixed mode)
    ```

    :::

2.  Start the Song Server:

    ```bash
    ./mvnw spring-boot:run -Dspring-boot.run.profiles=default,dev,secure -pl song-server
    ```

        :::info

             If you are looking to configure Song for your specific environment, [**the Song-server configuration file can be found here**](https://github.com/overture-stack/song/blob/develop/song-server/src/main/resources/application.yml). A summary of the available Spring profiles is provided below:

             <details>
             <summary>**Click here for a summary of the Song-server spring profiles**</summary>

             **Song Profiles**
             | Profile             | Description                                              |
             | ------------------- | -------------------------------------------------------- |
             | `default`           | Required to load common configurations                   |
             | `secure`            | Required to load security configuration                  |
             | `dev`               | (Optional) Facilitates development default configuration |
             | `prod`              | (Optional) Loads production-specific configurations      |
             | `kafka`             | (Optional) Enables Kafka integration                     |
             | `score-client-cred` | (Optional) Configures SCORE client credentials           |
             | `test`              | Used for testing purposes                                |
             | `async-test`        | Used for asynchronous testing                            |
             | `fastTest`          | Used for fast testing with reduced timeouts              |

             </details>

         :::

### Verification

After installing and configuring Song, verify that the system is functioning correctly:

1. **Check Server Health**

   ```bash
   curl -s -o /dev/null -w "%{http_code}" "http://localhost:8080/isAlive" -H "accept: */*"
   ```

   - Expected result: Status code `200`
   - Troubleshooting:
     - Ensure Song server is running
     - Check you're using the correct port (default is 8080)
     - Verify no firewall issues are blocking the connection

2. **Check the Swagger UI**

   - Navigate to `http://localhost:8080/swagger-ui.html` in a web browser
   - Expected result: Swagger UI page with a list of available API endpoints
   - Troubleshooting:
     - Check browser console for error messages
     - Verify you're using the correct URL

3. **Test GET Analysis Endpoint**

   This step needs a study to query. The repository's Compose stack starts with an empty database, so create one first (see [Data model management](/develop/Song/Reference/data-model-management)) and substitute its ID for `<your-study-id>` below.

   - Using Swagger UI:
     1. Locate the `GetAnalysesForStudy` endpoint in the **Analysis** section: `GET /studies/{studyId}/analysis/paginated`
     2. Click to expand and select "Try it out"
     3. Set parameters:
        - analysisStates: PUBLISHED
        - studyId: `<your-study-id>`
     4. Click "Execute"
   - Alternatively, use curl:
     ```bash
     curl -X GET "http://localhost:8080/studies/<your-study-id>/analysis?analysisStates=PUBLISHED" -H "accept: */*"
     ```
   - Expected result: JSON response containing the analyses registered under that study

:::info Need Help?
If you encounter any issues or have questions about our API, please don't hesitate to reach out through our [**support page**](/community/support) or our [**discussion forum**](https://github.com/overture-stack/docs/discussions?discussions_q=).
:::

## Song-Client Setup

The `song-client` is a CLI tool used for communicating with a `song-server`. For ease of deployment it can be run using Docker. The client can be configured through environment variables, which take precedence over the `application.yml` config.

```bash
docker run -d --name song-client \
   -e CLIENT_ACCESS_TOKEN=<your-api-key> \
   -e CLIENT_STUDY_ID=<your-study-id> \
   -e CLIENT_SERVER_URL=http://localhost:8080 \
   --network="host" \
   --platform="linux/amd64" \
   --mount type=bind,source=${pwd},target=/output \
ghcr.io/overture-stack/song-client:5.1.1 \
```

:::info Obtaining an API key

`CLIENT_ACCESS_TOKEN` is environment-specific; there is no fixed development token. The Keycloak that `make start-deps` brings up on port `9082` loads the `keycloak-apikeys` provider, which issues keys against the `myrealm` realm. See [Authentication](/develop/Song/Reference/authentication) for how the provider is installed and how Song validates the keys it issues.

<details>
<summary>**Click here for the steps to generate a key against the local stack**</summary>

The realm ships the users `admin` (a member of the `ADMIN` group) and `testca_user` (a member of `TESTCASONG_GROUP`), both with hashed passwords that are not recoverable from the realm export. Keys can only be issued by their owner or an administrator, so start by giving one of those users a password you know.

1. Open the Keycloak admin console at `http://localhost:9082` and sign in. The image's default administrator credentials are `user` / `bitnami`.

2. In the `myrealm` realm, set a password for the `admin` user (**Users** → `admin` → **Credentials**). Note its user ID from the same page; you will need it below.

3. Request a token for that user. The realm's `system` client has direct access grants enabled:

   ```bash
   curl -X POST "http://localhost:9082/realms/myrealm/protocol/openid-connect/token" \
     -d "grant_type=password" \
     -d "client_id=system" -d "client_secret=systemsecret" \
     -d "username=admin" -d "password=<the password you just set>"
   ```

4. Exchange that token for an API key, substituting the user ID from step 2:

   ```bash
   curl -X POST "http://localhost:9082/realms/myrealm/apikey/api_key?user_id=<user-id>&scopes=song.WRITE&scopes=score.WRITE" \
     -H "Authorization: Bearer <access_token from step 3>"
   ```

   The `name` field of the response is the key value. Pass it as `CLIENT_ACCESS_TOKEN`:

   ```json
   {
     "name": "5b1da354-37bd-409d-b938-ea14b8035bc3",
     "scope": ["score.WRITE", "song.WRITE"],
     "expiryDate": "2027-07-30T15:32:59.990+0000",
     "isRevoked": false
   }
   ```

Scopes take the form `<resource>.<READ|WRITE>`, and the resources the realm defines are `song`, `score`, `TEST-CA`, and `ABC123`. A request for a scope the user's group does not carry is rejected with `Invalid Scope`.

</details>

`CLIENT_STUDY_ID` must name a study that already exists on your server. The repository's Compose stack starts with an empty database, so create one first; see [Data model management](/develop/Song/Reference/data-model-management).

:::

    <details>
    <summary>**Click here for an explaination of command above**</summary>
      - `-e CLIENT_ACCESS_TOKEN=<your-api-key>` supplies the API key the song-client authenticates with, obtained from Keycloak as described above.
      - `-e CLIENT_STUDY_ID=<your-study-id>` the Study ID the song-client operates against, supplied on start-up.
      - `-e CLIENT_SERVER_URL=http://localhost:8080` is the url for the Song server which the Song-Client will interact with.
      - `--network="host"` Uses the host network stack inside the container, bypassing the usual network isolation. This means the container shares the network namespace with the host machine.
      - `--platform="linux/amd64"` Specifies the platform the container should emulate. In this case, it's set to linux/amd64, indicating the container is intended to run on a Linux system with an AMD64 architecture.
      - `--mount type=bind,source={pwd},target=/output` mounts the directory and its contents (volume) from the host machine to the container. In this case, it binds the present working directory from the host to /output inside the container. Any changes made to the files in this directory will be reflected in both locations.
    </details>

:::warning
This guide is meant to demonstrate the configuration and usage of Song for development purposes and is not intended for production. If you ignore this warning and use this in any public or production environment, please remember to use Spring profiles accordingly. For production do not use **dev** profile.
:::
