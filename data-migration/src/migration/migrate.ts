import { pool } from '../config/db';
import { env } from '../config/env';
import type { AnalysisData, UpdateAnalysesResult } from '../types';
import { getSamplesStructureForAnalyses } from './samplesStructureForAnalyses.js';
import { updateAnalyses } from './updateAnalyses';

// DB pagination query limit
const limit = env.ANALYSIS_QUERY_LIMIT;

/**
 * Retrieves all studies found in database
 * @returns
 */
const getAllStudies = async (): Promise<
	{ id: string; name: string; description: string; organization: string }[] | null
> => {
	const result = await pool.query('SELECT * FROM study');
	return result.rows;
};

/**
 * Retrieves all analyses by specific study.
 * This function uses pagination with limit and offset provided
 * @param studyId
 * @param limit
 * @param offset
 * @returns
 */
const getAnalysesFromStudy = async (studyId: string, limit: number, offset: number): Promise<AnalysisData[]> => {
	const analysisRes = await pool.query(
		'SELECT id, analysis_data_id FROM analysis WHERE study_id = $1 ORDER BY created_at, updated_at ASC LIMIT $2 OFFSET $3',
		[studyId, limit, offset],
	);

	return analysisRes.rows;
};

export const migrateData = async (): Promise<void> => {
	try {
		console.log('Starting migration script');

		let totalAnalysisCount = 0;

		// Iterate studies
		const allStudies = await getAllStudies();
		if (!allStudies) {
			throw new Error('No studies found in the database');
		}

		for (const study of allStudies) {
			let offset = 0;
			let studyAnalysisCount = 0;
			const failedAnalyses: UpdateAnalysesResult[] = [];
			console.log(`[${study.id}]:`, `Starting fetching analysis`);

			while (true) {
				const startDate = Date.now();

				// Paginate analysis
				const analyses = await getAnalysesFromStudy(study.id, limit, offset);

				if (analyses.length === 0) {
					break;
				}

				studyAnalysisCount += analyses.length;

				// Process the current batch of rows
				const analysesMappingDonors = await getSamplesStructureForAnalyses(analyses);

				const updateResult = await updateAnalyses(analysesMappingDonors);
				const failed = updateResult.filter((result) => !result.success);

				if (failed.length > 0) {
					failedAnalyses.push(...failed);
				}

				const endDate = Date.now();
				console.log(
					`[${study.id}]:`,
					`Processed ${analyses.length} analyses to get samples, donors and specimen in ${
						endDate - startDate
					} milliseconds`,
				);

				offset += limit;
			}

			if (failedAnalyses.length > 0) {
				console.error(`[${study.id}]:`, `Failed to update ${failedAnalyses.length} analyses`);
				failedAnalyses.forEach((result) => {
					console.error(`[${study.id}]:`, `Failed Analysis Data ID: "${result.analysisDataId}"`, result.error);
				});
			} else {
				console.log(`[${study.id}]:`, `All analyses updated successfully`);
			}

			console.log(`[${study.id}]:`, `Finished migrating '${studyAnalysisCount}' analyses`);
			totalAnalysisCount += studyAnalysisCount;
		}

		console.log(`Total analyses migrated: ${totalAnalysisCount}`);
		console.log('Data migration complete.');
		process.exit(0);
	} catch (err) {
		console.error('Migration failed:', err);
		process.exit(1);
	}
};
