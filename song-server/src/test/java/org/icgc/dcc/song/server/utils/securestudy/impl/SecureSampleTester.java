package org.icgc.dcc.song.server.utils.securestudy.impl;

import lombok.NonNull;
import lombok.val;
import org.icgc.dcc.song.core.utils.RandomGenerator;
import org.icgc.dcc.song.server.model.entity.Sample;
import org.icgc.dcc.song.server.service.SampleService;
import org.icgc.dcc.song.server.service.SpecimenService;
import org.icgc.dcc.song.server.service.StudyService;
import org.icgc.dcc.song.server.utils.securestudy.AbstractSecureTester;

import static com.google.common.collect.Lists.newArrayList;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.SAMPLE_DOES_NOT_EXIST;
import static org.icgc.dcc.song.server.model.enums.Constants.SAMPLE_TYPE;

public class SecureSampleTester extends AbstractSecureTester<String>{

  private final SpecimenService specimenService;
  private final SampleService sampleService;

  private SecureSampleTester(RandomGenerator randomGenerator,
      StudyService studyService,
      @NonNull SpecimenService specimenService,
      @NonNull SampleService sampleService) {
    super(randomGenerator, studyService, SAMPLE_DOES_NOT_EXIST);
    this.specimenService = specimenService;
    this.sampleService = sampleService;
  }

  @Override protected boolean isIdExist(String id) {
    return sampleService.isSampleExist(id);
  }

  @Override protected String createId(String existingStudyId, String specimenId) {
    specimenService.checkSpecimenExist(specimenId);
    val sample = Sample.builder()
        .sampleSubmitterId(getRandomGenerator().generateRandomUUIDAsString())
        .sampleType(getRandomGenerator().randomElement(newArrayList(SAMPLE_TYPE)))
        .specimenId(specimenId)
        .build();
    return sampleService.create(existingStudyId, sample);
  }

  public static SecureSampleTester createSecureSampleTester(RandomGenerator randomGenerator,
      StudyService studyService,
      @NonNull SpecimenService specimenService,
      @NonNull SampleService sampleService) {
    return new SecureSampleTester(randomGenerator, studyService, specimenService, sampleService);
  }

}
