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

CREATE DOMAIN gender AS TEXT CHECK (VALUE IN ('male', 'female', 'unspecified'));
CREATE DOMAIN specimen_class AS TEXT CHECK (VALUE IN ('Normal', 'Tumour', 'Adjacent normal'));
CREATE DOMAIN specimen_type AS TEXT CHECK (VALUE IN ('Normal-solid tissue',
  'Normal - blood derived', 'Normal - bone marrow',
  'Normal - tissue adjacent to primary', 'Normal - buccal cell',
  'Normal - EBV immortalized', 'Normal - lymph node', 'Normal - other',
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
  'Metastatic tumour - other', 'Cell line - derived from xenograft tumour'));

CREATE DOMAIN SAMPLE_TYPE AS TEXT CHECK (VALUE IN ('DNA', 'FFPE DNA', 'Amplified DNA', 'RNA', 'Total RNA', 'FFPE RNA'));
CREATE DOMAIN FILE_TYPE AS TEXT CHECK (VALUE IN ('FASTA', 'FAI', 'FASTQ', 'BAM', 'BAI', 'VCF', 'TBI', 'IDX', 'XML'));
CREATE DOMAIN ANALYSIS_STATE AS TEXT CHECK (VALUE IN ('CREATED', 'UPLOADED', 'PUBLISHED', 'SUPPRESSED'));
CREATE DOMAIN LIBRARY_STRATEGY AS TEXT CHECK (VALUE IN ('WGS', 'WXS', 'RNA-Seq', 'ChIP-Seq', 'miRNA-Seq', 'Bisulfite-Seq', 'Validation', 'Amplicon', 'Other'));

CREATE TABLE study (
  id           VARCHAR(36) PRIMARY KEY,
  name         TEXT,
  description  TEXT,
  organization TEXT
);

CREATE TABLE donor (
  id           VARCHAR(16) PRIMARY KEY,
  study_id     VARCHAR(36) REFERENCES study,
  submitter_id TEXT,
  gender       GENDER
);

CREATE INDEX study_donor_idx
  ON donor (study_id);
  
CREATE TABLE specimen (
  id           VARCHAR(16) PRIMARY KEY,
  study_id     VARCHAR(36) REFERENCES study,
  donor_id     VARCHAR(16) REFERENCES donor,
  submitter_id TEXT,
  class        SPECIMEN_CLASS,
  type         SPECIMEN_TYPE
);

CREATE INDEX donor_specimen_idx
  ON specimen (study_id, donor_id);
  
CREATE TABLE sample (
  id           VARCHAR(16) PRIMARY KEY,
  study_id     VARCHAR(36) REFERENCES study,
  specimen_id  VARCHAR(16) REFERENCES specimen,
  submitter_id TEXT,
  type         SAMPLE_TYPE
);

CREATE INDEX specimen_sample_idx
  ON sample (study_id, specimen_id);
  
CREATE TABLE file (
  id           VARCHAR(36) PRIMARY KEY,
  study_id     VARCHAR(36) REFERENCES study,
  sample_id    VARCHAR(36) REFERENCES sample,
  name         TEXT,
  size         BIGINT,
  md5          CHAR(32),
  type         FILE_TYPE,
  metadata_doc TEXT
);

CREATE INDEX sample_file_idx
  ON file (study_id, sample_id);
  
CREATE TABLE variant_call_analysis (
  id                   VARCHAR(36) PRIMARY KEY,
  study_id             VARCHAR(36) REFERENCES study,
  state                ANALYSIS_STATE,
  variant_calling_tool TEXT
);

CREATE TABLE variant_call_fileset (
  id          VARCHAR(16) PRIMARY KEY,
  study_id    VARCHAR(36) REFERENCES study,
  analysis_id VARCHAR(36) REFERENCES variant_call_analysis,
  file_id     VARCHAR(36) REFERENCES file
);

CREATE INDEX variant_call_files_idx
  ON file (study_id, analysis_id);
  
CREATE TABLE sequencing_read_analysis (
  id               VARCHAR(36) PRIMARY KEY,
  study_id         VARCHAR(36) REFERENCES study,
  state            ANALYSIS_STATE,
  library_strategy LIBRARY_STRATEGY,
  paired_end       BOOLEAN,
  insert_size      BIGINT,
  aligned          BOOLEAN,
  alignment_tool   TEXT,
  reference_genome TEXT
);

CREATE TABLE sequencing_read_fileset (
  id          VARCHAR(16) PRIMARY KEY,
  study_id    VARCHAR(36) REFERENCES study,
  analysis_id VARCHAR(36) REFERENCES sequencing_read_analysis,
  file_id     VARCHAR(36) REFERENCES file
);

CREATE INDEX sequencing_read_files_idx
  ON file (study_id, analysis_id);
  
CREATE TABLE maf_analysis (
  id       VARCHAR(36) PRIMARY KEY,
  study_id VARCHAR(36) REFERENCES study
);

CREATE TABLE maf_fileset (
  id          VARCHAR(36) PRIMARY KEY,
  study_id    VARCHAR(36) REFERENCES study,
  analysis_id VARCHAR(36) REFERENCES maf_analysis,
  file_id     VARCHAR(36) REFERENCES file
);

CREATE TABLE submissions (
  id              VARCHAR(36) PRIMARY KEY,
  study_id        VARCHAR(36) REFERENCES study,
  state           VARCHAR(50),
  errors          TEXT,
  payload         TEXT,
  analysis_object TEXT,
  created_by      VARCHAR(36),
  created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  updated_by      VARCHAR(36),
  updated_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

CREATE MATERIALIZED VIEW full_model AS
  SELECT
    T.id           AS study_id,
    D.id           AS donor_id,
    D.submitter_id AS submitter_donor_id,
    D.gender       AS donor_gender,
    P.id           AS specimen_id,
    P.submitter_id AS submitter_specimen_id,
    P.class        AS specimen_class,
    P.type         AS specimen_type,
    A.id           AS sample_id,
    A.submitter_id AS submitter_sample_id,
    A.type         AS sample_type,
    F.id           AS file_id,
    F.name         AS file_name,
    F.type         AS file_type,
    F.size         AS file_size
  FROM Study T, Donor D, Specimen P, Sample A, File F
  WHERE F.sample_id = A.id AND
        A.specimen_id = P.id AND
        P.donor_id = D.id AND
        D.study_id = T.id
  ORDER BY study_id, donor_id, specimen_id, sample_id, file_id;
