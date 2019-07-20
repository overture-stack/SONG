CREATE TABLE experiment (
    analysis_type            VARCHAR(255) PRIMARY KEY,
    schema                   json NOT NULL
);

CREATE TABLE analysis_type2 (
    id              UUID PRIMARY KEY,
    name            VARCHAR(225) NOT NULL,
    version         BIGSERIAL,
    schema          jsonb
);
