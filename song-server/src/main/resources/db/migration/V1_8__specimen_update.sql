--------------------------------------
-- Update specimen_type and backup the old version as legacy
--------------------------------------
ALTER TYPE specimen_type RENAME TO legacy_specimen_type;

-- Legacy values are kept so they are not deleted. May or may not be used by the service
CREATE TYPE specimen_type as ENUM(
          'Normal',
          'Normal - tissue adjacent to primary tumour',
          'Primary tumour',
          'Primary tumour - adjacent to normal',
          'Primary tumour - additional new primary',
          'Recurrent tumour',
          'Metastatic tumour',
          'Metastatic tumour - metastasis local to lymph node',
          'Metastatic tumour - metastasis to distant location',
          'Metastatic tumour - additional metastatic',
          'Xenograft - derived from primary tumour',
          'Xenograft - derived from tumour cell line',
          'Cell line - derived from xenograft tumour',
          'Cell line - derived from tumour',
          'Cell line - derived from normal'
          );
ALTER TABLE specimen RENAME COLUMN type TO legacy_type;
ALTER TABLE specimen ADD COLUMN type specimen_type;
UPDATE specimen SET type=CAST(CAST(legacy_type AS VARCHAR) AS specimen_type);

--------------------------------------
-- Add new field called tissue_source with tissue_source_type enum
--------------------------------------
CREATE TYPE tissue_source_type as ENUM(
  'Blood derived',
  'Blood derived - bone marrow',
  'Blood derived - peripheral blood',
  'Bone marrow',
  'Buccal cell',
  'Lymph node',
  'Solid tissue',
  'Plasma',
  'Serum',
  'Urine',
  'Cerebrospinal fluid',
  'Sputum',
  'Other',
  'Pleural effusion',
  'Mononuclear cells from bone marrow',
  'Saliva',
  'Skin',
  'Intestine',
  'Buffy coat',
  'Stomach',
  'Esophagus',
  'Tonsil',
  'Spleen',
  'Bone',
  'Cerebellum',
  'Endometrium'
);
ALTER TABLE specimen ADD COLUMN tissue_source tissue_source_type;


--------------------------------------
-- Create tumour_normal_designation non_null field using enum and legacy values from class
--------------------------------------
CREATE TYPE tumour_normal_designation_type as ENUM(
          'Normal',
          'Tumour'
          );
ALTER TABLE specimen ADD COLUMN tumour_normal_designation tumour_normal_designation_type;
UPDATE specimen SET tumour_normal_designation='Normal' WHERE class='Normal';
UPDATE specimen SET tumour_normal_designation='Tumour' WHERE class='Tumour' OR class='Adjacent normal';
ALTER TABLE specimen ALTER COLUMN tumour_normal_designation SET NOT NULL;


----------------------------------------------
-- Add matched_normal_submitter_sample_id
----------------------------------------------
ALTER TABLE sample ADD COLUMN matched_normal_submitter_sample_id VARCHAR(255) ;

----------------------------------------------
-- Add non-null contraint to donor.gender
----------------------------------------------
ALTER TABLE donor ALTER COLUMN gender SET NOT NULL;

