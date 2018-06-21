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
import org.icgc.dcc.song.server.converter.FileConverter;
import org.icgc.dcc.song.server.model.entity.file.File;
import org.icgc.dcc.song.server.model.entity.file.FileData;
import org.icgc.dcc.song.server.model.enums.FileUpdateTypes;
import org.icgc.dcc.song.server.repository.AnalysisRepository;
import org.icgc.dcc.song.server.repository.FileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static java.util.Objects.isNull;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.ENTITY_NOT_RELATED_TO_STUDY;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.FILE_NOT_FOUND;
import static org.icgc.dcc.song.core.exceptions.ServerException.buildServerException;
import static org.icgc.dcc.song.core.exceptions.ServerException.checkServer;
import static org.icgc.dcc.song.core.utils.Responses.OK;
import static org.icgc.dcc.song.server.model.enums.FileUpdateTypes.resolveFileUpdateType;

@Service
@NoArgsConstructor
public class FileService {

  @Autowired
  AnalysisRepository analysisRepository;

  @Autowired
  @Qualifier(value = "fileRepository")
  FileRepository repository;

  @Autowired
  FileInfoService infoService;
  @Autowired
  IdService idService;
  @Autowired
  StudyService studyService;
  @Autowired
  FileConverter fileConverter;

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

  public void checkFileAndStudyRelated(@NonNull String studyId, @NonNull String id){
    val numFiles = repository.countAllByStudyIdAndObjectId(studyId, id);
    if (numFiles < 1){
      studyService.checkStudyExist(studyId);
      val file = unsecuredRead(id);
      throw buildServerException(getClass(), ENTITY_NOT_RELATED_TO_STUDY,
          "The objectId '%s' is not related to the input studyId '%s'. It is actually related to studyId '%s'",
          id, studyId, file.getObjectId() );
    }
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

  public File securedRead(@NonNull String studyId, String id) {
    checkFileAndStudyRelated(studyId, id);
    return unsecuredRead(id);
  }

  public FileUpdateTypes securedUpdate(@NonNull String studyId, String id, FileData fileUpdateData) {
    checkFileAndStudyRelated(studyId, id);
    return unsecuredUpdate(id, fileUpdateData);
  }

  private FileUpdateTypes unsecuredUpdate(@NonNull String id, @NonNull FileData fileUpdateRequest) {
    val originalFile = unsecuredRead(id);
    return updateAndSave(originalFile, fileUpdateRequest);
  }

  /**
   * Updates a base file (the {@param originalFile} ) using {@param fileUpdateData}
   * while not modifing the base file.
   * @param originalFile
   * @param fileUpdateData
   * @return
   */
  @Transactional
  public FileUpdateTypes updateAndSave(@NonNull File originalFile, @NonNull FileData fileUpdateData) {
    val updatedFile = createUpdateFile(originalFile, fileUpdateData);
    repository.save(updatedFile);
    if (!isNull(fileUpdateData.getInfo())){
      infoService.update(originalFile.getObjectId(), updatedFile.getInfoAsString());
    }
    return resolveFileUpdateType(originalFile, fileUpdateData);
  }

  File createUpdateFile(@NonNull File baseFile, @NonNull FileData fileUpdateData){
    val updatedFile = fileConverter.copyFile(baseFile);
    fileConverter.updateEntityFromData(fileUpdateData, updatedFile);
    return updatedFile;
  }

  @Transactional
  public String securedDelete(@NonNull String studyId, List<String> id) {
    id.forEach(x -> securedDelete(studyId, x));
    return OK;
  }

  @Transactional
  public String securedDelete(@NonNull String studyId, String id) {
    checkFileAndStudyRelated(studyId, id);
    return internalDelete(id);
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
      val fileUpdateData = fileConverter.fileEntityToFileUpdateRequest(file);
      unsecuredUpdate(file.getObjectId(), fileUpdateData);
    }
    return fileId;
  }

  private File unsecuredRead(@NonNull String id) {
    val result = repository.findById(id);
    fileNotFoundCheck(result.isPresent(), id);
    val f = fileConverter.copyFile(result.get());
    f.setInfo(infoService.readNullableInfo(id));
    return f;
  }

  private String internalDelete(@NonNull String id) {
    repository.deleteById(id);
    infoService.delete(id);
    return OK;
  }

  private static void fileNotFoundCheck(boolean expression, @NonNull String id){
    checkServer(expression, FileService.class, FILE_NOT_FOUND,
        "The File with objectId '%s' does not exist", id);
  }


}
