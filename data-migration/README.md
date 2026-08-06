# Database Migration Script

> Note: This project contains a TypeScript-based database migration script designed to run in a **Node.js** environment (version 20 or higher). 

**SONG Version 5.3.0** introduces a significant internal change to the database schema. Previously, data related to **donor**, **specimen**, and **sample** entities was stored across multiple tables, each with required relationships enforced by the base schema. In version **5.3.0**, these requirements have been removed in favor of a simplified and more flexible model where all data is now stored in a single consolidated table.

Starting with **SONG version 5.3.0**, all new analysis data is stored in a single consolidated table. Which means, to maintain compatibility with this new structure, **existing data must be migrated accordingly**.

 
This script migrates the existing data to the updated database structure. It reads from and writes to a configured database, supporting pagination and connection pooling.

# Prerequisites

- [Node.js](https://nodejs.org/en) v20 or higher
- [pnpm](https://pnpm.io/) package manager
- A database accessible from your environment

# Installation

1. Clone Song and change into this directory:

```bash
git clone https://github.com/overture-stack/SONG.git
cd SONG/data-migration
```

2. Install dependencies using pnpm:

```bash
pnpm install
```

# Configuration

Before running the script, copy the `.env.schema` and name it `.env`. Make sure to place this file in the root of the project and adjust the values according to your setup.

```dotenv
# Number of analyses fetched per query (pagination)
ANALYSIS_QUERY_LIMIT=100

## Required DB configuration
DB_HOST=                  # Database server host
DB_PORT=                  # Database server port
DB_NAME=                  # Name of the target database
DB_USER=                  # Database username
DB_PASSWORD=              # Database user password

# Max concurrent DB connections in pool
MAX_DB_CONNECTIONS=10
```

# Running the Script (Development Mode)

> Important: It is best practice to take a full backup of current database before running this script.

To run the migration script in development mode:

```
pnpm run dev
```

> **Note:** This command will transpile the TypeScript code and execute the main migration entry point using your configured environment.

## Notes

- Ensure the database user has sufficient privileges to read from and write to the necessary tables.
- The script uses connection pooling to optimize performance. You can control the number of concurrent connections with `MAX_DB_CONNECTIONS`.
