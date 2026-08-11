-- Donor/specimen/sample are no longer populated for submitted analyses (that data now lives
-- in the analysis_data JSONB blob), so the previous idview definition's inner-join chain through
-- donor -> specimen -> sample -> sampleset meant it could never match a currently-submitted
-- analysis. Redefine it to join file -> analysis -> analysis_schema directly.
drop view idview;

create view idview
            (analysis_id, analysis_schema_id, analysis_schema_name, analysis_state, study_id, object_id)
as
SELECT DISTINCT a.id     AS analysis_id,
                ans.id   AS analysis_schema_id,
                ans.name AS analysis_schema_name,
                a.state  AS analysis_state,
                a.study_id,
                f.id     AS object_id
FROM file f
         JOIN analysis a ON f.analysis_id::text = a.id::text
         JOIN analysis_schema ans ON a.analysis_schema_id = ans.id;
