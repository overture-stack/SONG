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

import org.icgc.dcc.song.server.model.entity.File;
import org.icgc.dcc.song.server.repository.mapper.FileMapper;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.BindBean;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;

@RegisterMapper(FileMapper.class)
public interface FileRepository {

  @SqlUpdate("INSERT INTO File (id, analysis_id, name, study_id, size, type, md5) "
      + "VALUES (:objectId, :analysisId,:fileName,  :studyId, :fileSize, :fileType, :fileMd5sum)")
  int create(@BindBean File f);

  @SqlQuery("SELECT id, analysis_id, name, study_id, size, type, md5 FROM File WHERE id=:id")
  File read(@Bind("id") String id);

  @SqlUpdate("UPDATE File SET name=:fileName, analysis_id=:analysisId, size=:fileSize, type=:fileType, " +
          "md5=:fileMd5sum where id=:objectId")
  int update(@BindBean File file);

  @SqlUpdate("DELETE From File where id=:id")
  int delete(@Bind("id") String id);

  @SqlQuery("SELECT id, analysis_id, name, study_id, size, type, md5 FROM File WHERE study_id=:studyId")
  List<File> readByParentId(@Bind("studyId") String study_id);

  @SqlQuery("SELECT id from File WHERE name=:fileName AND analysis_id=:analysisId")
  String findByBusinessKey(@Bind("analysisId") String analysisId, @Bind("fileName") String fileName);
}
