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

import lombok.NonNull;
import lombok.val;
import org.icgc.dcc.song.core.exceptions.ServerException;
import org.icgc.dcc.song.server.model.entity.file.FileData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static org.icgc.dcc.song.core.exceptions.ServerErrors.FILE_UPDATE_REQUEST_VALIDATION_FAILED;
import static org.icgc.dcc.song.core.utils.Responses.OK;
import static org.icgc.dcc.song.server.model.enums.AnalysisStates.UNPUBLISHED;

@Service
public class FileModificationService {

  private final FileService fileService;
  private final AnalysisService analysisService;
  private final ValidationService validationService;

  public FileModificationService(
      @Autowired @NonNull ValidationService validationService,
      @Autowired @NonNull FileService fileService,
      @Autowired @NonNull AnalysisService analysisService) {
    this.fileService = fileService;
    this.analysisService = analysisService;
    this.validationService = validationService;
  }

  public String securedFileWithAnalysisUpdate(@NonNull String studyId,
      @NonNull String fileId, @NonNull FileData fileUpdateData){
    checkFileUpdateValidation(fileId, fileUpdateData);
    val file = fileService.securedUpdate(studyId, fileId, fileUpdateData);
    analysisService.securedUpdateState(studyId, file.getAnalysisId(), UNPUBLISHED);
    return OK;
  }

  private void checkFileUpdateValidation(String id, FileData fileUpdateData){
    val validationResponse = validationService.validate(fileUpdateData);
    if (validationResponse.isPresent()){
      throw ServerException.buildServerException(getClass(),
          FILE_UPDATE_REQUEST_VALIDATION_FAILED,
          "The file update request for objectId '%s' failed with the following errors: %s",
          id, validationResponse.get());
    }
  }

}
