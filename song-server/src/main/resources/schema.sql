/*
 * Copyright (c) 2017 The Ontario Institute for Cancer Research. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the GNU Public License v3.0.
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
 * SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER
 * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

------------------------------------------------------------------------
---                    Drop and Create Types
------------------------------------------------------------------------
DROP TYPE IF EXISTS gender CASCADE;
CREATE TYPE gender as ENUM('male','female','unspecified');
DROP TYPE IF EXISTS specimen_class CASCADE;
CREATE TYPE specimen_class as ENUM('Normal','Tumour','Adjacent normal');
DROP TYPE IF EXISTS specimen_type CASCADE;
CREATE TYPE specimen_type as ENUM(
	'Normal - solid tissue',
    'Normal - blood derived', 'Normal - bone marrow',
    'Normal - tissue adjacent to primary', 'Normal - buccal cell',
    'Normal - EBV immortalized', 'Normal - lymph node', 'Normal - other',
    'Primary tumour',
    'Primary tumour - solid tissue',
    'Primary tumour - blood derived (peripheral blood)',
    'Primary tumour - blood derived (bone marrow)',
    'Primary tumour - additional new primary',
    'Primary tumour - other', 'Recurrent tumour - solid tissue',
    'Recurrent tumour - blood derived (peripheral blood)',
    'Recurrent tumour - blood derived (bone marrow)',
    'Recurrent tumour - other', 'Metastatic tumour - NOS',
    'Metastatic tumour - lymph node',
    'Metastatic tumour - metastasis local to lymph node',
    'Metastatic tumour - metastasis to distant location',
    'Metastatic tumour - additional metastatic',
    'Xenograft - derived from primary tumour',
    'Xenograft - derived from tumour cell line',
    'Cell line - derived from tumour', 'Primary tumour - lymph node',
    'Metastatic tumour - other', 'Cell line - derived from xenograft tumour');
DROP TYPE IF EXISTS sample_type CASCADE;
CREATE TYPE sample_type as ENUM('DNA','FFPE DNA','Amplified DNA','RNA','Total RNA','FFPE RNA');
DROP TYPE IF EXISTS file_type CASCADE;
CREATE TYPE file_type as ENUM('FASTA','FAI','FASTQ','BAM','BAI','VCF','TBI','IDX', 'XML');
DROP TYPE IF EXISTS analysis_state CASCADE;
CREATE TYPE analysis_state as ENUM('PUBLISHED', 'UNPUBLISHED', 'SUPPRESSED');
DROP TYPE IF EXISTS library_strategy CASCADE;
CREATE TYPE library_strategy as ENUM('WGS','WXS','RNA-Seq','ChIP-Seq','miRNA-Seq','Bisulfite-Seq','Validation',
    'Amplicon','Other');

DROP TYPE IF EXISTS analysis_type CASCADE;
CREATE TYPE analysis_type as ENUM('sequencingRead','variantCall','MAF');

DROP TYPE IF EXISTS access_type CASCADE;
CREATE TYPE access_type as ENUM('controlled','open');

DROP TYPE IF EXISTS id_type CASCADE;
CREATE TYPE id_type as ENUM('Study','Donor','Specimen','Sample','File','Analysis','SequencingRead','VariantCall');

------------------------------------------------------------------------
---                    Drop and Create Tables
------------------------------------------------------------------------
DROP TABLE IF EXISTS Study CASCADE;
CREATE TABLE Study (
  id            VARCHAR(36) PRIMARY KEY,
  name          TEXT,
  description   TEXT,
  organization  TEXT
);

DROP TABLE IF EXISTS Donor CASCADE;
CREATE TABLE Donor (
  id            VARCHAR(36) PRIMARY KEY,
  study_id      VARCHAR(36) references Study,
  submitter_id  TEXT,
  gender        GENDER
);

DROP TABLE IF EXISTS Specimen CASCADE;
CREATE TABLE Specimen (
  id            VARCHAR(36) PRIMARY KEY,
  donor_id      VARCHAR(36) references Donor,
  submitter_id  TEXT,
  class         SPECIMEN_CLASS,
  type          SPECIMEN_TYPE
);

DROP TABLE IF EXISTS Sample CASCADE;
CREATE TABLE Sample (
  id            VARCHAR(36) PRIMARY KEY,
  specimen_id   VARCHAR(36) references Specimen,
  submitter_id  TEXT,
  type          SAMPLE_TYPE
);

DROP TABLE IF EXISTS Analysis CASCADE;
CREATE TABLE Analysis (
  id            VARCHAR(36) PRIMARY KEY,
  study_id      VARCHAR(36) references Study,
  type          ANALYSIS_TYPE,
  state         ANALYSIS_STATE
);

DROP TABLE IF EXISTS File CASCADE;
CREATE TABLE File (
  id            VARCHAR(36) PRIMARY KEY,
  analysis_id   VARCHAR(36) references Analysis,
  study_id      VARCHAR(36) references Study,
  name          TEXT,
  size          BIGINT,
  md5           CHAR(32),
  type          FILE_TYPE,
  access        ACCESS_TYPE
);

DROP TABLE IF EXISTS SampleSet CASCADE;
CREATE TABLE SampleSet (
  analysis_id   VARCHAR(36) references Analysis,
  sample_id     VARCHAR(36) references Sample
);

DROP TABLE IF EXISTS SequencingRead CASCADE;
CREATE TABLE SequencingRead (
  id                VARCHAR(36) references Analysis,
  library_strategy  LIBRARY_STRATEGY,
  paired_end        BOOLEAN,
  insert_size       BIGINT,
  aligned           BOOLEAN,
  alignment_tool    TEXT,
  reference_genome  TEXT
);

DROP TABLE IF EXISTS VariantCall CASCADE;
CREATE TABLE VariantCall (
  id                                  VARCHAR(36) references Analysis,
  variant_calling_tool                TEXT,
  tumour_sample_submitter_id          TEXT,
  matched_normal_sample_submitter_id  TEXT
);

DROP TABLE IF EXISTS Upload CASCADE;
CREATE TABLE Upload (
  id              VARCHAR(40) PRIMARY KEY,
  study_id        VARCHAR(36) references Study,
  analysis_id     TEXT,
  state           VARCHAR(50),
  errors          TEXT,
  payload         TEXT,
  created_at      TIMESTAMP NOT NULL DEFAULT now(),
  updated_at      TIMESTAMP NOT NULL DEFAULT now()
);

DROP TABLE IF EXISTS Info;
CREATE TABLE Info (
  id              VARCHAR(36),
  id_type         id_type,
  info            json
);


DROP VIEW IF EXISTS BusinessKeyView;
CREATE VIEW BusinessKeyView AS
  SELECT
    S.id as study_id,
    SP.id AS specimen_id,
    SP.submitter_id AS specimen_submitter_id,
    SA.id AS sample_id,
    SA.submitter_id AS sample_submitter_id
  FROM Study S
    INNER JOIN Donor D ON S.id = D.study_id
    INNER JOIN Specimen SP ON D.id = SP.donor_id
    INNER JOIN Sample SA ON SP.id = SA.specimen_id;

DROP VIEW IF EXISTS InfoView;
CREATE VIEW InfoView AS
  SELECT
    A.id as analysis_id,
    I_STUDY.info as study_info,
    I_DONOR.info as donor_info,
    I_SP.info as specimen_info,
    I_SA.info as sample_info,
    I_A.info as analysis_info,
    I_F.info as file_info
    FROM Study S
      INNER JOIN Info I_STUDY ON I_STUDY.id = S.id and I_STUDY.id_type = 'Study'
      INNER JOIN Donor D ON S.id = D.study_id
      INNER JOIN Info I_DONOR ON I_DONOR.id = D.id and I_DONOR.id_type = 'Donor'
      INNER JOIN Specimen SP ON D.id = SP.donor_id
      INNER JOIN Info I_SP ON I_SP.id = SP.id and I_SP.id_type = 'Specimen'
      INNER JOIN Sample SA ON SP.id = SA.specimen_id
      INNER JOIN Info I_SA ON I_SA.id = SA.id and I_SA.id_type = 'Sample'
      INNER JOIN SampleSet SS on SA.id = SS.sample_id
      INNER JOIN Analysis A on  SS.analysis_id = A.id
      INNER JOIN Info I_A ON I_A.id = A.id and I_A.id_type = 'Analysis'
      INNER JOIN File F on  A.id = F.analysis_id
      INNER JOIN Info I_F ON I_F.id = F.id and I_F.id_type = 'File';

DROP VIEW IF EXISTS IdView;
CREATE VIEW IdView AS
  SELECT DISTINCT
    A.id as analysis_id,
    A.type as analysis_type,
    A.state as analysis_state,
    A.study_id as study_id,
    D.id as donor_id,
    SP.id as specimen_id,
    SA.id as sample_id,
    F.id as object_id
  FROM Donor D
    INNER JOIN Specimen SP on D.id = SP.donor_id
    INNER JOIN Sample as SA on SP.id = SA.specimen_id
    INNER JOIN SampleSet as SAS on SA.id = SAS.sample_id
    INNER JOIN File as F on SAS.analysis_id = F.analysis_id
    INNER JOIN Analysis as A on SAS.analysis_id = A.id;

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
