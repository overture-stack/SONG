SELECT DISTINCT
  A.id,
  A.type,
  A.state,
  A.study_id
FROM Analysis A
WHERE
  A.study_id = :studyId