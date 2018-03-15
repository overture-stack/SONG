package org.icgc.dcc.song.server.utils;

import lombok.RequiredArgsConstructor;
import org.icgc.dcc.song.core.utils.RandomGenerator;
import org.icgc.dcc.song.server.model.entity.Study;
import org.icgc.dcc.song.server.service.StudyService;

import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public class StudyGenerator {

  private final StudyService studyService;
  private final RandomGenerator randomGenerator;

  public String createRandomStudy(){
    boolean studyExists;
    String studyId;
    do {
      studyId = randomGenerator.generateRandomAsciiString(12);
      studyExists = studyService.isStudyExist(studyId);
    } while (studyExists);
    studyService.saveStudy(Study.create(studyId, "", "", ""));
    return studyId;
  }

  public static StudyGenerator createStudyGenerator(StudyService studyService, RandomGenerator randomGenerator) {
    return new StudyGenerator(studyService, randomGenerator);
  }

}
