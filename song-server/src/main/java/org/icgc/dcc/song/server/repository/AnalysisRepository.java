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

import org.icgc.dcc.song.server.model.analysis.Analysis;
import org.icgc.dcc.song.server.model.entity.File;
import org.icgc.dcc.song.server.model.experiment.SequencingRead;
import org.icgc.dcc.song.server.model.experiment.VariantCall;
import org.icgc.dcc.song.server.repository.mapper.AnalysisMapper;
import org.icgc.dcc.song.server.repository.mapper.FileMapper;
import org.icgc.dcc.song.server.repository.mapper.SequencingReadMapper;
import org.icgc.dcc.song.server.repository.mapper.VariantCallMapper;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.BindBean;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;

import java.util.List;

@RegisterMapper(FileMapper.class)
public interface AnalysisRepository {

  @SqlUpdate("INSERT INTO Analysis (id, study_id, submitter_id, type, state, info) " +
          "VALUES (:analysisId, :study, :analysisSubmitterId, :analysisType, :analysisState, :info)")
  void createAnalysis(@BindBean Analysis analysis );

  @SqlUpdate("Update Analysis set state=:state where id=:analysisId")
  void updateState(@Bind("analysisId") String id, @Bind("state") String state);

  @SqlUpdate("INSERT INTO FileSet (analysis_id, file_id) values (:analysisId, :fileId)")
  void addFile(@Bind("analysisId") String id, @Bind("fileId") String fileId);

  @SqlUpdate("INSERT INTO SampleSet (analysis_id, sample_id) values (:analysisId, :sampleId)")
  void addSample(@Bind("analysisId") String id, @Bind("sampleId") String sampleId);

  @SqlUpdate("INSERT INTO SequencingRead (id, library_strategy, paired_end, insert_size,aligned,alignment_tool, reference_genome) "
          + "VALUES (:analysisId, :libraryStrategy, :pairedEnd, :insertSize, :aligned, :alignmentTool, :referenceGenome)")
  void createSequencingRead(@BindBean SequencingRead s);

  @SqlUpdate("INSERT INTO VariantCall (id, variant_calling_tool, matched_normal_sample_submitter_id) " +
          "VALUES (:analysisId, :variantCallingTool, :matchedNormalSampleSubmitterId)")
  void createVariantCall(@BindBean VariantCall c);

  @RegisterMapper(AnalysisMapper.class)
  @SqlQuery("SELECT id, study_id, submitter_id, type, state, info FROM Analysis WHERE id=:id")
  Analysis read(@Bind("id") String id);

  @SqlQuery("SELECT id, analysis_id, name, study_id, size, type, md5, info  FROM File WHERE analysis_id=:id ")
  List<File> readFiles(@Bind("id") String id);

  @SqlUpdate("DELETE FROM File WHERE analysis_id=:id")
  void deleteFiles(@Bind("id") String analysisId);

  @SqlUpdate("DELETE FROM SampleSet WHERE analysis_id=:id")
  void deleteCompositeEntities(@Bind("id") String analysisId);

  @SqlQuery("SELECT sample_id FROM SampleSet WHERE analysis_id=:id")
  List<String> findSampleIds(@Bind("id") String id);

  @SqlQuery("Select analysis_id FROM SampleSet WHERE sample_id=:id")
  List<String> findBySampleId(@Bind("id") String id);

  @RegisterMapper(SequencingReadMapper.class)
  @SqlQuery("SELECT id, library_strategy, paired_end, insert_size,aligned,alignment_tool,reference_genome, info " +
          "FROM SequencingRead where id=:id")
  SequencingRead readSequencingRead(@Bind("id") String id);

  @SqlUpdate("UPDATE SequencingRead SET library_strategy=:libraryStrategy, paired_end=:pairedEnd, " +
          "insert_size=:insertSize, aligned=:aligned, alignment_tool=:alignmentTool, reference_genome=:referenceGenome," +
          "info=:info WHERE id=:analysisId")
  void updateSequencingRead(@BindBean SequencingRead sequencingRead);

  @RegisterMapper(VariantCallMapper.class)
  @SqlQuery("SELECT id, variant_calling_tool, matched_normal_sample_submitter_id, info " +
          "FROM VariantCall where id=:id")
  VariantCall readVariantCall(@Bind("id") String id);

  @SqlUpdate("UPDATE VariantCall SET variant_calling_tool=:variantCallingTool, " +
          "matched_normal_sample_submitter_id=:matchedNormalSampleSubmitterId, info=:info WHERE id=:analysisId")
  void updateVariantCall(@BindBean VariantCall variantCall);

  @SqlQuery("SELECT id from Analysis where study_id=:studyId AND submitter_id=:key")
  String findByBusinessKey(@Bind("studyId") String studyId, @Bind("key") String key);

  @RegisterMapper(AnalysisMapper.class)
  @SqlQuery("queries/analysis/findByStudyId.sql")
  List<Analysis> find(@Bind("studyId") String studyId);

  @RegisterMapper(AnalysisMapper.class)
  @SqlQuery("queries/analysis/searchAnalysis.sql")
  List<Analysis> search(@Bind("studyId") String studyId,
      @Bind("donorId") String donorId,
      @Bind("specimenId") String specimenId,
      @Bind("sampleId") String sampleId,
      @Bind("fileId") String fileId);

}
