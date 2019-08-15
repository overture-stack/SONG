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

CREATE VIEW FullView AS
SELECT
    A.id as analysis_id,
    A.study_id as study_id,
    ANS.id as analysis_schema_id,
    ANS.name as analysis_schema_name,
    A.state as analysis_state,
    I_A.info as analysis_info,
    F.id as file_object_id,
    F.access as file_access,
    F.md5 as file_md5,
    F.name as file_name,
    F.size as file_size,
    F.type as file_type,
    I_F.info as file_info,
    SA.id as sample_id,
    SA.type as sample_type,
    SA.submitter_id as sample_submitter_id,
    I_SA.info as sample_info,
    SP.id as specimen_id,
    SP.type as specimen_type,
    SP.class as specimen_class,
    SP.submitter_id as specimen_submitter_id,
    I_SP.info as specimen_info,
    D.id as donor_id,
    D.gender as donor_gender,
    D.submitter_id as donor_submitter_id,
    I_D.info as donor_info,
    I_SR.info as sequencingread_info,
    I_VC.info as variantcall_info
FROM Analysis A
         left join analysis_schema ANS on A.analysis_schema_id = ANS.id
         left join Info I_A on I_A.id = A.id and I_A.id_type = 'Analysis'
         left join File F on F.analysis_id = A.id
         left join Info I_F on I_F.id = F.id and I_F.id_type = 'File'
         left join SampleSet SS on SS.analysis_id = A.id
         left join Sample SA on SA.id = SS.sample_id
         left join Info I_SA on I_SA.id = SA.id and I_SA.id_type = 'Sample'
         left join Specimen SP on SP.id = SA.specimen_id
         left join Info I_SP on I_SP.id = SP.id and I_SP.id_type = 'Specimen'
         left join Donor D on D.id = SP.donor_id
         left join Info I_D on I_D.id = D.id and I_D.id_type  = 'Donor'
         left join Info I_VC on I_VC.id = A.id and I_VC.id_type = 'VariantCall'
         left join Info I_SR on I_SR.id = A.id and I_SR.id_type = 'SequencingRead';
