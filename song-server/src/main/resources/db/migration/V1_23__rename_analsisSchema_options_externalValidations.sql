UPDATE analysis_schema
SET options = options - 'externalValidation' || jsonb_build_object('externalValidations', options->'externalValidation')
WHERE options ? 'externalValidation';