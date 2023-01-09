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

package bio.overture.song.server.utils.securestudy.impl;

import static bio.overture.song.core.exceptions.ServerErrors.SAMPLE_DOES_NOT_EXIST;

import bio.overture.song.core.utils.RandomGenerator;
import bio.overture.song.server.model.entity.Sample;
import bio.overture.song.server.service.SampleService;
import bio.overture.song.server.service.SpecimenService;
import bio.overture.song.server.service.StudyService;
import bio.overture.song.server.utils.TestConstants;
import bio.overture.song.server.utils.securestudy.AbstractSecureTester;
import com.google.common.collect.Lists;
import lombok.NonNull;
import lombok.val;

public class SecureSampleTester extends AbstractSecureTester<String> {

  private final SpecimenService specimenService;
  private final SampleService sampleService;

  private SecureSampleTester(
      RandomGenerator randomGenerator,
      StudyService studyService,
      @NonNull SpecimenService specimenService,
      @NonNull SampleService sampleService) {
    super(randomGenerator, studyService, SAMPLE_DOES_NOT_EXIST);
    this.specimenService = specimenService;
    this.sampleService = sampleService;
  }

  @Override
  protected boolean isIdExist(String id) {
    return sampleService.isSampleExist(id);
  }

  @Override
  protected String createId(String existingStudyId, String specimenId) {
    specimenService.checkSpecimenExist(specimenId);
    val sample =
        Sample.builder()
            .submitterSampleId(getRandomGenerator().generateRandomUUIDAsString())
            .sampleType(
                getRandomGenerator().randomElement(Lists.newArrayList(TestConstants.SAMPLE_TYPE)))
            .specimenId(specimenId)
            .build();
    return sampleService.create(existingStudyId, sample);
  }

  public static SecureSampleTester createSecureSampleTester(
      RandomGenerator randomGenerator,
      StudyService studyService,
      @NonNull SpecimenService specimenService,
      @NonNull SampleService sampleService) {
    return new SecureSampleTester(randomGenerator, studyService, specimenService, sampleService);
  }
}
