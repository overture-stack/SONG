CREATE TABLE analysis_schema (
    id              BIGSERIAL PRIMARY KEY ,
    name            VARCHAR(225) NOT NULL,
    schema          jsonb
);
