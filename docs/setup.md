# Setup

## Prerequisites

Before you begin, ensure you have the following installed on your system:
- [JDK11](https://www.oracle.com/ca-en/java/technologies/downloads/)
- [Docker](https://www.docker.com/products/docker-desktop/) (v4.32.0 or higher)

## Developer Setup

This guide will walk you through setting up a complete development environment, including Song and its complementary services.

### Setting up supporting services

We'll use our Conductor service, a flexible Docker Compose setup, to spin up Song's complementary services.

1. Clone the Conductor repository and move into its directory:

    ```bash
    git clone https://github.com/overture-stack/conductor.git
    cd conductor
    ```

2. Run the appropriate start command for your operating system:

   | Operating System | Command         |
   |------------------|-----------------|
   | Unix/macOS       | `make SongDev`  |
   | Windows          | `make.bat SongDev` |


<details>
<summary>**Click here for a detailed breakdown**</summary>

This command will set up all complementary services for Song development as follows:

![SongDev](./assets/songDev.svg 'Song Dev Environment')

| Service | Port | Description | Purpose in Score Development |
|---------|------|-------------|------------------------------|
| Conductor | `9204` | Orchestrates deployments and environment setups | Manages the overall development environment |
| Keycloak-db | - | Database for Keycloak (no exposed port) | Stores Keycloak data for authentication |
| Keycloak | `8180` | Authorization and authentication service | Provides OAuth2 authentication for Score |
| Song-db | `5433` | Database for Song | Stores metadata managed by Song |
| Score | `8087` | File Transfer service | Handles file uploads, downloads, and storage operation |
| Minio | `9000` | Object storage provider | Simulates S3-compatible storage for Score |

- Ensure all ports are free on your system before starting the environment.
- You may need to adjust the ports in the `docker-compose.yml` file if you have conflicts with existing services.

For more information, see our [Conductor documentation linked here](/docs/other-software/Conductor)

</details>

### Running the Development Server 

1. Clone Song and move into its directory:

    ```bash
    git clone https://github.com/overture-stack/song.git
    cd song
    ```

2. Build the application locally:

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

3. Start the Song Server:

   ```bash
   ./mvnw spring-boot:run -Dspring-boot.run.profiles=default,dev,secure -pl song-server
   ```

<details>
<summary>**Click here for an explanation of the command above**</summary>

- `./mvnw spring-boot:run` starts the Spring Boot application using the Maven wrapper.
- `-Dspring-boot.run.profiles=default,dev,secure` specifies which Spring profiles to activate.
- `-pl song-server` tells Maven to run the `song-server` module specifically.

Song Server's configuration file can be found in the Song repository [located here](https://github.com/overture-stack/SONG/blob/develop/song-server/src/main/resources/application.yml). A summary of the available profiles is provided below:

**Song Profiles**
| Profile | Description |
|---------|-------------|
| `default` | Required to load common configurations |
| `secure` | Required to load security configuration |
| `dev` | (Optional) Facilitates development default configuration |
| `prod` | (Optional) Loads production-specific configurations |
| `kafka` | (Optional) Enables Kafka integration |
| `score-client-cred` | (Optional) Configures SCORE client credentials |
| `test` | Used for testing purposes |
| `async-test` | Used for asynchronous testing |
| `fastTest` | Used for fast testing with reduced timeouts |


</details>

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
   - Using Swagger UI:
     1. Locate the `/GetAnalysesForStudy` endpoint
     2. Click to expand and select "Try it out"
     3. Set parameters:
        - analysisStates: PUBLISHED
        - studyId: demo
     4. Click "Execute"
   - Alternatively, use curl:
     ```bash
     curl -X GET "http://localhost:8080/studies/demo/analysis?analysisStates=PUBLISHED" -H "accept: */*"
     ```
   - Expected result: JSON response containing analysis data for the demo study

For further assistance, [open an issue on GitHub](https://github.com/overture-stack/song/issues/new?assignees=&labels=&projects=&template=Feature_Requests.md).

:::warning
This guide is meant to demonstrate the configuration and usage of Song for development purposes and is not intended for production. If you ignore this warning and use this in any public or production environment, please remember to use Spring profiles accordingly. For production do not use **dev** profile.
:::

