DROP TYPE IF EXISTS gender CASCADE;
CREATE TYPE gender as ENUM('male','female','unspecified');
DROP TYPE IF EXISTS specimen_class CASCADE;
CREATE TYPE specimen_class as ENUM('Normal','Tumour','Adjacent normal');
DROP TYPE IF EXISTS specimen_type CASCADE;
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
DROP TYPE IF EXISTS sample_type CASCADE;
CREATE TYPE sample_type as ENUM('DNA','FFPE DNA','Amplified DNA','RNA','Total RNA','FFPE RNA');
DROP TYPE IF EXISTS file_type CASCADE;
CREATE TYPE file_type as ENUM('FASTA','FAI','FASTQ','BAM','BAI','VCF','TBI','IDX');
DROP TYPE IF EXISTS analysis_state CASCADE;
CREATE TYPE analysis_state as ENUM('Created','Uploaded','Published','Suppressed');
DROP TYPE IF EXISTS library_strategy CASCADE;
CREATE TYPE library_strategy as ENUM('WGS','WXS','RNA-Seq','ChIP-Seq','miRNA-Seq','Bisulfite-Seq','Validation','Amplicon','Other');
