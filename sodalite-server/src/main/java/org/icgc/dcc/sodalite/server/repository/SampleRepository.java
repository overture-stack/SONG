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

import org.icgc.dcc.sodalite.server.model.File;
import org.icgc.dcc.sodalite.server.model.Sample;
import org.icgc.dcc.sodalite.server.repository.mapper.SampleMapper;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;

@RegisterMapper(SampleMapper.class)
public interface SampleRepository {

  @SqlUpdate("INSERT INTO sample (id, study_id, specimen_id, submitter_id, type) VALUES (:id, :study_id, :specimen_id, :submitter_id, :type)")
  int save(@Bind("id") String id, @Bind("study_id") String studyId, @Bind("specimen_id") String specimenId, @Bind("submitter_id") String submitterId,
      @Bind("type") String type);

  @SqlUpdate("UPDATE sample SET study_id=:study_id, submitter_id=:submitter_id, type=:type where id=:id")
  int update(@Bind("id") String id, @Bind("study_id") String studyId, @Bind("submitter_id") String submitterId,
      @Bind("type") String type);

  @SqlQuery("SELECT id, study_id, specimen_id, submitter_id, type FROM sample WHERE id=:id")
  Sample getById(@Bind("id") String id);

  @SqlQuery("SELECT id, study_id, specimen_id, submitter_id, type FROM sample WHERE specimen_id=:specimen_id")
  List<Sample> findByParentId(@Bind("specimen_id") String specimenId);

  @SqlQuery("SELECT id, study_id, specimen_id, submitter_id, type FROM sample WHERE study_id=:study_id AND submitter_id=:submitter_id")
  Sample getByBusinessKey(@Bind("study_id") String studyId, @Bind("submitter_id") String submitter_id);
  
  @SqlQuery("SELECT id from sample where specimen_id=:specimen_id")
  List<String> getIds(@Bind("specimen_id") String specimenId);

  @SqlUpdate("DELETE from sample where id=:id")
  int delete(@Bind("id") String id);

  @SqlUpdate("DELETE from sample where specimen_id=:specimen_id")
  String deleteByParentId(String specimenId);
}