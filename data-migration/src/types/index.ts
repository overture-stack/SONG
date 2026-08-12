export type Sample = {
	sampleId: string;
	submitterSampleId: string;
	sampleType: string;
	matchedNormalSubmitterSampleId: string | null;
	specimen: Specimen;
	donor: Donor;
};

export type Specimen = {
	specimenId: string;
	specimenType: string;
	submitterSpecimenId: string;
	tumourNormalDesignation: string;
	specimenTissueSource: string;
};

export type Donor = {
	donorId: string;
	submitterDonorId: string;
	gender: string;
};

export type UpdateAnalysesResult = {
	analysisDataId: string;
	success: boolean;
	error?: unknown;
};

export type Analysis = {
	id: string;
	study_id: string;
	type: string;
	state: string;
	analysis_schema_id: number;
	analysis_data_id: number;
	created_at: string;
	updated_at: string;
};

export type AnalysisData = Pick<Analysis, 'id' | 'analysis_data_id'>;

export type AnalysisSamplesMapping = Record<number, Sample[]>;
