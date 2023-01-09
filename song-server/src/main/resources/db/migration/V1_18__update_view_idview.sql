create or replace view idview
            (analysis_id, analysis_schema_id, analysis_schema_name, analysis_state, study_id, donor_id, specimen_id,
             sample_id, object_id)
as
SELECT DISTINCT a.id     AS analysis_id,
                ans.id   AS analysis_schema_id,
                ans.name AS analysis_schema_name,
                a.state  AS analysis_state,
                a.study_id,
                d.id     AS donor_id,
                sp.id    AS specimen_id,
                sa.id    AS sample_id,
                f.id     AS object_id,
                sa.submitter_id AS submitter_sample_id,
                d.submitter_id AS submitter_donor_id,
                sp.submitter_id AS submitter_specimen_id
FROM donor d
         JOIN specimen sp ON d.id::text = sp.donor_id::text
         JOIN sample sa ON sp.id::text = sa.specimen_id::text
         JOIN sampleset sas ON sa.id::text = sas.sample_id::text
         JOIN file f ON sas.analysis_id::text = f.analysis_id::text
         JOIN analysis a ON sas.analysis_id::text = a.id::text
         JOIN analysis_schema ans ON a.analysis_schema_id = ans.id;


