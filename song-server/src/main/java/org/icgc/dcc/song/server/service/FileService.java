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
import org.icgc.dcc.song.server.repository.AnalysisRepository;
import org.icgc.dcc.song.server.repository.FileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static java.util.Objects.isNull;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.FILE_NOT_FOUND;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.FILE_REPOSITORY_CREATE_RECORD;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.FILE_REPOSITORY_DELETE_RECORD;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.FILE_REPOSITORY_UPDATE_RECORD;
import static org.icgc.dcc.song.core.exceptions.ServerException.checkServer;
import static org.icgc.dcc.song.core.utils.Responses.OK;
import static org.icgc.dcc.song.server.service.AnalysisService.checkAnalysis;

@Service
@NoArgsConstructor
public class FileService {

  @Autowired
  AnalysisRepository analysisRepository;
  @Autowired
  FileRepository repository;
  @Autowired
  FileInfoService infoService;
  @Autowired
  IdService idService;
  @Autowired
  StudyService studyService;

  public String create(@NonNull String analysisId, @NonNull String studyId, @NonNull File file) {
    checkAnalysisAndStudyId(analysisId, studyId);

    val id = idService.generateFileId(analysisId, file.getFileName());
    file.setObjectId(id);
    file.setStudyId(studyId);
    file.setAnalysisId(analysisId);

    val status = repository.create(file);
    checkServer(status == 1,
        getClass(), FILE_REPOSITORY_CREATE_RECORD,
    "Could not create File record for file with objectId '%s': %s",
        file.getObjectId(), file);
    infoService.create(id, file.getInfoAsString());

    return id;
  }

  public boolean isFileExist(@NonNull String id){
    return !isNull(repository.read(id));
  }

  public void checkFileExists(String id){
    fileNotFoundCheck(isFileExist(id), id);
  }

  public void checkFileExists(@NonNull File file){
    checkFileExists(file.getObjectId());
  }

  public File read(@NonNull String id) {
    val f = repository.read(id);
    fileNotFoundCheck(!isNull(f), id);
    f.setInfo(infoService.readNullableInfo(id));
    return f;
  }

  public String update(@NonNull File f) {
    checkFileExists(f);
    val status = repository.update(f);
    checkServer(status == 1, getClass(), FILE_REPOSITORY_UPDATE_RECORD,
        "Cannot update objectId '%s' for file '%s'",
    f.getObjectId(), f);
    infoService.update(f.getObjectId(), f.getInfoAsString());
    return OK;
  }

  public String delete(@NonNull String id) {
    checkFileExists(id);
    val status = repository.delete(id);
    checkServer(status == 1, getClass(), FILE_REPOSITORY_DELETE_RECORD,
        "Cannot delete file with objectId '%s'", id);
    infoService.delete(id);
    return OK;
  }

  public String save(@NonNull String analysisId, @NonNull String studyId, @NonNull File file) {
    checkAnalysisAndStudyId(analysisId, studyId);
    String fileId = repository.findByBusinessKey(analysisId, file.getFileName());
    if (isNull(fileId)) {
      fileId = create(analysisId, studyId, file);
    } else {
      file.setObjectId(fileId);
      update(file);
    }
    return fileId;
  }

  private static void fileNotFoundCheck(boolean expression, @NonNull String id){
    checkServer(expression, FileService.class.getClass(), FILE_NOT_FOUND,
        "The File with objectId '%s' does not exist", id);
  }

  private void checkAnalysisAndStudyId(@NonNull String analysisId, @NonNull String studyId){
    checkAnalysis(analysisRepository, analysisId);
    studyService.checkStudyExist(studyId);
  }

}
