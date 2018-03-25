/*
 * Copyright (c) 2018. Ontario Institute for Cancer Research
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
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
    studyService.checkStudyExist(studyId);

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
    studyService.checkStudyExist(studyId);
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

}
