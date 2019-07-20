CREATE TABLE experiment (
    analysis_type            VARCHAR(255) PRIMARY KEY,
    schema                   json NOT NULL
);

CREATE TABLE analysis_type2 (
    id              BIGSERIAL PRIMARY KEY ,
    name            VARCHAR(225) NOT NULL,
    schema          jsonb
);
