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

import static bio.overture.song.core.exceptions.ServerErrors.ANALYSIS_ID_NOT_FOUND;
import static bio.overture.song.server.utils.generator.AnalysisGenerator.createAnalysisGenerator;

import bio.overture.song.core.utils.RandomGenerator;
import bio.overture.song.server.service.AnalysisService2;
import bio.overture.song.server.service.StudyService;
import bio.overture.song.server.utils.generator.LegacyAnalysisTypeName;
import bio.overture.song.server.utils.securestudy.AbstractSecureTester;
import bio.overture.song.server.utils.securestudy.SecureTestData;
import java.util.function.BiConsumer;
import lombok.NonNull;
import lombok.val;

/**
 * Utility test class that tests study security associated with analysis entities. Ensures that the
 * analysisService method throws the correct error if it is called for an analysis that is unrelated
 * to the supplied studyId.
 */
public class SecureAnalysisTester extends AbstractSecureTester<LegacyAnalysisTypeName> {

  @NonNull private final AnalysisService2 analysisService;

  private SecureAnalysisTester(
      RandomGenerator randomGenerator,
      StudyService studyService,
      AnalysisService2 analysisService) {
    super(randomGenerator, studyService, ANALYSIS_ID_NOT_FOUND);
    this.analysisService = analysisService;
  }

  @Override
  protected boolean isIdExist(String id) {
    return analysisService.isAnalysisExist(id);
  }

  @Override
  protected String createId(String existingStudyId, LegacyAnalysisTypeName legacyAnalysisTypeName) {
    val analysisGenerator =
        createAnalysisGenerator(existingStudyId, analysisService, getRandomGenerator());
    return analysisGenerator.createDefaultRandomAnalysis(legacyAnalysisTypeName).getAnalysisId();
  }

  public static SecureAnalysisTester createSecureAnalysisTester(
      RandomGenerator randomGenerator,
      StudyService studyService,
      AnalysisService2 analysisService) {
    return new SecureAnalysisTester(randomGenerator, studyService, analysisService);
  }

  public SecureTestData runSecureTest(BiConsumer<String, String> biConsumer) {
    return runSecureTest(biConsumer, getRandomGenerator().randomEnum(LegacyAnalysisTypeName.class));
  }
}
