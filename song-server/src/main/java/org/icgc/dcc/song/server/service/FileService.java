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
package org.icgc.dcc.song.server.service;

import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.val;
import org.icgc.dcc.song.server.model.entity.File;
import org.icgc.dcc.song.server.repository.FileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static org.icgc.dcc.song.core.exceptions.ServerErrors.FILE_RECORD_FAILED;
import static org.icgc.dcc.song.core.exceptions.ServerException.buildServerException;
import static org.icgc.dcc.song.core.utils.Responses.OK;
import static org.icgc.dcc.song.server.model.enums.IdPrefix.File;

@Service
@NoArgsConstructor
public class FileService {

  @Autowired
  FileRepository repository;
  @Autowired
  IdService idService;

  public String create(@NonNull String analysisId, @NonNull String studyId, @NonNull File file) {
    val id = idService.generateFileId(analysisId, file.getFileName());
    file.setObjectId(id);
    file.setStudyId(studyId);

    val status = repository.create(file);

    if (status != 1) {
      throw buildServerException(this.getClass(), FILE_RECORD_FAILED, "Cannot create File: %s", file.toString());
    }

    return id;
  }

  public File read(@NonNull String id) {
    return repository.read(id);
  }

  public String update(@NonNull File f) {
    repository.update(f);
    return OK;
  }

  public String delete(@NonNull String id) {
    repository.delete(id);
    return OK;
  }

  public String save(@NonNull String analysisId, @NonNull String studyId, @NonNull File file) {
    String fileId = repository.findByBusinessKey(analysisId, file.getFileName());
    if (fileId == null) {
      fileId = idService.generateFileId(analysisId, file.getFileName());
      file.setObjectId(fileId);
      file.setStudyId(studyId);
      val status=repository.create(file);
      if (status==-1) {
        throw buildServerException(this.getClass(), FILE_RECORD_FAILED, "Cannot create File: %s", file.toString());
      }
    } else {
      repository.update(file);
    }
    return fileId;
  }

}
