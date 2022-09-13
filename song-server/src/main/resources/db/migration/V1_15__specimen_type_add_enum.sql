ALTER TYPE specimen_type RENAME TO _specimen_type;
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
          'Cell line - derived from normal',
          'Tumour - unknown if derived from primary or metastatic');
ALTER TABLE specimen RENAME COLUMN type TO _type;
ALTER TABLE specimen ADD COLUMN type specimen_type;

UPDATE specimen SET type='Normal'                                               where _type='Normal';
UPDATE specimen SET type='Normal - tissue adjacent to primary tumour'           where _type='Normal - tissue adjacent to primary tumour';
UPDATE specimen SET type='Primary tumour'                                       where _type='Primary tumour';
UPDATE specimen SET type='Primary tumour - adjacent to normal'                  where _type='Primary tumour - adjacent to normal';
UPDATE specimen SET type='Primary tumour - additional new primary'              where _type='Primary tumour - additional new primary';
UPDATE specimen SET type='Recurrent tumour'                                     where _type='Recurrent tumour';
UPDATE specimen SET type='Metastatic tumour'                                    where _type='Metastatic tumour';
UPDATE specimen SET type='Metastatic tumour - metastasis local to lymph node'   where _type='Metastatic tumour - metastasis local to lymph node';
UPDATE specimen SET type='Metastatic tumour - metastasis to distant location'   where _type='Metastatic tumour - metastasis to distant location';
UPDATE specimen SET type='Metastatic tumour - additional metastatic'            where _type='Metastatic tumour - additional metastatic';
UPDATE specimen SET type='Xenograft - derived from primary tumour'              where _type='Xenograft - derived from primary tumour';
UPDATE specimen SET type='Xenograft - derived from tumour cell line'            where _type='Xenograft - derived from tumour cell line';
UPDATE specimen SET type='Cell line - derived from xenograft tumour'            where _type='Cell line - derived from xenograft tumour';
UPDATE specimen SET type='Cell line - derived from tumour'                      where _type='Cell line - derived from tumour';
UPDATE specimen SET type='Cell line - derived from normal'                      where _type='Cell line - derived from normal';

ALTER TABLE specimen DROP COLUMN _type;
DROP TYPE _specimen_type CASCADE;
