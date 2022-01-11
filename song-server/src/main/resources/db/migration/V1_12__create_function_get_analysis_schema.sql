create or replace function get_analysis_schema (studyId character varying, analysisState analysis_state[], pageLimit integer, pageOffset integer )
returns table
(
	id character varying,
	analysis_schema_id integer,
	study_id character varying,
	state analysis_state,
	schema_id bigint,
	version integer,
	name character varying,
	schema jsonb,
	analysis_schema_created_at timestamp
) as
$$
begin
  return query

  SELECT *
  FROM ( SELECT
          analysis.id,
          analysis.analysis_schema_id,
          analysis.study_id,
          analysis.state
        FROM analysis
        WHERE analysis.study_id = studyId AND analysis.state = any(analysisState)
        ORDER BY analysis.id  ASC
        LIMIT pageLimit
        OFFSET pageOffset) AS filtered_analysis
  LEFT JOIN
      ( SELECT
            analysis_schema.id               AS schema_id,
            analysis_schema.version,
            analysis_schema.name,
            analysis_schema.schema,
            analysis_schema.created_at       AS analysis_schema_created_at
        FROM analysis_schema) AS schema
  ON filtered_analysis.analysis_schema_id = schema.schema_id
  ORDER BY filtered_analysis.id  ASC;

end;

$$
language plpgsql;

