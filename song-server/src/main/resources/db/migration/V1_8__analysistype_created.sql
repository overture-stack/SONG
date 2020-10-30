------------------------------------------------------------------
-- Update AnalysisType to have a Created At value
------------------------------------------------------------------
ALTER TABLE analysis_schema ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT NOW();
