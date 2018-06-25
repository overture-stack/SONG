package org.icgc.dcc.song.server.utils.securestudy.impl;

import lombok.NonNull;
import lombok.val;
import org.icgc.dcc.song.core.utils.RandomGenerator;
import org.icgc.dcc.song.server.model.entity.file.impl.File;
import org.icgc.dcc.song.server.model.enums.AccessTypes;
import org.icgc.dcc.song.server.model.enums.AnalysisTypes;
import org.icgc.dcc.song.server.model.enums.FileTypes;
import org.icgc.dcc.song.server.service.AnalysisService;
import org.icgc.dcc.song.server.service.FileService;
import org.icgc.dcc.song.server.service.StudyService;
import org.icgc.dcc.song.server.utils.securestudy.AbstractSecureTester;
import org.icgc.dcc.song.server.utils.securestudy.SecureTestData;

import java.util.function.BiConsumer;

import static java.lang.String.format;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.FILE_NOT_FOUND;
import static org.icgc.dcc.song.server.utils.securestudy.impl.SecureAnalysisTester.createSecureAnalysisTester;

public class SecureFileTester extends AbstractSecureTester {

  private final FileService fileService;
  private final AnalysisService analysisService;

  private SecureFileTester(RandomGenerator randomGenerator,
      StudyService studyService,
      @NonNull FileService fileService,
      @NonNull AnalysisService analysisService) {
    super(randomGenerator, studyService, FILE_NOT_FOUND);
    this.fileService = fileService;
    this.analysisService = analysisService;
  }

  @Override protected boolean isIdExist(String id) {
    return fileService.isFileExist(id);
  }

  @Override protected String createId(String existingStudyId, Object context) {
    val analysisTester = createSecureAnalysisTester(getRandomGenerator(), getStudyService(), analysisService);
    val analysisData = analysisTester.generateData(getRandomGenerator().randomEnum(AnalysisTypes.class));
    analysisService.checkAnalysisExists(analysisData.getExistingId());
    val existingAnalysisId = analysisData.getExistingId();

    val type = getRandomGenerator().randomEnum(FileTypes.class).toString();
    val file = File.builder()
        .fileAccess(getRandomGenerator().randomEnum(AccessTypes.class).toString())
        .fileMd5sum(getRandomGenerator().generateRandomMD5())
        .fileType(type)
        .fileSize((long)getRandomGenerator().generateRandomIntRange(1000, 1000000))
        .analysisId(existingAnalysisId)
        .studyId(existingStudyId)
        .fileName(format("someFileName.%s.%s",
            getRandomGenerator().generateRandomAsciiString(30), type.toLowerCase()))
        .build();
    return fileService.create(existingAnalysisId, existingStudyId, file);
  }

  public SecureTestData runSecureTest(BiConsumer<String, String> biConsumer) {
    return runSecureTest(biConsumer, new Object());
  }

  public static SecureFileTester createSecureFileTester(RandomGenerator randomGenerator,
      StudyService studyService,
      @NonNull FileService fileService,
      @NonNull AnalysisService analysisService) {
    return new SecureFileTester(randomGenerator, studyService, fileService, analysisService);
  }

}
