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

import org.icgc.dcc.song.server.model.entity.Donor;
import org.icgc.dcc.song.server.repository.mapper.DonorMapper;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.BindBean;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;

@RegisterMapper(DonorMapper.class)
public interface DonorRepository {

  @SqlUpdate("INSERT INTO Donor (id, submitter_id, study_id, gender) VALUES (:donorId, :donorSubmitterId, :studyId, :donorGender)")
  int create(@BindBean Donor donor);

  @SqlQuery("SELECT id, submitter_id, study_id, gender, info FROM donor WHERE id=:id")
  Donor read(@Bind("id") String donor_id);

  @SqlUpdate("UPDATE Donor SET submitter_id=:donorSubmitterId, gender=:donorGender WHERE id=:donorId")
  int update(@BindBean Donor donor);

  @SqlUpdate("DELETE from donor where id=:id AND study_id=:study_id")
  int delete(@Bind("study_id") String study_id, @Bind("id") String id);

  @SqlQuery("SELECT id, submitter_id, study_id, gender FROM donor WHERE study_id=:study_id")
  List<Donor> readByParentId(@Bind("study_id") String study_id);

  @SqlQuery("SELECT id from donor where study_id=:study_id")
  List<String> findByParentId(@Bind("study_id") String parent_id);

  @SqlQuery("SELECT id from donor where study_id=:studyId AND submitter_id=:key")
  String findByBusinessKey(@Bind("studyId") String studyId, @Bind("key") String key);
}