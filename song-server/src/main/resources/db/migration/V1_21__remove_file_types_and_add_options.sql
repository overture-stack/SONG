
-- Remove the file_types column
ALTER TABLE public.analysis_schema
ADD COLUMN options jsonb;

UPDATE public.analysis_schema
SET options = jsonb_build_object(
    'fileTypes', file_types,
    'externalValidation', NULL
);
-- Remove the file_types column
ALTER TABLE public.analysis_schema
DROP COLUMN file_types;

