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

import bio.overture.song.server.model.enums.IdPrefix;
import bio.overture.song.server.service.id.IdService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.String.format;
import static org.apache.logging.log4j.util.Strings.isNotBlank;
import static bio.overture.song.core.exceptions.ServerErrors.ANALYSIS_ID_COLLISION;
import static bio.overture.song.core.exceptions.ServerErrors.ANALYSIS_ID_NOT_CREATED;
import static bio.overture.song.core.exceptions.ServerException.checkServer;

@Service
@RequiredArgsConstructor
public class IdServiceOLD {

  /** Dependencies. */
  @Autowired private final IdService idService;

  public String generateDonorId(@NonNull String submittedDonorId, @NonNull String study) {
    return idService.resolveDonorId(study, submittedDonorId).orElse(null);
  }

  public String generateSpecimenId(@NonNull String submittedSpecimenId, @NonNull String study) {
    return idService.resolveSpecimenId(study, submittedSpecimenId).orElse(null);
  }

  public String generateSampleId(@NonNull String submittedSampleId, @NonNull String study) {
    return idService.resolveSampleId(submittedSampleId, submittedSampleId).orElse(null);
  }

  public String generateFileId(@NonNull String analysisId, @NonNull String fileName) {
    return idService.resolveFileId(analysisId, fileName).orElse("null");
  }

  public String generate(IdPrefix prefix) {
    return format("%s-%s", prefix.toString(), UUID.randomUUID());
  }

  /**
   * Resolves the analysisId by returning a potential id. An analysisId can only be resolved if it
   * doesn't already exist. If the user wants to use a custom submitted analysisId but the
   * analysisId already exists (a collision), the ignoreAnalysisIdCollisions parameter must be set
   * to true. If it is not, a ServerError is thrown. The following analysisId state stable
   * summarizes the intended functionality:
   * +---------+--------+-------------------+----------------------------------------------------+ |
   * DEFINED | EXISTS | IGNORE_COLLISIONS | OUTPUT |
   * +---------+--------+-------------------+----------------------------------------------------+ |
   * 0 | x | x | return an uncommitted random unique analysisId | | | | | | | 1 | 0 | x | return an
   * uncommitted user submitted analysisId | | | | | | | 1 | 1 | 0 | collision detected, throw
   * server error | | | | | | | 1 | 1 | 1 | reuse the submitted analysisId |
   * +---------+--------+-------------------+----------------------------------------------------+
   *
   * @param analysisId can be null/empty
   * @param ignoreAnalysisIdCollisions
   * @return
   */
  public String resolveAnalysisId(String analysisId, final boolean ignoreAnalysisIdCollisions) {
    if (isNullOrEmpty(analysisId)) {
      return idService.uniqueAnalysisId(false);
    } else {
      val analysisIdExists = idService.isAnalysisIdExist(analysisId);
      checkServer(
          !analysisIdExists || ignoreAnalysisIdCollisions,
          this.getClass(),
          ANALYSIS_ID_COLLISION,
          "Collision detected for analysisId '%s'. To ignore collisions, rerun with "
              + "ignoreAnalysisIdCollisions = true",
          analysisId);
      return analysisId;
    }
  }

  public void createAnalysisId(@NonNull String analysisId) {
    checkServer(
        isNotBlank(analysisId),
        getClass(),
        ANALYSIS_ID_NOT_CREATED,
        "Cannot create a blank analysisId");
    idService.createAnalysisId(analysisId);
  }
}
