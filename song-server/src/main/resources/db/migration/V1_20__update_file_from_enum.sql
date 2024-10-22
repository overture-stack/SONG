ALTER TABLE public.file
ALTER COLUMN type TYPE text USING type::text;

ALTER TABLE public.analysis_schema
ADD COLUMN file_types text[];