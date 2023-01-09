ALTER TYPE file_type RENAME TO _file_type;
CREATE TYPE file_type as ENUM('FASTA','FAI','FASTQ','BAM','BAI','VCF','TBI','IDX','XML','TGZ','CRAM','CRAI');
ALTER TABLE file RENAME COLUMN type TO _type;
ALTER TABLE file ADD COLUMN type file_type;

UPDATE file SET type='FASTA' where _type='FASTA';
UPDATE file SET type='FAI'   where _type='FAI';
UPDATE file SET type='FASTQ' where _type='FASTQ';
UPDATE file SET type='BAM'   where _type='BAM';
UPDATE file SET type='BAI'   where _type='BAI';
UPDATE file SET type='VCF'   where _type='VCF';
UPDATE file SET type='TBI'   where _type='TBI';
UPDATE file SET type='IDX'   where _type='IDX';
UPDATE file SET type='XML'   where _type='XML';
UPDATE file SET type='TGZ'   where _type='TGZ';

ALTER TABLE file DROP COLUMN _type;
DROP TYPE _file_type CASCADE;
