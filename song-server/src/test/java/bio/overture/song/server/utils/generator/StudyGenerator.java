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

package bio.overture.song.server.utils.generator;

import static lombok.AccessLevel.PRIVATE;

import bio.overture.song.core.utils.RandomGenerator;
import bio.overture.song.server.model.entity.Study;
import bio.overture.song.server.service.StudyService;
import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor(access = PRIVATE)
public class StudyGenerator {

  private final StudyService studyService;
  private final RandomGenerator randomGenerator;

  public String createRandomStudy() {
    val studyId = generateNonExistingStudyId();
    studyService.saveStudy(
        Study.builder().studyId(studyId).name("").description("").organization("").build());
    return studyId;
  }

  public String generateNonExistingStudyId() {
    boolean studyExists;
    String studyId;
    do {
      studyId = randomGenerator.generateRandomAsciiString(12);
      studyExists = studyService.isStudyExist(studyId);
    } while (studyExists);
    return studyId;
  }

  public static StudyGenerator createStudyGenerator(
      StudyService studyService, RandomGenerator randomGenerator) {
    return new StudyGenerator(studyService, randomGenerator);
  }
}
