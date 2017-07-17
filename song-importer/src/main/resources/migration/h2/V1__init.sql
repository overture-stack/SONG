application.yml/*
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
 
DROP DOMAIN IF EXISTS gender;
CREATE DOMAIN gender as TEXT CHECK(VALUE IN('male','female','unspecified'));
DROP DOMAIN IF EXISTS SPECIMEN_CLASS;
CREATE DOMAIN specimen_class as TEXT CHECK(VALUE IN('Normal','Tumour','Adjacent normal'));
DROP DOMAIN IF EXISTS specimen_type;
CREATE DOMAIN specimen_type as TEXT CHECK(VALUE IN(
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
    'Metastatic tumour - other', 'Cell line - derived from xenograft tumour'));
DROP DOMAIN IF EXISTS sample_type;
CREATE DOMAIN sample_type as TEXT CHECK(VALUE IN('DNA','FFPE DNA','Amplified DNA','RNA','Total RNA','FFPE RNA'));
DROP DOMAIN IF EXISTS file_type;
CREATE DOMAIN file_type as TEXT CHECK(VALUE IN('FASTA','FAI','FASTQ','BAM','BAI','VCF','TBI','IDX'));

DROP DOMAIN IF EXISTS library_strategy;
CREATE DOMAIN library_strategy as TEXT CHECK(VALUE IN('WGS','WXS','RNA-Seq','ChIP-Seq','miRNA-Seq','Bisulfite-Seq','Validation','Amplicon','Other'));

DROP DOMAIN IF EXISTS analysis_type;
CREATE DOMAIN analysis_type as TEXT CHECK(VALUE IN('sequencingRead','variantCall','MAF'));

DROP DOMAIN IF EXISTS analysis_state;
CREATE DOMAIN analysis_state as TEXT CHECK(VALUE IN('PUBLISHED', 'UNPUBLISHED', 'SUPPRESSED'));

DROP TABLE IF EXISTS Study,Donor,Specimen,Sample,File,Analysis,VariantCallAnalysis,Submission;
 
CREATE TABLE Study(id VARCHAR(36) PRIMARY KEY, name TEXT, description TEXT, organization TEXT, info TEXT);
CREATE TABLE Donor(id VARCHAR(16) PRIMARY KEY, study_id VARCHAR(36) references Study, submitter_id TEXT,
    gender GENDER, info TEXT);
CREATE TABLE Specimen(id VARCHAR(16) PRIMARY KEY, donor_id VARCHAR(16) references Donor, submitter_id TEXT,
    class SPECIMEN_CLASS, type SPECIMEN_TYPE, info TEXT);
CREATE TABLE Sample(id VARCHAR(16) PRIMARY KEY, specimen_id VARCHAR(16) references Specimen, submitter_id TEXT,
    type SAMPLE_TYPE, info TEXT);
CREATE TABLE File(id VARCHAR(36) PRIMARY KEY, study_id VARCHAR(36) references Study, name TEXT, size BIGINT,
    md5 CHAR(32), type FILE_TYPE, info TEXT);

CREATE TABLE Analysis(id VARCHAR(36) PRIMARY KEY, type ANALYSIS_TYPE, state ANALYSIS_STATE,
    study_id VARCHAR(36) references Study, info TEXT);
CREATE TABLE FileSet(analysis_id VARCHAR(36) references Analysis, file_id VARCHAR(36) references File);
CREATE TABLE SampleSet(analysis_id VARCHAR(36) references Analysis, sample_id VARCHAR(36) references Sample);
CREATE TABLE SequencingRead(id VARCHAR(36) references Analysis, library_strategy LIBRARY_STRATEGY, paired_end BOOLEAN,
    insert_size BIGINT, aligned BOOLEAN, alignment_tool TEXT, reference_genome TEXT, info TEXT);
CREATE TABLE VariantCall(id VARCHAR(36) references Analysis, variant_calling_tool TEXT,
    tumour_sample_submitter_id TEXT, matched_normal_sample_submitter_id TEXT, info TEXT);
;

CREATE TABLE Upload(id VARCHAR(36) PRIMARY KEY, study_id VARCHAR(36) references Study, state VARCHAR(50),
    errors TEXT, payload TEXT, created_at TIMESTAMP WITH TIMEZONE NOT NULL DEFAULT now(),
    updated_at TIMESTAMP WITH TIMEZONE NOT NULL DEFAULT now());
