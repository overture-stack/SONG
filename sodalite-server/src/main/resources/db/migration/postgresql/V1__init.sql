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
CREATE TYPE gender as ENUM('Male','Female','Unknown');
DROP TYPE IF EXISTS specimen_class CASCADE;
CREATE TYPE specimen_class as ENUM('Normal','Tumour','Adjacent normal');
DROP TYPE IF EXISTS specimen_type CASCADE;
CREATE TYPE specimen_type as ENUM('Normal-solid tissue',
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
    'Metastatic tumour - other', 'Cell line - derived from xenograft tumour');
DROP TYPE IF EXISTS sample_type CASCADE;
CREATE TYPE sample_type as ENUM('DNA','FFPE DNA','Amplified DNA','RNA','Total RNA','FFPE RNA');
DROP TYPE IF EXISTS file_type CASCADE;
CREATE TYPE file_type as ENUM('FASTA','FAI','FASTQ','BAM','BAI','VCF','TBI','IDX');
DROP TYPE IF EXISTS analysis_state CASCADE;
CREATE TYPE analysis_state as ENUM('Created','Uploaded','Published','Suppressed');
DROP TYPE IF EXISTS library_strategy CASCADE;
CREATE TYPE library_strategy as ENUM('WGS','WXS','RNA-Seq','ChIP-Seq','miRNA-Seq','Bisulfite-Seq','Validation','Amplicon','Other');

DROP TABLE IF EXISTS Study,Donor,Specimen,Sample,File,VariantCallAnalysis,VariantCallFileSet,SequencingReadAnalysis,SequencingReadFileSet,MAFAnalysis,MAFFileSet,Submissions;

CREATE TABLE Study (id VARCHAR(36) PRIMARY KEY, name TEXT, description TEXT, organization TEXT);
CREATE TABLE Donor (id VARCHAR(16) PRIMARY KEY, study_id VARCHAR(36) references Study, submitter_id TEXT, gender GENDER);
CREATE TABLE Specimen (id VARCHAR(16) PRIMARY KEY, donor_id VARCHAR(16) references Donor, submitter_id TEXT, class SPECIMEN_CLASS, type SPECIMEN_TYPE);
CREATE TABLE Sample(id VARCHAR(16) PRIMARY KEY, specimen_id VARCHAR(16) references Specimen, submitter_id TEXT, type SAMPLE_TYPE);
CREATE TABLE File(id VARCHAR(36) PRIMARY KEY, sample_id VARCHAR(36) references Sample, name TEXT, size BIGINT, md5sum CHAR(32), type FILE_TYPE, metadata_doc TEXT);
CREATE TABLE VariantCallAnalysis (id VARCHAR(36) PRIMARY KEY, study_id VARCHAR(36) references Study, state ANALYSIS_STATE, variant_calling_tool TEXT);
CREATE TABLE VariantCallFileSet (id VARCHAR(16) PRIMARY KEY, analysis_id VARCHAR(36) references VariantCallAnalysis, file_id VARCHAR(36) references File);
CREATE TABLE SequencingReadAnalysis (id VARCHAR(36) PRIMARY KEY, study_id VARCHAR(36) references Study, state ANALYSIS_STATE, library_strategy LIBRARY_STRATEGY, paired_end BOOLEAN, insert_size BIGINT, aligned BOOLEAN, alignment_tool TEXT, reference_genome TEXT);
CREATE TABLE SequencingReadFileSet (id VARCHAR(16) PRIMARY KEY, analysis_id VARCHAR(36) references SequencingReadAnalysis, file_id VARCHAR(36) references File);
CREATE TABLE MAFAnalysis (id VARCHAR(36) PRIMARY KEY, study_id VARCHAR(36) references Study);
CREATE TABLE MAFFileSet(id VARCHAR(36) PRIMARY KEY, analysis_id VARCHAR(36) references MAFAnalysis, file_id VARCHAR(36) references File);
CREATE TABLE Submissions (id VARCHAR(36) PRIMARY KEY, study_id VARCHAR(36) references Study, state VARCHAR(50), errors TEXT, payload TEXT, created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(), updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now());

insert into Study (id, name,description,organization) values ('ABC123','X1-CA','A fictional study', 'Sample Data Research Institute');
insert into Study (id, name,description,organization) values ('XYZ234','X2-CA','A new study', 'Sample Data Research Institute');
insert into Donor (id, study_id, submitter_id, gender) values ('DO1','ABC123', 'Subject-X23Alpha7', 'Male');
insert into Specimen (id, donor_id, submitter_id, class, type) values ('SP1','DO1','Tissue-Culture 284 Gamma 3', 'Tumour', 'Recurrent tumour - solid tissue');
insert into Specimen (id, donor_id, submitter_id, class, type) values ('SP2','DO1','Tissue-Culture 285 Gamma 7', 'Normal', 'Normal - other');
insert into Sample (id, specimen_id, submitter_id, type) values ('SA1', 'SP1', 'T285-G7-A5','DNA'); 
insert into Sample (id, specimen_id, submitter_id, type) values ('SA11', 'SP1', 'T285-G7-B9','DNA');
insert into Sample (id, specimen_id, submitter_id, type) values ('SA21', 'SP2', 'T285-G7N','DNA');
insert into File (id, sample_id, name, size, type, metadata_doc) values ('FI1', 'SA1','ABC-TC285G7-A5-ae3458712345.bam', 122333444455555, 'BAM', '<XML>Not even well-formed <XML></XML>');
insert into File (id, sample_id, name, size, type, metadata_doc) values ('FI2', 'SA1','ABC-TC285G7-A5-wleazprt453.bai', 123456789, 'BAI', '<XML>Not even well-formed<XML></XML>');
insert into File(id, sample_id, name, size, type, metadata_doc) values ('FI3', 'SA11', 'ABC-TC285-G7-B9-kthx12345.bai', 23456789,'BAI','<XML><Status>Inconclusive</Status></XML>');
insert into File(id, sample_id, name, size, type, metadata_doc) values ('FI4','SA21','ABC-TC285-G7N-alpha12345.fai', 12345,'FAI','<XML></XML>');
insert into VariantCallAnalysis(id, study_id, state, variant_calling_tool) values ('AN1',  'ABC123', 'Suppressed', 'SuperNewVariantCallingTool');
insert into VariantCallFileSet(id, analysis_id, file_id) values ('FS1','AN1','FI1'),('FS2','AN1','FI2');
insert into SequencingReadAnalysis (id, study_id, state, library_strategy, paired_end, insert_size, aligned, alignment_tool, reference_genome) values ('AN2','ABC123','Suppressed', 'Other', TRUE, 12345, TRUE, 'BigWrench', 'hg19');
insert into SequencingReadFileSet(id, analysis_id, file_id) values ('FS3','AN2', 'FI1'),('FS4','AN2','FI3');
insert into MAFAnalysis(id, study_id) values ('MU1','ABC123');
insert into MAFFileSet(id, analysis_id, file_id) values ('FS3', 'MU1', 'FI1'),('FS5','MU1','FI2'),('FS6','MU1','FI3');
