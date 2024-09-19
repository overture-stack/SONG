CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE analysis_schema (
    id              BIGSERIAL PRIMARY KEY,
    version         INTEGER,
    name            VARCHAR(225) NOT NULL,
    schema          jsonb NOT NULL,
    file_types      VARCHAR(225)
);
CREATE INDEX analysis_schema_name_index ON public.analysis_schema (name);
CREATE INDEX analysis_schema_version_index ON public.analysis_schema (version);
CREATE INDEX analysis_schema_name_version_index ON public.analysis_schema (name,version);
