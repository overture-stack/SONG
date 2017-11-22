SELECT DISTINCT
  A.id,
  A.type,
  A.state,
  A.study_id
FROM Donor D
  INNER JOIN Specimen SP on D.id = SP.donor_id
  INNER JOIN Sample as SA on SP.id = SA.specimen_id
  INNER JOIN SampleSet as SAS on SA.id = SAS.sample_id
  INNER JOIN File as F on SAS.analysis_id = F.analysis_id
  INNER JOIN Analysis as A on SAS.analysis_id = A.id
WHERE D.study_id = :studyId
      AND D.id  ~* :donorId
      AND SP.id ~* :specimenId
      AND SA.id ~* :sampleId
      AND F.id  ~* :fileId;
