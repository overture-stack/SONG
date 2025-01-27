--- Replace the get_analysis function with a new query that does not join with Sample/Specimen/Donors 
---   since those have been removed from the base schema

DROP FUNCTION get_analysis(character varying, analysis_state[], integer, integer);

CREATE OR REPLACE FUNCTION get_analysis(	studyId character varying,
																					analysisState analysis_state[],
																					pageLimit integer,
																					pageOffset integer
																				)

RETURNS TABLE
	(
		-- Analysis table
		id                 character varying,
		study_id           character varying,
		type               analysis_type,
		state              analysis_state,
		analysis_schema_id integer,
		analysis_data_id   integer,
		created_at         timestamp,
		updated_at         timestamp,
		-- File table
		file_id            character varying,
		file_analysis_id   character varying,
		file_study_id      character varying,
		name               text,
		size               bigint,
		md5                character,
		access             access_type,
		file_type          text,
		data_type          character varying,
		info               json
	)
AS
$$
	BEGIN
		RETURN QUERY
			SELECT *
			FROM (
				SELECT	analysis.id,
								analysis.study_id,
								analysis.type,
								analysis.state,
								analysis.analysis_schema_id,
								analysis.analysis_data_id,
								analysis.created_at,
								analysis.updated_at
				FROM analysis
				WHERE analysis.study_id = studyId AND analysis.state = ANY (analysisState)
				ORDER BY analysis.id ASC
				LIMIT pageLimit OFFSET pageOffset
			) AS filtered_analysis
			JOIN (
				SELECT 	filtered_file.id					AS file_id,
								filtered_file.analysis_id	AS file_analysis_id,
								filtered_file.study_id		AS file_study_id,
								filtered_file.name,
								filtered_file.size,
								filtered_file.md5,
								filtered_file.access,
								filtered_file.type				AS file_type,
								filtered_file.data_type,
								info.info
				FROM (
					SELECT * FROM file WHERE file.study_id = studyId
				) AS filtered_file
				JOIN info 
				ON filtered_file.id = info.id AND info.id_type = 'File'
			)
			AS file_info
			ON filtered_analysis.id = file_info.file_analysis_id;

	END;
$$
LANGUAGE plpgsql;
