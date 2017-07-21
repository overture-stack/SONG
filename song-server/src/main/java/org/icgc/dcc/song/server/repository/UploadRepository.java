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

import org.icgc.dcc.song.server.model.Upload;
import org.icgc.dcc.song.server.repository.mapper.UploadMapper;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;

//TODO: [DCC-5643] Cleanup SQLQueries to reference constants
@RegisterMapper(UploadMapper.class)
public interface UploadRepository {

  @SqlUpdate("INSERT INTO upload (id, study_id, state, payload, updated_at) VALUES (:id, :studyId, :state, :payload, now())")
  int create(@Bind("id") String id, @Bind("studyId") String studyId, @Bind("state") String state,
      @Bind("payload") String jsonPayload);

  // note: avoiding handling datetime's in application; keeping it all in the SQL (also, see schema)
  @SqlUpdate("UPDATE upload SET state = :state, errors = :errors, updated_at = now() WHERE id = :id")
  int update(@Bind("id") String id, @Bind("state") String state, @Bind("errors") String errrors);

  @SqlQuery("SELECT id, study_id, state, created_at, updated_at, errors, payload FROM upload WHERE id = :uploadId")
  Upload get(@Bind("uploadId") String id);

}
