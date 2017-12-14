CREATE VIEW full_table AS
SELECT
  a.study_id AS study_id,
  a.id AS analysis_id,
  a.type AS analysis_type,
  a.state AS analysis_state,
  d.id AS donor_id,
  d.submitter_id AS donor_submitter_id,
  d.gender AS donor_gender,
  sp.id AS specimen_id,
  sp.submitter_id AS specimen_submitter_id,
  sp.type AS specimen_type,
  sa.id AS sample_id,
  sa.submitter_id AS sample_submitter_id,
  sa.type AS sample_type,
  f.id AS file_id,
  f.type AS file_type,
  f.name AS file_name,
  f.access AS file_access,
  f.md5 AS file_md5,
  f.size AS file_size,
  u.id AS upload_id,
  u.state AS upload_state,
  u.errors AS upload_errors
FROM sample AS sa
  INNER JOIN specimen AS sp  ON sp.id = sa.specimen_id
  INNER JOIN donor AS d ON d.id = sp.donor_id
  INNER JOIN sampleset AS ss ON ss.sample_id = sa.id
  INNER JOIN analysis AS a ON a.id = ss.analysis_id
  INNER JOIN file AS f ON f.analysis_id = a.id
  INNER JOIN upload AS u ON u.analysis_id = a.id;
