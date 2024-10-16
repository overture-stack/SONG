# Publication Controls

Managing data release with publication controls.

Administrators control analysis availability to downstream services like such as our indexing service [Maestro](/docs/core-software/Maestro/overviews) across three states:

- **Unpublished:** By default, new data committed to Songs database is in this state as it awaits file upload. Custom configurations can allow Maestro to index this data, making it available to users to see the pending analysis without availble file data to download.

- **Published:** By default published analyses will trigger Maestro to index. To be published, analyses require file data available in object storage.

- **Suppressed:** For data that shouldn't be used, available or needs to blocked from use. Though not downloadable by default, custom setups can let Maestro index these and allow users to see what analyses have been made unavailable. Please be cognizant and use this state with care. Analyses in this state cannot be reverted.

:::info Removing Indexed Data
To ensure the safe removal of indexed data, the suggested state transition for suppression is: Published → Unpublished → Suppressed.
:::

## Managing Publication Status

Updating publication statuses can be done through two main methods: the Song Client command line tool and the Swagger UI. The Swagger UI provides a user-friendly interface for interacting with the Song API, while the Song Client allows you to more easily perform data management operations from the command line.

- **Setting up the Song Client:** For detailed instructions on setting up the Song Client, please refer to the section on [Installing the Song Client](/docs/core-software/Song/setup#song-client-setup).

- **Accessing the Swagger UI:** To access the Swagger UI, you need to determine the URL based on your Song setup. 

  - If you are running Song locally, the Swagger UI can be accessed at `http://localhost:8080/swagger-ui.html`. 
  - If Song is deployed on a server, the URL will be `https://<Your-URL>/song-api/swagger-ui.html#`.

# Using the Song Client

You can use the Song Client to perform analysis management operations from the command line.

## Publishing an Analysis 

To publish an analysis use the following command with the desired analysis ID:

```bash
docker exec song-client sh -c "sing publish -a <insert-analysis-id>"
```

## Unpublishing an Analysis 

To unpublish an analysis use the following command with the desired analysis ID:

```bash
docker exec song-client sh -c "sing unpublish -a <insert-analysis-id>"
```

## Suppressing an Analysis 

To surpress an analysis use the following command with the desired analysis ID:

```bash
docker exec song-client sh -c "sing suppress -a <insert-analysis-id>"
```
 
# Using the Swagger API 

1. **Locate your endpoint of interest:** From the analysis dropdown you can find the `PUT` endpoints for publishing, unpublishing and suppressing analyses. Select your endpoint of interest and click `Try it out`.

    ![Entity](../assets/swagger_analysis_endpoints.png 'analysis endpoints')

2. **Input the Analysis ID and Credentials:** Input your authorization token and the analysis ID and study ID corresponding to the data you want to suppress.

    ![Entity](../assets/swagger-publishid.png 'publish endpoint')

    :::info
    For Authorization include the "Bearer " prefix. All API requests requiring authorization must include the "Bearer" prefix before your authentication token in the Authorization header (ex. `Authorization: Bearer your_token_here`)
    :::

3. **Execute:** Click Execute. The Swagger UI will provide your response and detailed descriptions of all potential response codes.

:::info Support
For technical support or specific use cases, please don't hesitate to reach out through our relevant [**community support channels**](/community/support).
:::
