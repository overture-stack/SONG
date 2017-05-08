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

import org.icgc.dcc.sodalite.server.model.Sample;

import org.icgc.dcc.sodalite.server.repository.mapper.SampleMapper;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;

import java.util.List;

@RegisterMapper(SampleMapper.class)
public interface SampleRepository extends EntityRepository<Sample> {

  @SqlUpdate("INSERT INTO Sample (id, specimen_id, submitter_id, type) VALUES (:id, :specimen_id, :submitter_id, :type)")
  int save(@Bind("id") String id, @Bind("specimen_id") String specimen_id, @Bind("submitter_id") String submitter_id,
      @Bind("type") String type);

  @SqlUpdate("UPDATE Sample SET submitter_id=:submitter_id, type=:type where id=:id")
  int set(@Bind("id") String id, @Bind("submitter_id") String submitter_id,
      @Bind("type") String type);

  @SqlQuery("SELECT id, submitter_id, type FROM Sample WHERE id=:id")
  Sample getById(@Bind("id") String id);

  @SqlQuery("SELECT id, submitter_id, type FROM Sample WHERE specimen_id=:specimen_id")
  List<Sample> findByParentId(@Bind("specimen_id") String specimen_id);

  @SqlQuery("SELECT id from Sample where specimen_id=:specimen_id")
  List<String> getIds(@Bind("specimen_id") String specimen_id);

  @SqlUpdate("DELETE from Sample where id=:id")
  int delete(@Bind("id") String id);

  @SqlUpdate("DELETE from Sample where specimen_id=:specimen_id")
  String deleteByParentId(String specimen_id);
}