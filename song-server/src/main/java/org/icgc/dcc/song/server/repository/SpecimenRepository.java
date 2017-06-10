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

import org.icgc.dcc.song.server.model.entity.Specimen;
import org.icgc.dcc.song.server.repository.mapper.SpecimenMapper;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.BindBean;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;

@RegisterMapper(SpecimenMapper.class)
public interface SpecimenRepository {

  @SqlUpdate("INSERT INTO Specimen (id, submitter_id, donor_id, class, type, info) VALUES (:specimenId, :specimenSubmitterId, :donorId, :specimenClass, :specimenType, :info)")
  int create(@BindBean Specimen specimen);

  @SqlQuery("SELECT id, submitter_id, donor_id, class, type, info FROM Specimen where id=:id")
  Specimen read(@Bind("id") String id);

  @SqlUpdate("UPDATE Specimen SET submitter_id=:specimenSubmitterId, class=:specimenClass, type=:specimenType where id=:specimenId")
  int update(@BindBean Specimen specimen);

  @SqlUpdate("DELETE from Specimen where id=:id")
  int delete(@Bind("id") String id);

  @SqlQuery("SELECT id, submitter_id, donor_id, class, type, info FROM Specimen where donor_id=:donor_id")
  List<Specimen> readByParentId(@Bind("donor_id") String donor_id);

  @SqlQuery("SELECT id from Specimen where donor_id=:donor_id")
  List<String> findByParentId(@Bind("donor_id") String donor_id);

  @SqlQuery("SELECT s.id from Specimen s, Donor d "
      + "WHERE s.submitter_id=:submitterId "
      + "AND s.donor_id = d.id "
      + "AND d.study_id=:studyId")
  String findByBusinessKey(@Bind("studyId") String studyId, @Bind("submitterId") String submitterId);
}
