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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static org.icgc.dcc.song.core.exceptions.ServerErrors.FILE_NOT_FOUND;
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

    repository.save(file);
    infoService.create(id, file.getInfoAsString());
    return id;
  }

  public Page<File> findAll(@NonNull Pageable pageable){
    return repository.findAll(pageable);
  }

  public boolean isFileExist(@NonNull String id){
    return repository.existsById(id);
  }

  public void checkFileExists(String id){
    fileNotFoundCheck(isFileExist(id), id);
  }

  public void checkFileExists(@NonNull File file){
    checkFileExists(file.getObjectId());
  }

  public File read(@NonNull String id) {
    val result = repository.findById(id);
    fileNotFoundCheck(result.isPresent(), id);
    val f = result.get();
    f.setInfo(infoService.readNullableInfo(id));
    return f;
  }

  public String update(@NonNull File fileUpdate) {
    checkFileExists(fileUpdate.getObjectId());
    repository.save(fileUpdate);
    infoService.update(fileUpdate.getObjectId(), fileUpdate.getInfoAsString());
    return OK;
  }

  public String delete(@NonNull String id) {
    checkFileExists(id);
    repository.deleteById(id);
    infoService.delete(id);
    return OK;
  }

  public Optional<String> findByBusinessKey(@NonNull String analysisId, @NonNull String fileName){
    return repository.findAllByAnalysisIdAndFileName(analysisId, fileName)
        .stream()
        .map(File::getObjectId)
        .findFirst();
  }

  public String save(@NonNull String analysisId, @NonNull String studyId, @NonNull File file) {
    studyService.checkStudyExist(studyId);
    val result = findByBusinessKey(analysisId, file.getFileName());
    String fileId;
    if (!result.isPresent()) {
      fileId = create(analysisId, studyId, file);
    } else {
      fileId = result.get();
      file.setObjectId(fileId);
      update(file);
    }
    return fileId;
  }

  private static void fileNotFoundCheck(boolean expression, @NonNull String id){
    checkServer(expression, FileService.class, FILE_NOT_FOUND,
        "The File with objectId '%s' does not exist", id);
  }

}
