# Schema Strictness

Song's schema strictness determines whether analyses adhere strictly to the latest schema version or can use any version.

Song has two analysis validation modes: **strict** and **non-strict**:

- In **strict mode**, all submitted data must adhere to the latest version of the analysis schema. For example, if the newest version of the schema is 5, all analyses of type `variant_calling` must conform to version 5 of the `variant_calling` schema.

- In **non-strict mode**, submitted data can conform to any schema version. If users don't specify a version in their payload, the latest version of the schema is used for validation.

    :::tip
    If you are unsure which mode to choose, we recommend using the strict mode as it ensures that all data being submitted is up-to-date.
    :::

### Basic Configuration

The following configuration is used for Schema Strictness in [Songs application.yaml](https://github.com/overture-stack/SONG/blob/develop/song-server/src/main/resources/application.yml):

```yaml title="./song-server/src/main/resources/application.yml"
schemas:
  enforceLatest: true  # Enforce the use of the latest schema version
```

### Environment Variable Setup

You can also configure Schema strictness using the following environment variable:

```bash
SCHEMAS_ENFORCELATEST=true
```

By setting `SCHEMAS_ENFORCELATEST` to true, the Song server will enforce that data conforms to the latest schema versions. Conversely, if set to false, data can be submitted under any schema version.

:::info Need Help?
If you encounter any issues or have questions about our API, please don't hesitate to reach out through our relevant [**community support channels**](https://docs.overture.bio/community/support).
:::
