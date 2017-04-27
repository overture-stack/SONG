DROP DOMAIN IF EXISTS gender;
CREATE DOMAIN gender as TEXT CHECK(VALUE IN('male','female','unknown'));
DROP DOMAIN IF EXISTS SPECIMEN_CLASS;
CREATE DOMAIN specimen_class as TEXT CHECK(VALUE IN('Normal','Tumour','Adjacent normal'));
DROP DOMAIN IF EXISTS specimen_type;
CREATE DOMAIN specimen_type as TEXT CHECK(VALUE IN('Normal-solid tissue',
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
    'Metastatic tumour - other', 'Cell line - derived from xenograft tumour'));
DROP DOMAIN IF EXISTS sample_type;
CREATE DOMAIN sample_type as TEXT CHECK(VALUE IN('DNA','FFPE DNA','Amplified DNA','RNA','Total RNA','FFPE RNA'));
DROP DOMAIN IF EXISTS file_type;
CREATE DOMAIN file_type as TEXT CHECK(VALUE IN('FASTA','FAI','FASTQ','BAM','BAI','VCF','TBI','IDX'));
DROP DOMAIN IF EXISTS analysis_state;
CREATE DOMAIN analysis_state as TEXT CHECK(VALUE IN('Created','Uploaded','Published','Suppressed'));
DROP DOMAIN IF EXISTS library_strategy;
CREATE DOMAIN library_strategy as TEXT CHECK(VALUE IN('WGS','WXS','RNA-Seq','ChIP-Seq','miRNA-Seq','Bisulfite-Seq','Validation','Amplicon','Other'));
