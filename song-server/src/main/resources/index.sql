---------------------------------------------------------------
--            Drop Indices
---------------------------------------------------------------
DROP INDEX IF EXISTS file_id_index;
DROP INDEX IF EXISTS file_analysis_id_uindex;
DROP INDEX IF EXISTS file_id_analysis_id_uindex;
DROP INDEX IF EXISTS file_study_id_uindex;
DROP INDEX IF EXISTS file_name_analysis_id_uindex;
DROP INDEX IF EXISTS sample_id_uindex;
DROP INDEX IF EXISTS sample_submitter_id_uindex;
DROP INDEX IF EXISTS sample_specimen_id_uindex;
DROP INDEX IF EXISTS sample_id_specimen_id_uindex;
DROP INDEX IF EXISTS sample_submitter_id_specimen_id_uindex;
DROP INDEX IF EXISTS donor_id_uindex;
DROP INDEX IF EXISTS donor_submitter_id_uindex;
DROP INDEX IF EXISTS donor_study_id_uindex;
DROP INDEX IF EXISTS donor_id_study_id_uindex;
DROP INDEX IF EXISTS donor_submitter_id_study_id_uindex;
DROP INDEX IF EXISTS specimen_id_uindex;
DROP INDEX IF EXISTS specimen_submitter_id_uindex;
DROP INDEX IF EXISTS specimen_donor_id_uindex;
DROP INDEX IF EXISTS specimen_id_donor_id_uindex;
DROP INDEX IF EXISTS specimen_submitter_id_donor_id_uindex;
DROP INDEX IF EXISTS analysis_id_uindex;
DROP INDEX IF EXISTS analysis_study_id_uindex;
DROP INDEX IF EXISTS analysis_id_study_id_uindex;
DROP INDEX IF EXISTS sampleset_sample_id_uindex;
DROP INDEX IF EXISTS sampleset_analysis_id_uindex;
DROP INDEX IF EXISTS sampleset_sample_id_analysis_id_uindex;
DROP INDEX IF EXISTS sequencingread_id_uindex;
DROP INDEX IF EXISTS variantcall_id_uindex;
DROP INDEX IF EXISTS study_id_uindex;
DROP INDEX IF EXISTS upload_id_uindex;
DROP INDEX IF EXISTS upload_study_id_analysis_id_uindex;
DROP INDEX IF EXISTS info_id_uindex;
DROP INDEX IF EXISTS info_id_type_uindex;
DROP INDEX IF EXISTS info_id_id_type_uindex;


---------------------------------------------------------------
--            Create Indices
---------------------------------------------------------------
CREATE UNIQUE INDEX file_id_index ON public.file (id);
CREATE INDEX file_analysis_id_uindex ON public.file (analysis_id);
CREATE UNIQUE INDEX file_id_analysis_id_uindex ON public.file (id, analysis_id);
CREATE INDEX file_study_id_uindex ON public.file (study_id);
CREATE INDEX file_name_analysis_id_uindex ON public.file (name, analysis_id);


CREATE UNIQUE INDEX sample_id_uindex ON public.sample (id);
CREATE INDEX sample_submitter_id_uindex ON public.sample (submitter_id);
CREATE INDEX sample_specimen_id_uindex ON public.sample (specimen_id);
CREATE UNIQUE INDEX sample_id_specimen_id_uindex ON public.sample (id, specimen_id);
CREATE UNIQUE INDEX sample_submitter_id_specimen_id_uindex ON public.sample (submitter_id, specimen_id);


CREATE UNIQUE INDEX donor_id_uindex ON public.donor (id);
CREATE INDEX donor_submitter_id_uindex ON public.donor (submitter_id);
CREATE INDEX donor_study_id_uindex ON public.donor (study_id);
CREATE UNIQUE INDEX donor_id_study_id_uindex ON public.donor (id, study_id);
CREATE UNIQUE INDEX donor_submitter_id_study_id_uindex ON public.donor (submitter_id, study_id);


CREATE UNIQUE INDEX specimen_id_uindex ON public.specimen (id);
CREATE INDEX specimen_submitter_id_uindex ON public.specimen (submitter_id);
CREATE INDEX specimen_donor_id_uindex ON public.specimen (donor_id);
CREATE UNIQUE INDEX specimen_id_donor_id_uindex ON public.specimen (id, donor_id);
CREATE UNIQUE INDEX specimen_submitter_id_donor_id_uindex ON public.specimen (submitter_id, donor_id);


CREATE UNIQUE INDEX analysis_id_uindex ON public.analysis (id);
CREATE INDEX analysis_study_id_uindex ON public.analysis (study_id);
CREATE UNIQUE INDEX analysis_id_study_id_uindex ON public.analysis (id, study_id);


CREATE INDEX sampleset_sample_id_uindex ON public.sampleset (sample_id);
CREATE INDEX sampleset_analysis_id_uindex ON public.sampleset (analysis_id);
CREATE INDEX sampleset_sample_id_analysis_id_uindex ON public.sampleset (sample_id,analysis_id);


CREATE UNIQUE INDEX sequencingread_id_uindex ON public.sequencingread (id);


CREATE UNIQUE INDEX variantcall_id_uindex ON public.variantcall (id);


CREATE UNIQUE INDEX study_id_uindex ON public.study (id);


CREATE UNIQUE INDEX upload_id_uindex ON public.upload (id);
CREATE INDEX upload_study_id_analysis_id_uindex ON public.upload (study_id, analysis_id);

-- Note: cannot be unique because id_type Analysis and SequencingRead can share the same id
CREATE INDEX info_id_uindex ON public.info (id);
CREATE INDEX info_id_type_uindex ON public.info (id_type);
CREATE UNIQUE INDEX info_id_id_type_uindex ON public.info (id, id_type);
-- CREATE INDEX info_info_uindex ON public.info (info);

