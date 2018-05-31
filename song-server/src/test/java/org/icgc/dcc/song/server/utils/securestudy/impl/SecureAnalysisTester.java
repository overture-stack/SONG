package org.icgc.dcc.song.server.utils.securestudy.impl;

import lombok.NonNull;
import lombok.val;
import org.icgc.dcc.song.core.utils.RandomGenerator;
import org.icgc.dcc.song.server.model.enums.AnalysisTypes;
import org.icgc.dcc.song.server.service.AnalysisService;
import org.icgc.dcc.song.server.service.StudyService;
import org.icgc.dcc.song.server.utils.securestudy.AbstractSecureTester;
import org.icgc.dcc.song.server.utils.securestudy.SecureTestData;

import java.util.function.BiConsumer;

import static java.lang.String.format;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.ANALYSIS_ID_NOT_FOUND;
import static org.icgc.dcc.song.server.model.enums.AnalysisTypes.SEQUENCING_READ;
import static org.icgc.dcc.song.server.model.enums.AnalysisTypes.VARIANT_CALL;
import static org.icgc.dcc.song.server.utils.AnalysisGenerator.createAnalysisGenerator;

/**
 * Utility test class that tests study security associated with analysis entities. Ensures that
 * the analysisService method throws the correct error if it is called for an analysis
 * that is unrelated to the supplied studyId.
 */
public class SecureAnalysisTester extends AbstractSecureTester<AnalysisTypes> {

  @NonNull private final AnalysisService analysisService;

  private SecureAnalysisTester(RandomGenerator randomGenerator,
      StudyService studyService,
      AnalysisService analysisService) {
    super(randomGenerator, studyService, ANALYSIS_ID_NOT_FOUND);
    this.analysisService = analysisService;
  }

  @Override protected boolean isIdExist(String id){
    return analysisService.isAnalysisExist(id);
  }

  @Override protected String createId(String existingStudyId, AnalysisTypes analysisType){
    val analysisGenerator = createAnalysisGenerator(existingStudyId, analysisService, getRandomGenerator());

    if (analysisType == SEQUENCING_READ){
      return analysisGenerator.createDefaultRandomSequencingReadAnalysis().getAnalysisId();
    } else if (analysisType == VARIANT_CALL){
      return analysisGenerator.createDefaultRandomVariantCallAnalysis().getAnalysisId();
    } else {
      throw new IllegalStateException(format("The analysisType '%s' cannot be generated", analysisType));
    }
  }

  public static SecureAnalysisTester createSecureAnalysisTester(RandomGenerator randomGenerator,
      StudyService studyService,
      AnalysisService analysisService) {
    return new SecureAnalysisTester(randomGenerator, studyService, analysisService);
  }

  public SecureTestData runSecureAnalysisTest(BiConsumer<String, String> biConsumer){
    return runSecureAnalysisTest(biConsumer, getRandomGenerator().randomEnum(AnalysisTypes.class));
  }

}
