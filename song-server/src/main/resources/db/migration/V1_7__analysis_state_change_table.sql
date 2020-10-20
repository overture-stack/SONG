------------------------------------------------------------------
-- Create table to record state changes
------------------------------------------------------------------
DROP TABLE IF EXISTS analysis_state_change CASCADE;
CREATE TABLE analysis_state_change (
  id            BIGSERIAL PRIMARY KEY,
  analysis_id   VARCHAR(36) references Analysis,
  initial_state ANALYSIS_STATE,
  updated_state ANALYSIS_STATE,
  updated_at    TIMESTAMP NOT NULL DEFAULT NOW()
);
CREATE INDEX analysis_state_change_analysis_index ON public.analysis_state_change (analysis_id);

------------------------------------------------------------------
-- Update Analysis to have a Created At value
------------------------------------------------------------------
ALTER TABLE Analysis ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT NOW();
ALTER TABLE Analysis ADD COLUMN updated_at TIMESTAMP NOT NULL DEFAULT NOW();

------------------------------------------------------------------
-- Add state history record for all currently published analysis
------------------------------------------------------------------
INSERT INTO analysis_state_change (analysis_id, initial_state, updated_state, updated_at)
SELECT id, 'UNPUBLISHED', 'PUBLISHED', updated_at FROM analysis WHERE state = 'PUBLISHED';