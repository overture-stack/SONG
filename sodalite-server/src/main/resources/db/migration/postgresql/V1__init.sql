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
CREATE TABLE File(id VARCHAR(36) PRIMARY KEY, sample_id VARCHAR(36) references Sample, name TEXT, size BIGINT, md5 CHAR(32), type FILE_TYPE, metadata_doc TEXT);
CREATE TABLE VariantCallAnalysis (id VARCHAR(36) PRIMARY KEY, study_id VARCHAR(36) references Study, state ANALYSIS_STATE, variant_calling_tool TEXT);
CREATE TABLE VariantCallFileSet (id VARCHAR(16) PRIMARY KEY, analysis_id VARCHAR(36) references VariantCallAnalysis, file_id VARCHAR(36) references File);
CREATE TABLE SequencingReadAnalysis (id VARCHAR(36) PRIMARY KEY, study_id VARCHAR(36) references Study, state ANALYSIS_STATE, library_strategy LIBRARY_STRATEGY, paired_end BOOLEAN, insert_size BIGINT, aligned BOOLEAN, alignment_tool TEXT, reference_genome TEXT);
CREATE TABLE SequencingReadFileSet (id VARCHAR(16) PRIMARY KEY, analysis_id VARCHAR(36) references SequencingReadAnalysis, file_id VARCHAR(36) references File);
CREATE TABLE MAFAnalysis (id VARCHAR(36) PRIMARY KEY, study_id VARCHAR(36) references Study);
CREATE TABLE MAFFileSet(id VARCHAR(36) PRIMARY KEY, analysis_id VARCHAR(36) references MAFAnalysis, file_id VARCHAR(36) references File);
CREATE TABLE Submissions (id VARCHAR(36) PRIMARY KEY, study_id VARCHAR(36) references Study, state VARCHAR(50), errors TEXT, payload TEXT, created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(), updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now());

CREATE VIEW details AS 
    SELECT 
        T.name as StudyName,
        D.id as DonorId, D.submitter_id as SubmitterDonorId, D.gender as DonorGender,
        P.id as SpecimenId, P.submitter_id as SubmitterSpecimenId, P.class as SpecimenClass, P.type as SpecimenType, 
        A.id as SampleId, A.submitter_id as SubmitterSampleId,  A.type as SampleType,
        F.id as FileId, F.name as FileName, F.type as FileType, F.size as FileSize 
    FROM Study T, Donor D, Specimen P, Sample A, File F 
    WHERE F.sample_id = A.id AND 
          A.specimen_id=P.id AND 
          P.donor_id = D.id AND
          D.study_id = T.id
   ORDER BY StudyName,DonorId,Specimenid,SampleId,FileId;
