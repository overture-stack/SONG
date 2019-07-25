CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE analysis_schema (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(225) NOT NULL,
    schema          jsonb NOT NULL
);
CREATE INDEX analysis_schema_name_index ON public.analysis_schema (name);
