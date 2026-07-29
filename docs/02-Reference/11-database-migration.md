# Database Migration

Migrating an existing Song database to the consolidated schema introduced in Song 5.3.0.

## Overview

Song 5.3.0 introduces a significant internal change to the database schema. In earlier versions, data related to donor, specimen, and sample entities was stored across multiple tables, each with required relationships enforced by the [base schema](./04-data-model-management.md). In 5.3.0 those requirements have been removed in favour of a simpler, more flexible model: all analysis data is now stored in a single consolidated table.

Starting with 5.3.0, all new analysis data is written to this consolidated table. To keep data created by earlier versions compatible with the new structure, existing data must be migrated.

A TypeScript-based migration script handles this. It reads from and writes to a configured database, with support for pagination and connection pooling, and is designed to run in a Node.js environment (version 20 or higher).

:::warning Back up your database first
It is best practice to take a full backup of your database before running the migration script.
:::

## Prerequisites

- Node.js v20 or higher
- The [pnpm](https://pnpm.io/) package manager
- A database accessible from your environment, with a user that has sufficient privileges to read from and write to the necessary tables

## Installation

Clone the migration repository and install its dependencies:

```bash
git clone https://github.com/overture-stack/song_5_3_migration.git
cd song_5_3_migration
pnpm install
```

## Configuration

Copy `.env.schema`, rename the copy to `.env`, and place it in the root of the project. Adjust the values to match your setup:

```bash
# Number of analyses fetched per query (pagination)
ANALYSIS_QUERY_LIMIT=100

# Required DB configuration
DB_HOST=       # Database server host
DB_PORT=       # Database server port
DB_NAME=       # Name of the target database
DB_USER=       # Database username
DB_PASSWORD=   # Database user password

# Max concurrent DB connections in the pool
MAX_DB_CONNECTIONS=10
```

- `ANALYSIS_QUERY_LIMIT` controls how many analyses are fetched per query during pagination.
- `MAX_DB_CONNECTIONS` controls the number of concurrent connections in the pool. Tune it to balance migration speed against database load.

## Running the migration

With your `.env` in place, run the script:

```bash
pnpm run dev
```

This transpiles the TypeScript code and executes the main migration entry point using your configured environment, reading existing analyses and rewriting them into the consolidated table.

:::info Need Help?
If you encounter any issues or have questions about this migration, please don't hesitate to reach out through our [**support page**](/community/support) or our [**discussion forum**](https://github.com/overture-stack/docs/discussions?discussions_q=).
:::
