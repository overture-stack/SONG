create or replace function get_analysis_state_change(studyId character varying, analysisState analysis_state[], pageLimit integer, pageOffset integer )
returns table (
	id character varying, study_id character varying, type analysis_type, state analysis_state,
	analysis_schema_id integer, analysis_data_id integer, created_at timestamp,updated_at timestamp,
	analysis_state_change_id bigint, analysis_id character varying, initial_state analysis_state,
	updated_state analysis_state, state_updated_at timestamp
) as
$$
	begin
	return query

	SELECT *
	FROM    ( SELECT * FROM analysis
     		 WHERE analysis.study_id = studyId AND analysis.state =  ANY(analysisState)
		     ORDER BY analysis.id  ASC
			 LIMIT pageLimit
		     OFFSET pageOffset) AS filtered_analysis
	LEFT JOIN ( SELECT
					analysis_state_change.id               AS analysis_state_change_id,
					analysis_state_change.analysis_id,
					analysis_state_change.initial_state,
					analysis_state_change.updated_state,
					analysis_state_change.updated_at       AS state_updated_at
		        FROM analysis_state_change
				ORDER BY analysis_state_change.analysis_id ASC ) AS history
	ON filtered_analysis.id = history.analysis_id;

end;

$$
language plpgsql;