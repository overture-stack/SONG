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

import bio.overture.song.core.model.enums.AccessTypes;
import bio.overture.song.core.model.enums.FileTypes;
import bio.overture.song.core.utils.RandomGenerator;
import bio.overture.song.server.model.entity.FileEntity;
import bio.overture.song.server.service.AnalysisService;
import bio.overture.song.server.service.FileService;
import bio.overture.song.server.service.StudyService;
import bio.overture.song.server.utils.generator.LegacyAnalysisTypeName;
import bio.overture.song.server.utils.securestudy.AbstractSecureTester;
import bio.overture.song.server.utils.securestudy.SecureTestData;
import lombok.NonNull;
import lombok.val;

import java.util.function.BiConsumer;

import static java.lang.String.format;
import static bio.overture.song.core.exceptions.ServerErrors.FILE_NOT_FOUND;

public class SecureFileTester extends AbstractSecureTester {

  private final FileService fileService;
  private final AnalysisService analysisService;

  private SecureFileTester(
      RandomGenerator randomGenerator,
      StudyService studyService,
      @NonNull FileService fileService,
      @NonNull AnalysisService analysisService) {
    super(randomGenerator, studyService, FILE_NOT_FOUND);
    this.fileService = fileService;
    this.analysisService = analysisService;
  }

  @Override
  protected boolean isIdExist(String id) {
    return fileService.isFileExist(id);
  }

  @Override
  protected String createId(String existingStudyId, Object context) {
    val analysisTester =
        SecureAnalysisTester.createSecureAnalysisTester(
            getRandomGenerator(), getStudyService(), analysisService);
    val analysisData =
        analysisTester.generateData(getRandomGenerator().randomEnum(LegacyAnalysisTypeName.class));
    analysisService.checkAnalysisExists(analysisData.getExistingId());
    val existingAnalysisId = analysisData.getExistingId();

    val type = getRandomGenerator().randomEnum(FileTypes.class).toString();
    val file =
        FileEntity.builder()
            .fileAccess(getRandomGenerator().randomEnum(AccessTypes.class).toString())
            .fileMd5sum(getRandomGenerator().generateRandomMD5())
            .fileType(type)
            .fileSize((long) getRandomGenerator().generateRandomIntRange(1000, 1000000))
            .analysisId(existingAnalysisId)
            .studyId(existingStudyId)
            .fileName(
                format(
                    "someFileName.%s.%s",
                    getRandomGenerator().generateRandomAsciiString(30), type.toLowerCase()))
            .build();
    return fileService.create(existingAnalysisId, existingStudyId, file);
  }

  public SecureTestData runSecureTest(BiConsumer<String, String> biConsumer) {
    return runSecureTest(biConsumer, new Object());
  }

  public static SecureFileTester createSecureFileTester(
      RandomGenerator randomGenerator,
      StudyService studyService,
      @NonNull FileService fileService,
      @NonNull AnalysisService analysisService) {
    return new SecureFileTester(randomGenerator, studyService, fileService, analysisService);
  }
}
