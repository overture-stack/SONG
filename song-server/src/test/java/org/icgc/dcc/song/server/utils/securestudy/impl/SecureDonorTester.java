package org.icgc.dcc.song.server.utils.securestudy.impl;

import lombok.NonNull;
import lombok.val;
import org.icgc.dcc.song.core.utils.RandomGenerator;
import org.icgc.dcc.song.server.model.entity.Donor;
import org.icgc.dcc.song.server.model.entity.composites.DonorWithSpecimens;
import org.icgc.dcc.song.server.service.DonorService;
import org.icgc.dcc.song.server.service.StudyService;
import org.icgc.dcc.song.server.utils.securestudy.AbstractSecureTester;
import org.icgc.dcc.song.server.utils.securestudy.SecureTestData;

import java.util.function.BiConsumer;

import static com.google.common.collect.Lists.newArrayList;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.DONOR_DOES_NOT_EXIST;
import static org.icgc.dcc.song.server.model.enums.Constants.DONOR_GENDER;

public class SecureDonorTester extends AbstractSecureTester {

  private final DonorService donorService;

  private SecureDonorTester(RandomGenerator randomGenerator,
      StudyService studyService,
      @NonNull DonorService donorService) {
    super(randomGenerator, studyService, DONOR_DOES_NOT_EXIST);
    this.donorService = donorService;
  }

  @Override protected boolean isIdExist(String id) {
    return donorService.isDonorExist(id);
  }

  @Override
  protected String createId(String existingStudyId, Object context) {
    getStudyService().checkStudyExist(existingStudyId);
    val donor = Donor.builder()
        .donorGender(getRandomGenerator().randomElement(newArrayList(DONOR_GENDER)))
        .studyId(existingStudyId)
        .donorSubmitterId(getRandomGenerator().generateRandomUUIDAsString())
        .build();

    val donorCreateRequest = new DonorWithSpecimens();
    donorCreateRequest.setDonor(donor);
    return donorService.create(donorCreateRequest);
  }

  public SecureTestData generateData(){
    return generateData(new Object());
  }

  public SecureTestData runSecureAnalysisTest(BiConsumer<String, String> biConsumer){
      return runSecureTest(biConsumer, new Object() );
  }

  public static SecureDonorTester createSecureDonorTester(RandomGenerator randomGenerator,
      StudyService studyService,
      @NonNull DonorService donorService) {
    return new SecureDonorTester(randomGenerator, studyService, donorService);
  }

}
