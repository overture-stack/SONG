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
CREATE TABLE Submissions (id VARCHAR(36) PRIMARY KEY, study_id VARCHAR(36) references Study, state VARCHAR(50), errors TEXT, payload TEXT, created_at TIMESTAMP WITH TIMEZONE NOT NULL DEFAULT now(), updated_at TIMESTAMP WITH TIMEZONE NOT NULL DEFAULT now());
