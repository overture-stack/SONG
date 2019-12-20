CREATE TYPE tumour_normal_designation_type as ENUM(
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
          'Cell line - derived from normal');

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
  '"Mononuclear cells from bone marrow"',
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

ALTER TABLE specimen ADD COLUMN tumour_normal_designation tumour_normal_designation_type;
ALTER TABLE specimen ADD COLUMN tissue_source tissue_source_type;

