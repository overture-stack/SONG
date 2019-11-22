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

import static bio.overture.song.core.exceptions.ServerErrors.ILLEGAL_FILE_UPDATE_REQUEST;
import static bio.overture.song.core.exceptions.ServerErrors.INVALID_FILE_UPDATE_REQUEST;
import static bio.overture.song.core.exceptions.ServerException.buildServerException;
import static bio.overture.song.core.exceptions.ServerException.checkServer;
import static bio.overture.song.core.model.enums.AnalysisStates.PUBLISHED;
import static bio.overture.song.core.model.enums.AnalysisStates.SUPPRESSED;
import static bio.overture.song.core.model.enums.AnalysisStates.UNPUBLISHED;
import static bio.overture.song.core.model.enums.FileUpdateTypes.CONTENT_UPDATE;
import static bio.overture.song.core.model.enums.FileUpdateTypes.METADATA_UPDATE;
import static bio.overture.song.core.model.enums.FileUpdateTypes.NO_UPDATE;
import static bio.overture.song.core.model.enums.FileUpdateTypes.resolveFileUpdateType;
import static java.lang.String.format;

import bio.overture.song.core.exceptions.ServerException;
import bio.overture.song.core.model.FileData;
import bio.overture.song.core.model.FileUpdateResponse;
import bio.overture.song.core.model.enums.FileUpdateTypes;
import bio.overture.song.server.converter.FileConverter;
import bio.overture.song.server.model.entity.FileEntity;
import javax.transaction.Transactional;
import lombok.NonNull;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FileModificationService {

  private final FileService fileService;
  private final FileConverter fileConverter;
  private final AnalysisService analysisService;
  private final ValidationService validationService;

  public FileModificationService(
      @Autowired @NonNull ValidationService validationService,
      @Autowired @NonNull FileService fileService,
      @Autowired @NonNull FileConverter fileConverter,
      @Autowired @NonNull AnalysisService analysisService) {
    this.fileService = fileService;
    this.analysisService = analysisService;
    this.validationService = validationService;
    this.fileConverter = fileConverter;
  }

  @Transactional
  public FileUpdateTypes updateWithRequest(
      @NonNull FileEntity originalFile, FileData fileUpdateRequest) {
    val updatedFile = createUpdateFile(originalFile, fileUpdateRequest);
    fileService.unsafeUpdate(updatedFile);
    return resolveFileUpdateType(originalFile, fileUpdateRequest);
  }

  /**
   * Securely updates a file, while handling the associated analysis's state appropriately.
   *
   * @param studyId study associated with the file
   * @param objectId id associated with the file
   * @param fileUpdateRequest data to update
   * @return {@code FileUpdateResponse}
   * @exception ServerException INVALID_FILE_UPDATE_REQUEST, ILLEGAL_FILE_UPDATE_REQUEST
   */
  @Transactional
  public FileUpdateResponse securedFileWithAnalysisUpdate(
      @NonNull String studyId, @NonNull String objectId, @NonNull FileData fileUpdateRequest) {

    // Validate the fileUpdateRequest
    checkFileUpdateRequestValidation(objectId, fileUpdateRequest);

    // Get original file
    val originalFile = fileService.securedRead(studyId, objectId);
    val analysisId = originalFile.getAnalysisId();

    // Check the analysis associated with the file is not suppressed. It is ILLEGAL to unsuppress an
    // analysis
    val currentState = analysisService.readState(analysisId);
    checkServer(
        currentState != SUPPRESSED,
        getClass(),
        ILLEGAL_FILE_UPDATE_REQUEST,
        "The file with objectId '%s' and analysisId '%s' cannot "
            + "be updated since its analysisState is '%s'",
        objectId,
        analysisId,
        SUPPRESSED.toString());

    // Update the target file record using the originalFile and the update request
    val fileUpdateType = updateWithRequest(originalFile, fileUpdateRequest);

    // Build the response
    val response = FileUpdateResponse.builder().unpublishedAnalysis(false);
    response.originalFile(fileConverter.convertToFileDTO(originalFile));
    response.originalAnalysisState(currentState);
    response.fileUpdateType(fileUpdateType);

    // Can only transition from PUBLISHED to UNPUBLISHED states.
    if (currentState == PUBLISHED) {
      if (doUnpublish(fileUpdateType)) {
        analysisService.securedUpdateState(studyId, analysisId, UNPUBLISHED);
        response.unpublishedAnalysis(true);
        response.message(
            format(
                "[WARNING]: Changed analysis from '%s' to '%s'",
                PUBLISHED.toString(), UNPUBLISHED.toString()));
      } else {
        response.message(
            format(
                "Original analysisState '%s' was not changed since the fileUpdateType was '%s'",
                currentState.toString(), fileUpdateType.name()));
      }
    } else if (currentState == UNPUBLISHED) { // Can still update an unpublished analysis
      response.message(
          format("Did not change analysisState since it is '%s'", currentState.toString()));
    } else {
      throw new IllegalStateException(
          format("Could not process the analysisState '%s'", currentState.toString()));
    }
    return response.build();
  }

  /**
   * Validates the file update request is correct, and does not violate any rules
   *
   * @exception ServerException INVALID_FILE_UPDATE_REQUEST
   */
  public void checkFileUpdateRequestValidation(String id, FileData fileUpdateRequest) {
    val validationResponse = validationService.validate(fileUpdateRequest);
    if (validationResponse.isPresent()) {
      throw buildServerException(
          getClass(),
          INVALID_FILE_UPDATE_REQUEST,
          "The file update request for objectId '%s' failed with the following errors: %s",
          id,
          validationResponse.get());
    }
  }

  private FileEntity createUpdateFile(
      @NonNull FileEntity baseFile, @NonNull FileData fileUpdateData) {
    val updatedFile = fileConverter.copyFile(baseFile);
    fileConverter.updateEntityFromData(fileUpdateData, updatedFile);
    return updatedFile;
  }

  /**
   * Decides whether or not the input {@code fileUpdateType} should unpublish an analysis
   *
   * @param fileUpdateType
   * @return boolean
   */
  public static boolean doUnpublish(@NonNull FileUpdateTypes fileUpdateType) {
    if (fileUpdateType == CONTENT_UPDATE) {
      return true;
    } else if (fileUpdateType == METADATA_UPDATE || fileUpdateType == NO_UPDATE) {
      return false;
    } else {
      throw new IllegalStateException(
          format("The updateType '%s' is unrecognized", fileUpdateType.name()));
    }
  }
}
