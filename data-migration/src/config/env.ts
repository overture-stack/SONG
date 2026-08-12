import { config } from 'dotenv';
import { z } from 'zod';

config();

const envSchema = z.object({
	ANALYSIS_QUERY_LIMIT: z.string().default('20').transform(Number),
	DB_HOST: z.string(),
	DB_NAME: z.string(),
	DB_PORT: z.string().transform(Number),
	DB_USER: z.string(),
	DB_PASSWORD: z.string(),
	MAX_DB_CONNECTIONS: z.string().default('10').transform(Number),
});

export const env = envSchema.parse(process.env);
