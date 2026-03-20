import { pool } from '../config/db';
import type { AnalysisSamplesMapping, UpdateAnalysesResult } from '../types';

/**
 * Updates the analyses in the database with the provided samples mapping.
 * @param samplesMapping Mapping of analysis_data_id's to arrays of Sample objects
 * @returns
 */
export const updateAnalyses = async (samplesMapping: AnalysisSamplesMapping) => {
	// make sure analysis have samples
	const entries = Object.entries(samplesMapping).filter(([, samples]) => samples.length > 0);

	const updatePromises = entries.map<Promise<UpdateAnalysesResult>>(async ([analysisDataId, samples]) => {
		try {
			const resultUpdate = await pool.query('UPDATE analysis_data SET data = data || $1 WHERE id = $2', [
				{ samples },
				analysisDataId,
			]);

			if (resultUpdate.rowCount === 0) {
				console.error(`No rows updated for Analysis Data ID: "${analysisDataId}".`);
			}

			return { analysisDataId, success: (resultUpdate.rowCount || 0) > 0 };
		} catch (error) {
			const errorMessage = error instanceof Error ? error.message : 'Unknown error';
			return { analysisDataId, success: false, error: errorMessage };
		}
	});

	return await Promise.all(updatePromises);
};
