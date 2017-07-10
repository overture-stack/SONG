SELECT DISTINCT
  A.id,
  A.type,
  A.state,
  A.study_id,
  A.info
FROM Analysis A
WHERE
  A.study_id = :studyId