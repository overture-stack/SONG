CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE analysis_schema (
    order_id        BIGSERIAL PRIMARY KEY,
    id              UUID NOT NULL UNIQUE DEFAULT uuid_generate_v4() ,
    name            VARCHAR(225) NOT NULL,
    schema          jsonb
);
CREATE UNIQUE INDEX analysis_schema_id_index ON public.analysis_schema (id);
CREATE INDEX analysis_schema_name_index ON public.analysis_schema (name);
CREATE UNIQUE INDEX analysis_schema_order_id_index ON public.analysis_schema (order_id);
CREATE UNIQUE INDEX analysis_schema_order_id_name_index ON public.analysis_schema (order_id, name);
