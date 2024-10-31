
-- Remove the file_types column
ALTER TABLE public.analysis_schema
DROP COLUMN file_types;

-- Add the options column as jsonb (nullable by default)
ALTER TABLE public.analysis_schema
ADD COLUMN options jsonb;