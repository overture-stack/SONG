SELECT DISTINCT
  A.id,
  A.submitter_id,
  A.type,
  A.state,
  A.study_id,
  A.info
FROM Donor D
INNER JOIN Specimen SP on D.id = SP.donor_id
INNER JOIN Sample as SA on SP.id = SA.specimen_id
INNER JOIN SampleSet as SAS on SA.id = SAS.sample_id
INNER JOIN File as F on SAS.analysis_id = F.analysis_id
INNER JOIN Analysis as A on SAS.analysis_id = A.id
WHERE D.study_id = :studyId
      AND D.id LIKE :donorId
      AND SP.id LIKE :specimenId
      AND SA.id LIKE :sampleId
      AND F.id LIKE :fileId;
