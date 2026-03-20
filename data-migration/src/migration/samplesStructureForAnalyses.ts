import { pool } from '../config/db';
import type { AnalysisData, AnalysisSamplesMapping, Donor, Sample, Specimen } from '../types';

/**
 * Retrieves the samples structure for a list of analyses using a single batch JOIN query.
 * @param analysisList An array of Analysis objects to be processed
 * @returns A mapping of analysis_data_id to an array of Sample objects
 */
const getSamplesStructureForAnalyses = async (analysisList: AnalysisData[]): Promise<AnalysisSamplesMapping> => {
	if (analysisList.length === 0) {
		return {};
	}

	// Build a lookup from analysis.id → analysis_data_id so we can group results below
	const analysisIdToDataId = new Map(analysisList.map(({ id, analysis_data_id }) => [id, analysis_data_id]));
	const analysisIds = analysisList.map((a) => a.id);

	const result = await pool.query<{
		analysis_id: string;
		sample_id: string;
		sample_submitter_id: string;
		sample_type: string;
		matched_normal_submitter_sample_id: string | null;
		specimen_id: string;
		specimen_type: string;
		specimen_submitter_id: string;
		tumour_normal_designation: string;
		specimen_tissue_source: string;
		donor_id: string;
		donor_submitter_id: string;
		gender: string;
	}>(
		`SELECT
			ss.analysis_id,
			sa.id                                AS sample_id,
			sa.submitter_id                      AS sample_submitter_id,
			sa.type                              AS sample_type,
			sa.matched_normal_submitter_sample_id,
			sp.id                                AS specimen_id,
			sp.type                              AS specimen_type,
			sp.submitter_id                      AS specimen_submitter_id,
			sp.tumour_normal_designation,
			sp.tissue_source                     AS specimen_tissue_source,
			d.id                                 AS donor_id,
			d.submitter_id                       AS donor_submitter_id,
			d.gender
		FROM sampleset ss
		JOIN sample   sa ON ss.sample_id   = sa.id
		JOIN specimen sp ON sa.specimen_id = sp.id
		JOIN donor    d  ON sp.donor_id    = d.id
		WHERE ss.analysis_id = ANY($1)`,
		[analysisIds],
	);

	// Pre-initialise mapping with empty arrays so analyses with no samples are still present
	const mapping: AnalysisSamplesMapping = {};
	for (const { analysis_data_id } of analysisList) {
		mapping[analysis_data_id] = [];
	}

	for (const row of result.rows) {
		const analysisDataId = analysisIdToDataId.get(row.analysis_id);
		if (analysisDataId === undefined) {
			continue;
		}

		const specimen: Specimen = {
			specimenId: row.specimen_id,
			specimenType: row.specimen_type,
			submitterSpecimenId: row.specimen_submitter_id,
			tumourNormalDesignation: row.tumour_normal_designation,
			specimenTissueSource: row.specimen_tissue_source,
		};

		const donor: Donor = {
			donorId: row.donor_id,
			submitterDonorId: row.donor_submitter_id,
			gender: row.gender,
		};

		const sample: Sample = {
			sampleId: row.sample_id,
			submitterSampleId: row.sample_submitter_id,
			sampleType: row.sample_type,
			matchedNormalSubmitterSampleId: row.matched_normal_submitter_sample_id,
			specimen,
			donor,
		};

		mapping[analysisDataId].push(sample);
	}

	return mapping;
};

export { getSamplesStructureForAnalyses };
