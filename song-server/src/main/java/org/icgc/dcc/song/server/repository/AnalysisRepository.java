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
 *
 */
package org.icgc.dcc.song.server.repository;

import java.util.List;

import org.icgc.dcc.song.server.model.analysis.Analysis;
import org.icgc.dcc.song.server.model.analysis.SequencingRead;
import org.icgc.dcc.song.server.model.analysis.VariantCall;
import org.icgc.dcc.song.server.model.entity.File;
import org.icgc.dcc.song.server.repository.mapper.FileMapper;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.BindBean;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;

@RegisterMapper(FileMapper.class)
public interface AnalysisRepository {

  @SqlUpdate("INSERT INTO Analysis (id, study_id,state) VALUES (:analysisId, :study, :analysisState)")
  void createAnalysis(@BindBean Analysis analysis );

  @SqlUpdate("Update Analysis set state=:state where id=:analysisId")
  void updateState(@Bind("analysisId") String id, @Bind("state") String state);

  @SqlUpdate("INSERT INTO FileSet (analysis_id, file_id) values (:analysisId, :fileId)")
  void addFile(@Bind("analysisId") String id, @Bind("fileId") String fileId);

  @SqlUpdate("INSERT INTO AnalysisSampleSet (analysis_id, sample_id) values (:analysisId, :sampleId)")
  void addSample(@Bind("analysisId") String id, @Bind("sampleId") String fileId);

  @SqlUpdate("INSERT INTO SequencingRead (id, library_strategy, paired_end, insert_size,aligned,alignment_tool, reference_genome) "
          + "VALUES (:analysisId, :libraryStrategy, :pairedEnd, :insertSize, :aligned, :alignmentTool, :referenceGenome)")
  void createSequencingRead(@BindBean SequencingRead s);

  @SqlUpdate("INSERT INTO VariantCall (id, variant_calling_tool, matched_normal_sample_submitter_id) " +
          "values(:analysisId, :variantCallingTool, :matchedNormalSampleSubmitterId)")
  void createVariantCall(@BindBean VariantCall c);

  @SqlQuery("SELECT f.id, f.name, f.study_id, f.size, f.type, f.md5, f.info "
      + "FROM File f, FileSet s "
      + "WHERE s.analysis_id=:analysisId "
      + "  AND f.id = s.file_id")
  List<File> getFilesById(@Bind("analysisId") String id);
}