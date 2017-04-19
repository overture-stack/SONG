drop view details;
CREATE VIEW details AS 
    SELECT 
        T.name as StudyName,
        D.submitter_id as SubmitterDonorId, D.gender as DonorGender,
        P.submitter_id as SubmitterSpecimenId, P.class as SpecimenClass, P.type as SpecimenType, 
        A.submitter_id as SubmitterSampleId,  A.type as SampleType,
        F.name as FileName, F.type as FileType, F.size as FileSize 
    FROM Study T, Donor D, Specimen P, Sample A, File F 
    WHERE F.sample_id = A.id AND 
          A.specimen_id=P.id AND 
          P.donor_id = D.id AND
          D.study_id = T.id;
