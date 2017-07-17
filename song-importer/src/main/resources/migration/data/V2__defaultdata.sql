insert into Study (id, name,description,organization) values ('ABC123','X1-CA','A fictional study', 'Sample Data Research Institute');
insert into Study (id, name,description,organization) values ('XYZ234','X2-CA','A new study', 'Sample Data Research Institute');
insert into Donor (id, study_id, submitter_id, gender) values ('DO1','ABC123', 'Subject-X23Alpha7', 'male');
insert into Donor (id, study_id, submitter_id, gender) values ('DO2','XYZ234', 'Zalgon26', 'unspecified');
insert into Specimen (id, donor_id, submitter_id, class, type) values ('SP1','DO1','Tissue-Culture 284 Gamma 3', 'Tumour', 'Recurrent tumour - solid tissue');
insert into Specimen (id, donor_id, submitter_id, class, type) values ('SP2','DO1','Tissue-Culture 285 Gamma 7', 'Normal', 'Normal - other');
insert into Sample (id, specimen_id, submitter_id, type) values ('SA1', 'SP1', 'T285-G7-A5','DNA');
insert into Sample (id, specimen_id, submitter_id, type) values ('SA11', 'SP1', 'T285-G7-B9','DNA');
insert into Sample (id, specimen_id, submitter_id, type) values ('SA21', 'SP2', 'T285-G7N','DNA');
insert into File (id, study_id, name, size, type, md5, info) values ('FI1', 'ABC123', 'ABC-TC285G7-A5-ae3458712345.bam', 122333444455555, 'BAM', '20de2982390c60e33452bf8736c3a9f1', '<XML>Not even well-formed <XML></XML>');
insert into File (id, study_id, name, size, type, md5, info) values ('FI2', 'ABC123', 'ABC-TC285G7-A5-wleazprt453.bai', 123456789, 'BAI', '53ae1343e3ae333ac24c5a2e6279a21d', '<XML>Not even well-formed<XML></XML>');
insert into File(id, study_id, name, size, type, md5, info) values ('FI3', 'ABC123', 'ABC-TC285-G7-B9-kthx12345.bai', 23456789, 'BAI', '0f41f4e4619e5731447432d101bcfb34', '<XML><Status>Inconclusive</Status></XML>');
insert into File(id, study_id, name, size, type, md5, info) values ('FI4', 'ABC123', 'ABC-TC285-G7N-alpha12345.fai', 12345, 'FAI', '1ad22383391004fd12441f39ba7f2380', '<XML></XML>');


insert into Analysis(id, study_id, state, type) values('AN1','ABC123', 'UNPUBLISHED', 'variantCall');
insert into VariantCall(id, variant_calling_tool) values ('AN1','SuperNewVariantCallingTool');
insert into FileSet(analysis_id, file_id) values ('AN1', 'FI1'),('AN1','FI2');


insert into Analysis(id, study_id, state, type) values ('AN2','ABC123', 'UNPUBLISHED','sequencingRead');
insert into SequencingRead (id, library_strategy, paired_end, insert_size, aligned, alignment_tool, reference_genome) values ('AN2','Other', TRUE, 12345, TRUE, 'BigWrench', 'hg19');
insert into FileSet(analysis_id, file_id) values ('AN2', 'FI1'),('AN2','FI3');

insert into Analysis(id, study_id, state, type) values ('MU1', 'ABC123', 'SUPPRESSED', 'MAF');
insert into SampleSet(analysis_id, sample_id) values ('AN2','SA1');
insert into SampleSet(analysis_id, sample_id) values ('AN1', 'SA11');
insert into SampleSet(analysis_id, sample_id) values ('AN1', 'SA21');
insert into FileSet(analysis_id, file_id) values ('MU1','FI1'),('MU1','FI2'),('MU1','FI3');