ALTER TYPE sample_type RENAME TO _sample_type;

-- Legacy values are kept so they are not deleted. May or mayn not be used by the service
CREATE TYPE sample_type as ENUM(
    'Total DNA',
    'Amplified DNA',
    'ctDNA',
    'Other DNA enrichments',
    'Total RNA',
    'Ribo-Zero RNA',
    'polyA+ RNA',
    'Other RNA fractions',
    'DNA', -- Legacy
    'FFPE DNA', -- Legacy
    'RNA', -- Legacy
    'FFPE RNA' -- Legacy
);
ALTER TABLE sample RENAME COLUMN type TO _type;
ALTER TABLE sample ADD COLUMN type sample_type;

UPDATE sample SET type=CAST(CAST(_type AS VARCHAR) AS sample_type);

ALTER TABLE sample DROP COLUMN _type;
DROP TYPE _sample_type CASCADE;
