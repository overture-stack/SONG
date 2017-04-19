drop table IF EXISTS Study, Donor, Specimen, Sample, File, VariantCallAnalysis, VariantCallFileSet, SequencingReadAnalysis, SequencingReadFileSet, MAFAnalysis, MAFFileSet;

CREATE TABLE Study (id VARCHAR(36) PRIMARY KEY, name TEXT, description TEXT, organization TEXT, info TEXT);
CREATE TABLE Donor (id VARCHAR(16) PRIMARY KEY, study_id VARCHAR(36) references Study, submitter_id TEXT, gender TEXT, info TEXT);
CREATE TABLE Specimen (id VARCHAR(16) PRIMARY KEY, donor_id VARCHAR(16) references Donor, submitter_id TEXT, class TEXT, type TEXT, info TEXT);
CREATE TABLE Sample(id VARCHAR(16) PRIMARY KEY, specimen_id VARCHAR(16) references Specimen, submitter_id TEXT, type TEXT, info TEXT);
CREATE TABLE File(id VARCHAR(36) PRIMARY KEY, sample_id VARCHAR(36) references Sample, name TEXT, size BIGINT, md5sum CHAR(32), type TEXT, metadata_doc TEXT, info TEXT);
CREATE TABLE VariantCallAnalysis (id VARCHAR(36) PRIMARY KEY, study_id VARCHAR(36) references Study, state TEXT, variant_calling_tool TEXT, info TEXT);
CREATE TABLE VariantCallFileSet (id VARCHAR(16) PRIMARY KEY, analysis_id VARCHAR(36) references VariantCallAnalysis, file_id VARCHAR(36) references File);
CREATE TABLE SequencingReadAnalysis (id VARCHAR(36) PRIMARY KEY, study_id VARCHAR(36) references Study, state TEXT, library_strategy TEXT, paired_end BOOLEAN, insert_size BIGINT, aligned BOOLEAN, alignment_tool TEXT, reference TEXT, info TEXT);
CREATE TABLE SequencingReadFileSet (id VARCHAR(16) PRIMARY KEY, analysis_id VARCHAR(36) references SequencingReadAnalysis, file_id VARCHAR(36) references File);
CREATE TABLE MAFAnalysis (id VARCHAR(36) PRIMARY KEY, study_id VARCHAR(36) references Study, info TEXT);
CREATE TABLE MAFFileSet(id VARCHAR(36) PRIMARY KEY, analysis_id VARCHAR(36) references MAFAnalysis, file_id VARCHAR(36) references File);
