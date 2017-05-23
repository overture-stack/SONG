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
package org.icgc.dcc.sodalite.server.repository;

import java.util.List;

import org.icgc.dcc.sodalite.server.model.SequencingRead;
import org.icgc.dcc.sodalite.server.repository.mapper.SequencingReadMapper;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;

@RegisterMapper(SequencingReadMapper.class)
public interface SequencingReadRepository {

  @SqlUpdate("INSERT INTO sequencing_read_analysis (id, study_id, submitter_id, state, library_strategy, paired_end, insert_size, aligned, alignment_tool, reference_genome) "
      + "VALUES (:id, :study_id, LOWER(:submitter_id), :state, :library_strategy, :paired_end, :insert_size, :aligned, :alignment_tool, :reference_genome)")
  int save(@Bind("id") String id, @Bind("study_id") String studyId, @Bind("submitter_id") String submitterId,
      @Bind("state") String state,
      @Bind("library_strategy") String libraryStrategy, @Bind("paired_end") Boolean pairedEnd,
      @Bind("insert_size") Integer insertSize, @Bind("aligned") Boolean aligned,
      @Bind("alignment_tool") String alignmentTool, @Bind("reference_genome") String referenceGenome);

  @SqlUpdate("UPDATE sequencing_read_analysis SET study_id=:study_id, submitter_id=LOWER(:submitter_id), state=:state, library_strategy=:library_strategy, paired_end=:paired_end, "
      + " insert_size=:insert_size, aligned=:aligned, alignment_tool=:alignment_tool, reference_genome=:reference_genome where id=:id")
  int update(@Bind("id") String id, @Bind("study_id") String studyId, @Bind("submitter_id") String submitterId,
      @Bind("state") String state,
      @Bind("library_strategy") String libraryStrategy, @Bind("paired_end") Boolean pairedEnd,
      @Bind("insert_size") Integer insertSize, @Bind("aligned") Boolean aligned,
      @Bind("alignment_tool") String alignmentTool, @Bind("reference_genome") String referenceGenome);

  @SqlQuery("SELECT id, study_id, submitter_id, state, library_strategy, paired_end, insert_size, aligned, alignment_tool, reference_genome FROM sequencing_read_analysis WHERE id=:id")
  SequencingRead getById(@Bind("id") String id);

  @SqlQuery("SELECT id, study_id, submitter_id, state, library_strategy, paired_end, insert_size, aligned, alignment_tool, reference_genome FROM sequencing_read_analysis "
      + "WHERE study_id=:study_id AND submitter_id=:submitter_id")
  SequencingRead getByBusinessKey(@Bind("study_id") String studyId, @Bind("submitter_id") String submitterId);

  @SqlUpdate("DELETE FROM sequencing_read_analysis where id=:id")
  int delete(@Bind("id") String id);

  @SqlUpdate("INSERT INTO sequencing_read_fileset (id, study_id, analysis_id, file_id) VALUES (:id, :study_id, :analysis_id, :file_id)")
  int saveAssociation(@Bind("id") String id, @Bind("study_id") String studyId, @Bind("analysis_id") String analysisId,
      @Bind("file_id") String fileId);

  @SqlUpdate("UPDATE sequencing_read_fileset SET study_id=:study_id, analysis_id=:analysis_id, file_id=:file_id WHERE id=:id")
  int updateAssociation(@Bind("id") String id, @Bind("study_id") String studyId, @Bind("analysis_id") String analysisId,
      @Bind("file_id") String fileId);

  @SqlUpdate("DELETE FORM sequencing_read_fileset WHERE analysis_id=:analysis_id")
  int deleteAllAssociations(@Bind("analysis_id") String analysisId);

  @SqlUpdate("DELETE FROM sequencing_read_fileset WHERE id=:id")
  int deleteAssociation(@Bind("id") String id);

  @SqlQuery("SELECT file_id FROM sequencing_read_fileset WHERE analysis_id=:analysis_id")
  List<String> getFileIds(@Bind("analysis_id") String analysisId);

}
