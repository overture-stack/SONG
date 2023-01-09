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

/**
 * Retrieves a canonical ID that maps to the input business keys. All methods that retieve a
 * canonical ID value, return an Optional which indicates its existence
 */
public interface IdService {

  Optional<String> getFileId(String analysisId, String fileName);

  Optional<String> getDonorId(String studyId, String submitterDonorId);

  Optional<String> getSpecimenId(String studyId, String submitterSpecimenId);

  Optional<String> getSampleId(String studyId, String submitterSampleId);

  /** Generates a random UUID */
  String generateAnalysisId();
}
