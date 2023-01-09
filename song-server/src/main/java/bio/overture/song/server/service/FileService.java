/*
 * Copyright (c) 2019. Ontario Institute for Cancer Research
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
package bio.overture.song.server.service;

import static bio.overture.song.core.exceptions.ServerErrors.ENTITY_NOT_RELATED_TO_STUDY;
import static bio.overture.song.core.exceptions.ServerErrors.FILE_NOT_FOUND;
import static bio.overture.song.core.exceptions.ServerErrors.ID_NOT_FOUND;
import static bio.overture.song.core.exceptions.ServerException.buildServerException;
import static bio.overture.song.core.exceptions.ServerException.checkServer;
import static bio.overture.song.core.exceptions.ServerException.checkServerOptional;
import static bio.overture.song.core.utils.Responses.OK;

import bio.overture.song.server.converter.FileConverter;
import bio.overture.song.server.model.entity.FileEntity;
import bio.overture.song.server.repository.FileRepository;
import bio.overture.song.server.service.InfoService.FileInfoService;
import bio.overture.song.server.service.id.IdService;
import java.util.List;
import java.util.Optional;
import javax.transaction.Transactional;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@NoArgsConstructor
public class FileService {

  @Autowired FileRepository repository;

  @Autowired FileInfoService infoService;
  @Autowired IdService idService;
  @Autowired StudyService studyService;
  @Autowired FileConverter fileConverter;

  public String create(
      @NonNull String analysisId, @NonNull String studyId, @NonNull FileEntity file) {
    studyService.checkStudyExist(studyId);

    val result = idService.getFileId(analysisId, file.getFileName());
    val id =
        checkServerOptional(
            result,
            getClass(),
            ID_NOT_FOUND,
            "The objectId for analysisId '%s' and fileName '%s' was not found",
            analysisId,
            file.getFileName());

    file.setObjectId(id);
    file.setStudyId(studyId);
    file.setAnalysisId(analysisId);

    repository.save(file);
    infoService.create(id, file.getInfoAsString());
    return id;
  }

  public void checkFileAndStudyRelated(@NonNull String studyId, @NonNull String id) {
    val numFiles = repository.countAllByStudyIdAndObjectId(studyId, id);
    if (numFiles < 1) {
      studyService.checkStudyExist(studyId);
      val file = unsecuredRead(id);
      throw buildServerException(
          getClass(),
          ENTITY_NOT_RELATED_TO_STUDY,
          "The objectId '%s' is not related to the input studyId '%s'. It is actually related to studyId '%s'",
          id,
          studyId,
          file.getObjectId());
    }
  }

  public boolean isFileExist(@NonNull String id) {
    return repository.existsById(id);
  }

  public void checkFileExists(String id) {
    fileNotFoundCheck(isFileExist(id), id);
  }

  public void checkFileExists(@NonNull FileEntity file) {
    checkFileExists(file.getObjectId());
  }

  public FileEntity securedRead(@NonNull String studyId, String id) {
    checkFileAndStudyRelated(studyId, id);
    return unsecuredRead(id);
  }

  public void unsafeUpdate(FileEntity file) {
    repository.save(file);
    infoService.update(file.getObjectId(), file.getInfoAsString());
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

  public Optional<String> findByBusinessKey(@NonNull String analysisId, @NonNull String fileName) {
    return repository.findAllByAnalysisIdAndFileName(analysisId, fileName).stream()
        .map(FileEntity::getObjectId)
        .findFirst();
  }

  public String save(
      @NonNull String analysisId, @NonNull String studyId, @NonNull FileEntity file) {
    studyService.checkStudyExist(studyId);
    val result = findByBusinessKey(analysisId, file.getFileName());
    String fileId;
    if (!result.isPresent()) {
      fileId = create(analysisId, studyId, file);
    } else {
      fileId = result.get();
      file.setObjectId(fileId);
      val transientFile = fileConverter.copyFile(file);
      unsafeUpdate(transientFile);
    }
    return fileId;
  }

  private FileEntity unsecuredRead(@NonNull String id) {
    val result = repository.findById(id);
    fileNotFoundCheck(result.isPresent(), id);
    val transientFile = fileConverter.copyFile(result.get());
    transientFile.setInfo(infoService.readNullableInfo(id));
    return transientFile;
  }

  private String internalDelete(@NonNull String id) {
    repository.deleteById(id);
    infoService.delete(id);
    return OK;
  }

  private static void fileNotFoundCheck(boolean expression, @NonNull String id) {
    checkServer(
        expression,
        FileService.class,
        FILE_NOT_FOUND,
        "The File with objectId '%s' does not exist",
        id);
  }
}
