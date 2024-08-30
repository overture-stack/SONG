-- This migration updates the valid file types for legacy analysis types
-- (sequencing-experiment and variant-calling) to be specific to those types.

BEGIN;

-- Update sequencing-experiment analysis type
UPDATE analysis_table
SET fileType = 'BAM', 'FASTQ', 'BAI', 'CRAM', 'CRAI'
WHERE analysisType = 'sequencing-experiment';

-- Update variant-calling analysis type
UPDATE analysis_table
SET fileType = 'VCF', 'TBI'
WHERE analysisType = 'variant-calling';

COMMIT;