insert into Study (id, name,description,organization) values ('ABC123','X1-CA','A fictional study', 'Sample Data Research Institute');
insert into Study (id, name,description,organization) values ('XYZ234','X2-CA','A new study', 'Sample Data Research Institute');
insert into Info(id, id_type, info) values ('ABC123',  'Study', '{"name":"study1"}' );
insert into Info(id, id_type, info) values ('XYZ234',  'Study', '{"name":"study2"}' );

insert into Donor (id, study_id, submitter_id, gender) values ('DO1','ABC123', 'Subject-X23Alpha7', 'Male');
insert into Donor (id, study_id, submitter_id, gender) values ('DO2','XYZ234', 'Zalgon26', 'Other');
insert into Info(id, id_type, info) values ('DO1',  'Donor', '{"name":"donor1"}' );
insert into Info(id, id_type, info) values ('DO2',  'Donor', '{"name":"donor2"}' );

insert into Specimen (id, donor_id, submitter_id, class, type) values ('SP1','DO1','Tissue-Culture 284 Gamma 3', 'Tumour', 'Recurrent tumour - solid tissue');
insert into Specimen (id, donor_id, submitter_id, class, type) values ('SP2','DO1','Tissue-Culture 285 Gamma 7', 'Normal', 'Normal - other');
insert into Info(id, id_type, info) values ('SP1',  'Specimen', '{"name":"specimen1"}' );
insert into Info(id, id_type, info) values ('SP2',  'Specimen', '{"name":"specimen2"}' );

insert into Sample (id, specimen_id, submitter_id, type) values ('SA1', 'SP1', 'T285-G7-A5','DNA');
insert into Sample (id, specimen_id, submitter_id, type) values ('SA11', 'SP1', 'T285-G7-B9','DNA');
insert into Sample (id, specimen_id, submitter_id, type) values ('SA21', 'SP2', 'T285-G7N','DNA');
insert into Info(id, id_type, info) values ('SA1',  'Sample', '{"name":"sample1"}' );
insert into Info(id, id_type, info) values ('SA11', 'Sample', '{"name":"sample2"}' );
insert into Info(id, id_type, info) values ('SA21', 'Sample', '{"name":"sample3"}' );


insert into Analysis(id, study_id, state, type, analysis_schema_id) values('AN1','ABC123', 'UNPUBLISHED', 'variantCall', 1);
insert into Info(id, id_type, info) values ('AN1',  'Analysis',       '{"name":"analysis1"}' );
insert into Info(id, id_type, info) values ('AN1',  'VariantCall', '{"name":"variantCall1"}' );

insert into Analysis(id, study_id, state, type, analysis_schema_id) values ('AN2','ABC123', 'UNPUBLISHED','sequencingRead', 2);
insert into Info(id, id_type, info) values ('AN2',  'Analysis', '{"name":"analysis2"}' );
insert into Info(id, id_type, info) values ('AN2',  'SequencingRead', '{"name":"sequencingRead2"}' );


insert into SampleSet(analysis_id, sample_id) values ('AN2','SA1');
insert into SampleSet(analysis_id, sample_id) values ('AN1', 'SA11');
insert into SampleSet(analysis_id, sample_id) values ('AN1', 'SA21');

insert into File (id, analysis_id, study_id, name, size, type, md5, access, data_type) values ('FI1', 'AN1', 'ABC123', 'ABC-TC285G7-A5-ae3458712345.bam', 122333444455555, 'BAM', '20de2982390c60e33452bf8736c3a9f1', 'open', 'DATA_TYPE_BAM');
insert into File (id, analysis_id, study_id, name, size, type, md5, access, data_type) values ('FI2', 'AN1', 'ABC123', 'ABC-TC285G7-A5-wleazprt453.bai', 123456789, 'BAI', '53ae1343e3ae333ac24c5a2e6279a21d', 'controlled', 'DATA_TYPE_BAI');
insert into File(id, analysis_id, study_id, name, size, type, md5, access, data_type) values ('FI3', 'AN2', 'ABC123', 'ABC-TC285-G7-B9-kthx12345.bai', 23456789, 'BAI', '0f41f4e4619e5731447432d101bcfb34', 'controlled', 'DATA_TYPE_BAI');
insert into File(id, analysis_id, study_id, name, size, type, md5, access, data_type) values ('FI4', 'AN2', 'ABC123', 'ABC-TC285-G7N-alpha12345.fai', 12345, 'FAI', '1ad22383391004fd12441f39ba7f2380', 'controlled', 'DATA_TYPE_FAI');

insert into Info(id, id_type, info) values ('FI1', 'File', '{"name":"file1"}' );
insert into Info(id, id_type, info) values ('FI2', 'File', '{"name":"file2"}' );
insert into Info(id, id_type, info) values ('FI3', 'File', '{"name":"file3"}' );
insert into Info(id, id_type, info) values ('FI4', 'File', '{"name":"file4"}' );
