import { Pool } from 'pg';

import { env } from './env';

export const pool = new Pool({
	user: env.DB_USER,
	host: env.DB_HOST,
	database: env.DB_NAME,
	password: env.DB_PASSWORD,
	port: env.DB_PORT,
	max: env.MAX_DB_CONNECTIONS, // limit number of concurrent connections
});

// Gracefully shut down DB Pool to avoid orphaned connections
process.on('SIGINT', async () => {
	console.log('\nGracefully shutting down (SIGINT)...');
	await shutdown();
	process.exit(0);
});

process.on('SIGTERM', async () => {
	console.log('\nGracefully shutting down (SIGTERM)...');
	await shutdown();
	process.exit(0);
});

const shutdown = async () => {
	try {
		await pool.end(); // This closes all idle clients and waits for active ones to complete
		console.log('Database connections closed.');
	} catch (err) {
		console.error('Error closing database connections:', err);
	}
};
