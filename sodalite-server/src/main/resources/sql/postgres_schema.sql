CREATE TYPE gender as ENUM('Male','Female','Unknown');
CREATE TYPE specimen_class as ENUM('Normal','Tumour','Adjacent normal');
CREATE TYPE specimen_type as ENUM('Normal-solid tissue',
    'Normal - blood derived', 'Normal - bone marrow',
    'Normal - tissue adjacent to primary', 'Normal - buccal cell',
    'Normal - EBV immortalized', 'Normal - lymph node', 'Normal - other',
    'Primary tumour - solid tissue',
    'Primary tumour - blood derived (peripheral blood)',
    'Primary tumour - blood derived (bone marrow)',
    'Primary tumour - additional new primary',
    'Primary tumour - other', 'Recurrent tumour - solid tissue',
    'Recurrent tumour - blood derived (peripheral blood)',
    'Recurrent tumour - blood derived (bone marrow)',
    'Recurrent tumour - other', 'Metastatic tumour - NOS',
    'Metastatic tumour - lymph node',
    'Metastatic tumour - metastasis local to lymph node',
    'Metastatic tumour - metastasis to distant location',
    'Metastatic tumour - additional metastatic',
    'Xenograft - derived from primary tumour',
    'Xenograft - derived from tumour cell line',
    'Cell line - derived from tumour', 'Primary tumour - lymph node',
    'Metastatic tumour - other', 'Cell line - derived from xenograft tumour');
CREATE TYPE sample_type as ENUM('DNA','FFPE DNA','Amplified DNA','RNA','Total RNA','FFPE RNA');
create TYPE file_type as ENUM('FASTA','FAI','FASTQ','BAM','BAI','VCF','TBI');
CREATE TYPE analysis_state as ENUM('Created','Uploaded','Published','Suppressed');
CREATE TYPE library_strategy as ENUM('WGS','WXS','RNA-Seq','ChIP-Seq','miRNA-Seq','Bisulfite-Seq','Validation','Amplicon','Other');

CREATE TABLE IF NOT EXISTS Study (id VARCHAR(36) PRIMARY KEY, name TEXT, description TEXT, organization TEXT, info TEXT);
CREATE TABLE IF NOT EXISTS Donor (id VARCHAR(16) PRIMARY KEY, study_id VARCHAR(36) references Study, submitter_id TEXT, gender GENDER, info TEXT);
CREATE TABLE IF NOT EXISTS Specimen (id VARCHAR(16) PRIMARY KEY, donor_id VARCHAR(16) references Donor, submitter_id TEXT, class SPECIMEN_CLASS, type SPECIMEN_TYPE, info TEXT);
CREATE TABLE IF NOT EXISTS Sample(id VARCHAR(16) PRIMARY KEY, specimen_id VARCHAR(16) references Specimen, submitter_id TEXT, type SAMPLE_TYPE, info TEXT);
CREATE TABLE IF NOT EXISTS File(id VARCHAR(36) PRIMARY KEY, sample_id VARCHAR(36) references Sample, name TEXT, size BIGINT, md5sum CHAR(32), type FILE_TYPE, metadata_doc TEXT, info TEXT);
CREATE TABLE IF NOT EXISTS VariantCallAnalysis (id VARCHAR(36) PRIMARY KEY, study_id VARCHAR(36) references Study, state ANALYSIS_STATE, variant_calling_tool TEXT, info TEXT);
CREATE TABLE IF NOT EXISTS VariantCallFileSet (id VARCHAR(16) PRIMARY KEY, analysis_id VARCHAR(36) references VariantCallAnalysis, file_id VARCHAR(36) references File);
CREATE TABLE IF NOT EXISTS SequencingReadAnalysis (id VARCHAR(36) PRIMARY KEY, study_id VARCHAR(36) references Study, state ANALYSIS_STATE, library_strategy LIBRARY_STRATEGY, paired_end BOOLEAN, insert_size BIGINT, aligned BOOLEAN, alignment_tool TEXT, reference TEXT, info TEXT);
CREATE TABLE IF NOT EXISTS SequencingReadFileSet (id VARCHAR(16) PRIMARY KEY, analysis_id VARCHAR(36) references SequencingReadAnalysis, file_id VARCHAR(36) references File);
CREATE TABLE IF NOT EXISTS MAFAnalysis (id VARCHAR(36) PRIMARY KEY, study_id VARCHAR(36) references Study, info TEXT);
CREATE TABLE IF NOT EXISTS MAFFileSet(id VARCHAR(36) PRIMARY KEY, analysis_id VARCHAR(36) references MAFAnalysis, file_id VARCHAR(36) references File);
