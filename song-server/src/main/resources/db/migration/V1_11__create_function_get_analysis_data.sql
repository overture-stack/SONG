create or replace function get_analysis_data (studyId character varying, analysisState analysis_state[], pageLimit integer, pageOffset integer)
returns table
(
	id character varying,
	analysis_data_id integer,
	study_id character varying,
	state analysis_state,
	data_id bigint,
	data jsonb
) as
$$
begin
  return query

  SELECT *
  FROM (    SELECT
              analysis.id,
              analysis.analysis_data_id,
              analysis.study_id,
              analysis.state
            FROM analysis
            WHERE analysis.study_id = studyId AND analysis.state = any(analysisState)
            ORDER BY analysis.id  ASC
            LIMIT pageLimit
            OFFSET pageOffset) AS filtered_analysis
        LEFT JOIN
          ( SELECT
            analysis_data.id               AS data_id,
            analysis_data.data
            FROM analysis_data ) AS analysisData
        ON filtered_analysis.analysis_data_id = analysisData.data_id
        ORDER BY filtered_analysis.id  ASC;

end;

$$
language plpgsql;

