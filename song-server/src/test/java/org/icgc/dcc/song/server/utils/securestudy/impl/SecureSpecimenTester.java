package org.icgc.dcc.song.server.utils.securestudy.impl;

import lombok.NonNull;
import lombok.val;
import org.icgc.dcc.song.core.utils.RandomGenerator;
import org.icgc.dcc.song.server.model.entity.Specimen;
import org.icgc.dcc.song.server.service.DonorService;
import org.icgc.dcc.song.server.service.SpecimenService;
import org.icgc.dcc.song.server.service.StudyService;
import org.icgc.dcc.song.server.utils.securestudy.AbstractSecureTester;

import static com.google.common.collect.Lists.newArrayList;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.SPECIMEN_DOES_NOT_EXIST;
import static org.icgc.dcc.song.server.model.enums.Constants.SPECIMEN_CLASS;
import static org.icgc.dcc.song.server.model.enums.Constants.SPECIMEN_TYPE;

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
        .specimenType(getRandomGenerator().randomElement(newArrayList(SPECIMEN_TYPE)))
        .specimenClass(getRandomGenerator().randomElement(newArrayList(SPECIMEN_CLASS)))
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
