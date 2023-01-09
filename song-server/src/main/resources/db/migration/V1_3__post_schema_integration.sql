CREATE VIEW IdView AS
SELECT DISTINCT
    A.id as analysis_id,
    ANS.id as analysis_schema_id,
    ANS.name as analysis_schema_name,
    A.state as analysis_state,
    A.study_id as study_id,
    D.id as donor_id,
    SP.id as specimen_id,
    SA.id as sample_id,
    F.id as object_id
FROM Donor D
         INNER JOIN Specimen SP on D.id = SP.donor_id
         INNER JOIN Sample as SA on SP.id = SA.specimen_id
         INNER JOIN SampleSet as SAS on SA.id = SAS.sample_id
         INNER JOIN File as F on SAS.analysis_id = F.analysis_id
         INNER JOIN Analysis as A on SAS.analysis_id = A.id
         INNER JOIN analysis_schema ANS on A.analysis_schema_id = ANS.id;

