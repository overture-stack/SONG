# ID Management

Song assigns unique identifiers to the two entities it tracks: **analyses** and **files**. Both are generated internally by Song in UUID format — no external ID service is required.

## Analysis IDs

Each analysis is assigned an **Analysis ID**, a randomly generated UUID, when it is created. Song registers each Analysis ID to guarantee uniqueness across the system. Analysis IDs are used to track, retrieve, and manage an analysis throughout Song and Score.

## File (Object) IDs

Each file within an analysis is assigned a **File ID** (also referred to as an object ID). File IDs are deterministic: Song computes them as a name-based (UUID5, SHA-1) hash of the analysis ID and file name. Because they are derived from these inputs rather than stored to enforce uniqueness, the same analysis ID and file name always resolve to the same File ID.

## Immutability

Song-assigned IDs are immutable and cannot be altered once created. If you need to change a value that contributes to an ID, UNPUBLISH and SUPPRESS the analysis, then resubmit it with the new information. See [**Updating Metadata**](./02-updating-metadata.md) for details.

:::note ID management change in Song 5.3.0
Earlier versions of Song also managed donor, specimen, and sample IDs and supported a "federated" mode that delegated ID generation to an external ID service. As of 5.3.0, donor, specimen, and sample entities have been removed from the base schema, and Song generates the remaining analysis and file IDs internally. For upgrading existing deployments, see [**Database Migration**](./11-database-migration.md).
:::

:::info Need Help?
If you encounter any issues or have questions about our API, please don't hesitate to reach out through our [**support page**](/community/support) or our [**discussion forum**](https://github.com/overture-stack/docs/discussions?discussions_q=).
:::
