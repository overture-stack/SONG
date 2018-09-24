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

package bio.overture.song.server.utils.securestudy.impl;

import bio.overture.song.server.model.entity.Specimen;
import bio.overture.song.server.model.enums.Constants;
import com.google.common.collect.Lists;
import lombok.NonNull;
import lombok.val;
import bio.overture.song.core.utils.RandomGenerator;
import bio.overture.song.server.service.DonorService;
import bio.overture.song.server.service.SpecimenService;
import bio.overture.song.server.service.StudyService;
import bio.overture.song.server.utils.securestudy.AbstractSecureTester;

import static com.google.common.collect.Lists.newArrayList;
import static bio.overture.song.core.exceptions.ServerErrors.SPECIMEN_DOES_NOT_EXIST;

public class SecureSpecimenTester extends AbstractSecureTester<String> {

  private final DonorService donorService;
  private final SpecimenService specimenService;

  private SecureSpecimenTester(RandomGenerator randomGenerator,
      StudyService studyService,
      @NonNull DonorService donorService,
      @NonNull SpecimenService specimenService) {
    super(randomGenerator, studyService, SPECIMEN_DOES_NOT_EXIST);
    this.donorService = donorService;
    this.specimenService = specimenService;
  }

  @Override protected boolean isIdExist(String id) {
    return specimenService.isSpecimenExist(id);
  }

  @Override protected String createId(String existingStudyId, String donorId) {
    donorService.checkDonorExists(donorId);
    val specimen = Specimen.builder()
        .donorId(donorId)
        .specimenSubmitterId(getRandomGenerator().generateRandomUUIDAsString())
        .specimenType(getRandomGenerator().randomElement(Lists.newArrayList(Constants.SPECIMEN_TYPE)))
        .specimenClass(getRandomGenerator().randomElement(Lists.newArrayList(Constants.SPECIMEN_CLASS)))
        .build();
    return specimenService.create(existingStudyId, specimen);
  }

  public static SecureSpecimenTester createSecureSpecimenTester(RandomGenerator randomGenerator,
      StudyService studyService,
      @NonNull DonorService donorService,
      @NonNull SpecimenService specimenService) {
    return new SecureSpecimenTester(randomGenerator, studyService, donorService, specimenService);
  }

}
