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

import org.icgc.dcc.sodalite.server.model.entity.Sample;
import org.icgc.dcc.sodalite.server.repository.mapper.SampleMapper;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.BindBean;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;

@RegisterMapper(SampleMapper.class)
public interface SampleRepository {

  @SqlUpdate("INSERT INTO Sample (id, submitter_id, specimen_id, type) VALUES (:sampleId, :sampleSubmitterId, :specimenId, :sampleType)")
  int create(@BindBean Sample sample);

  @SqlQuery("SELECT id, submitter_id, specimen_id, type FROM Sample WHERE id=:id")
  Sample read(@Bind("id") String id);

  @SqlQuery("SELECT id, submitter_id, specimen_id, type FROM Sample WHERE specimen_id=:specimen_id")
  List<Sample> readByParentId(@Bind("specimen_id") String specimenId);

  @SqlUpdate("UPDATE Sample SET submitter_id=:sampleSubmitterId, type=:sampleType where id=:sampleId")
  int update(@BindBean Sample sample);

  @SqlUpdate("DELETE from Sample where id=:id")
  int delete(@Bind("id") String id);

  @SqlUpdate("DELETE from Sample where specimen_id=:specimenId")
  String deleteByParentId(String specimenId);

  @SqlQuery("SELECT id from Sample where specimen_id=:specimenId")
  List<String> findByParentId(@Bind("specimenId") String specimen_id);

  @SqlQuery("SELECT s.id "
      + "FROM Sample s, Specimen sp, Donor d "
      + "WHERE s.submitter_id = :submitterId AND "
      + "s.specimen_id = sp.id AND "
      + "sp.donor_id = d.id AND "
      + "d.study_id = :studyId")
  String findByBusinessKey(@Bind("studyId") String studyId, @Bind("submitterId") String submitterId);
}