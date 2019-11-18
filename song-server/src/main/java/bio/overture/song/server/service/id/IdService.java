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

package bio.overture.song.server.service.id;

import java.util.Optional;

public interface IdService {

  Optional<String> resolveFileId(String analysisId, String fileName);

  Optional<String> resolveDonorId(String studyId, String submitterDonorId);

  Optional<String> resolveSpecimenId(String studyId, String submitterSpecimenId);

  Optional<String> resolveSampleId(String studyId, String submitterSampleId);

  /** Indicates if the submitterAnalysisId exists already */
  boolean isAnalysisIdExist(String analysisId);

  /** Generates a random unique analysisId, without persisting */
  String uniqueCandidateAnalysisId();

  /** Idempotent method that creates an analysisId */
  void saveAnalysisId(String submitterAnalysisId);
}
