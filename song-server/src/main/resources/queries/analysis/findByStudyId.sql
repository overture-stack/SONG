SELECT DISTINCT
  A.id,
  A.submitter_id,
  A.type,
  A.state,
  A.study_id,
  A.access
FROM Analysis A
WHERE
  A.study_id = :studyId