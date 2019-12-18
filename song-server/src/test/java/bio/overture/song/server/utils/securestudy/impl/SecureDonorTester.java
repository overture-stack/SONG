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

import static bio.overture.song.core.exceptions.ServerErrors.DONOR_DOES_NOT_EXIST;

import bio.overture.song.core.utils.RandomGenerator;
import bio.overture.song.server.model.entity.Donor;
import bio.overture.song.server.model.entity.composites.DonorWithSpecimens;
import bio.overture.song.server.model.enums.Constants;
import bio.overture.song.server.service.DonorService;
import bio.overture.song.server.service.StudyService;
import bio.overture.song.server.utils.securestudy.AbstractSecureTester;
import bio.overture.song.server.utils.securestudy.SecureTestData;
import com.google.common.collect.Lists;
import java.util.function.BiConsumer;
import lombok.NonNull;
import lombok.val;

public class SecureDonorTester extends AbstractSecureTester {

  private final DonorService donorService;

  private SecureDonorTester(
      RandomGenerator randomGenerator,
      StudyService studyService,
      @NonNull DonorService donorService) {
    super(randomGenerator, studyService, DONOR_DOES_NOT_EXIST);
    this.donorService = donorService;
  }

  @Override
  protected boolean isIdExist(String id) {
    return donorService.isDonorExist(id);
  }

  @Override
  protected String createId(String existingStudyId, Object context) {
    getStudyService().checkStudyExist(existingStudyId);
    val donor =
        Donor.builder()
            .donorGender(
                getRandomGenerator().randomElement(Lists.newArrayList(Constants.DONOR_GENDER)))
            .studyId(existingStudyId)
            .submitterDonorId(getRandomGenerator().generateRandomUUIDAsString())
            .build();

    val donorCreateRequest = new DonorWithSpecimens();
    donorCreateRequest.setDonor(donor);
    return donorService.create(donorCreateRequest);
  }

  public SecureTestData generateData() {
    return generateData(new Object());
  }

  public SecureTestData runSecureAnalysisTest(BiConsumer<String, String> biConsumer) {
    return runSecureTest(biConsumer, new Object());
  }

  public static SecureDonorTester createSecureDonorTester(
      RandomGenerator randomGenerator,
      StudyService studyService,
      @NonNull DonorService donorService) {
    return new SecureDonorTester(randomGenerator, studyService, donorService);
  }
}
