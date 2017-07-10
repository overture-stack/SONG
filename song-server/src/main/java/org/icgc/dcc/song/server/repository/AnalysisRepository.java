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
import org.icgc.dcc.song.server.model.experiment.SequencingRead;
import org.icgc.dcc.song.server.model.experiment.VariantCall;
import org.icgc.dcc.song.server.model.entity.File;
import org.icgc.dcc.song.server.repository.mapper.AnalysisMapper;
import org.icgc.dcc.song.server.repository.mapper.FileMapper;
import org.icgc.dcc.song.server.repository.mapper.SequencingReadMapper;
import org.icgc.dcc.song.server.repository.mapper.VariantCallMapper;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.BindBean;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;

@RegisterMapper(FileMapper.class)
public interface AnalysisRepository {

  @SqlUpdate("INSERT INTO Analysis (id, study_id, type, state, info) " +
          "VALUES (:analysisId, :study, :analysisType, :analysisState, :info)")
  void createAnalysis(@BindBean Analysis analysis );

  @SqlUpdate("Update Analysis set state=:state where id=:analysisId")
  void updateState(@Bind("analysisId") String id, @Bind("state") String state);

  @SqlUpdate("INSERT INTO FileSet (analysis_id, file_id) values (:analysisId, :fileId)")
  void addFile(@Bind("analysisId") String id, @Bind("fileId") String fileId);

  @SqlUpdate("INSERT INTO SampleSet (analysis_id, sample_id) values (:analysisId, :sampleId)")
  void addSample(@Bind("analysisId") String id, @Bind("sampleId") String fileId);

  @SqlUpdate("INSERT INTO SequencingRead (id, library_strategy, paired_end, insert_size,aligned,alignment_tool, reference_genome) "
          + "VALUES (:analysisId, :libraryStrategy, :pairedEnd, :insertSize, :aligned, :alignmentTool, :referenceGenome)")
  void createSequencingRead(@BindBean SequencingRead s);

  @SqlUpdate("INSERT INTO VariantCall (id, variant_calling_tool, matched_normal_sample_submitter_id) " +
          "VALUES (:analysisId, :variantCallingTool, :matchedNormalSampleSubmitterId)")
  void createVariantCall(@BindBean VariantCall c);

  @RegisterMapper(AnalysisMapper.class)
  @SqlQuery("SELECT id, study_id, type, state, info FROM Analysis WHERE id=:id")
  Analysis read(@Bind("id") String id);

  @SqlQuery("SELECT f.id, f.name, f.study_id, f.size, f.type, f.md5, f.info "
      + "FROM File f, FileSet s "
      + "WHERE s.analysis_id=:analysisId "
      + "  AND f.id = s.file_id")
  List<File> readFiles(@Bind("analysisId") String id);

  @SqlQuery("SELECT sample_id FROM SampleSet WHERE analysis_id=:id")
  List<String> findSampleIds(@Bind("id") String id);

  @SqlQuery("Select analysis_id FROM SampleSet WHERE sample_id=:id")
  List<String> findBySampleId(@Bind("id") String id);

  @RegisterMapper(SequencingReadMapper.class)
  @SqlQuery("SELECT id, library_strategy, paired_end, insert_size,aligned,alignment_tool, reference_genome, info " +
          "FROM SequencingRead where id=:id")
  SequencingRead readSequencingRead(@Bind("id") String id);

  @RegisterMapper(VariantCallMapper.class)
  @SqlQuery("SELECT id, variant_calling_tool, matched_normal_sample_submitter_id, info " +
          "FROM VariantCall where id=:id")
  VariantCall readVariantCall(@Bind("id") String id);

  @RegisterMapper(AnalysisMapper.class)
  @SqlQuery("queries/analysis/find.sql")
  List<Analysis> find(@Bind("studyId") String studyId);
}