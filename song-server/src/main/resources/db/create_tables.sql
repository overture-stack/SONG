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
CREATE TYPE file_type as ENUM('FASTA','FAI','FASTQ','BAM','BAI','VCF','TBI','IDX');
DROP TYPE IF EXISTS analysis_state CASCADE;
CREATE TYPE analysis_state as ENUM('PUBLISHED', 'UNPUBLISHED', 'SUPPRESSED');
DROP TYPE IF EXISTS library_strategy CASCADE;
CREATE TYPE library_strategy as ENUM('WGS','WXS','RNA-Seq','ChIP-Seq','miRNA-Seq','Bisulfite-Seq','Validation',
    'Amplicon','Other');

DROP TYPE IF EXISTS analysis_type CASCADE;
CREATE TYPE analysis_type as ENUM('sequencingRead','variantCall','MAF');

DROP TABLE IF EXISTS Study CASCADE;
CREATE TABLE Study(id VARCHAR(36) PRIMARY KEY, name TEXT, description TEXT, organization TEXT);

DROP TABLE IF EXISTS Donor CASCADE;
CREATE TABLE Donor(id VARCHAR(36) PRIMARY KEY, study_id VARCHAR(36) references Study, submitter_id TEXT,
    gender GENDER);

DROP TABLE IF EXISTS Specimen CASCADE;
CREATE TABLE Specimen(id VARCHAR(36) PRIMARY KEY, donor_id VARCHAR(36) references Donor, submitter_id TEXT,
    class SPECIMEN_CLASS, type SPECIMEN_TYPE);
DROP TABLE IF EXISTS Sample CASCADE;
CREATE TABLE Sample(id VARCHAR(36) PRIMARY KEY, specimen_id VARCHAR(36) references Specimen, submitter_id TEXT,
    type SAMPLE_TYPE);

DROP TABLE IF EXISTS Analysis CASCADE;
CREATE TABLE Analysis(id VARCHAR(36) PRIMARY KEY, type ANALYSIS_TYPE, study_id VARCHAR(36) references Study, submitter_id TEXT, state ANALYSIS_STATE);

DROP TABLE IF EXISTS File CASCADE;
CREATE TABLE File(id VARCHAR(36) PRIMARY KEY, analysis_id VARCHAR(36) references Analysis, study_id VARCHAR(36) references Study, name TEXT, size BIGINT,
    md5 CHAR(32), type FILE_TYPE);

DROP TABLE IF EXISTS SampleSet CASCADE;
CREATE TABLE SampleSet(analysis_id VARCHAR(36) references Analysis, sample_id VARCHAR(36) references Sample);

DROP TABLE IF EXISTS SequencingRead CASCADE;
CREATE TABLE SequencingRead(id VARCHAR(36) references Analysis, library_strategy LIBRARY_STRATEGY, paired_end BOOLEAN, insert_size BIGINT, aligned BOOLEAN, alignment_tool TEXT, reference_genome TEXT);
DROP TABLE IF EXISTS VariantCall CASCADE;
CREATE TABLE VariantCall(id VARCHAR(36) references Analysis, variant_calling_tool TEXT, tumour_sample_submitter_id TEXT, matched_normal_sample_submitter_id TEXT);

DROP TABLE IF EXISTS Update CASCADE;
CREATE TABLE Upload(id VARCHAR(40) PRIMARY KEY, study_id VARCHAR(36) references Study, analysis_submitter_id TEXT, state VARCHAR(50), errors TEXT, payload TEXT, created_at TIMESTAMP NOT NULL DEFAULT now(), updated_at TIMESTAMP NOT NULL DEFAULT now());

drop TYPE if exists id_type CASCADE;
create TYPE id_type as ENUM('Study','Donor','Specimen','Sample','File','Analysis','SequencingRead','VariantCall');

DROP TABLE IF EXISTS Info;
CREATE TABLE Info(id VARCHAR(36), id_type id_type, info JSON);

