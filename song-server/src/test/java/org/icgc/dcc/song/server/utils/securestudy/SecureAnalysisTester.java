package org.icgc.dcc.song.server.utils.securestudy;

import lombok.Builder;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.icgc.dcc.song.core.utils.RandomGenerator;
import org.icgc.dcc.song.server.model.enums.AnalysisTypes;
import org.icgc.dcc.song.server.service.AnalysisService;
import org.icgc.dcc.song.server.service.StudyService;

import java.util.function.BiConsumer;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.ANALYSIS_ID_NOT_FOUND;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.ENTITY_NOT_RELATED_TO_STUDY;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.STUDY_ID_DOES_NOT_EXIST;
import static org.icgc.dcc.song.core.testing.SongErrorAssertions.assertSongError;
import static org.icgc.dcc.song.server.model.enums.AnalysisTypes.SEQUENCING_READ;
import static org.icgc.dcc.song.server.model.enums.AnalysisTypes.VARIANT_CALL;
import static org.icgc.dcc.song.server.utils.AnalysisGenerator.createAnalysisGenerator;
import static org.icgc.dcc.song.server.utils.StudyGenerator.createStudyGenerator;

/**
 * Utility test class that tests study security associated with analysis entities. Ensures that
 * the analysisService method throws the correct error if it is called for an analysis
 * that is unrelated to the supplied studyId.
 */
@Builder
@RequiredArgsConstructor
public class SecureAnalysisTester {
  @NonNull private final RandomGenerator randomGenerator;
  @NonNull private final StudyService studyService;
  @NonNull private final AnalysisService analysisService;

  /**
   * Reliably generates existing, nonExisting, and unRelated ids for study and analysis entities.
   * @param analysisType
   * @return
   */
  public SecureAnalysisTestData generateData(AnalysisTypes analysisType){
    val studyGenerator = createStudyGenerator(studyService, randomGenerator);

    val existingStudyId = studyGenerator.createRandomStudy();
    val unrelatedExistingStudyId = studyGenerator.createRandomStudy();
    val nonExistingStudyId = studyGenerator.generateNonExistingStudyId();


    val analysisGenerator = createAnalysisGenerator(existingStudyId, analysisService, randomGenerator);
    val nonExistingAnalysisId = randomGenerator.generateRandomUUIDAsString();
    assertThat(analysisService.isAnalysisExist(nonExistingAnalysisId)).isFalse();
    String existingAnalysisId;
    if (analysisType == SEQUENCING_READ){
      existingAnalysisId = analysisGenerator.createDefaultRandomSequencingReadAnalysis().getAnalysisId();
    } else if (analysisType == VARIANT_CALL){
      existingAnalysisId = analysisGenerator.createDefaultRandomVariantCallAnalysis().getAnalysisId();
    } else {
      throw new IllegalStateException(format("The analysisType '%s' cannot be generated", analysisType));
    }

    return SecureAnalysisTestData.builder()
        .existingStudyId(existingStudyId)
        .nonExistingStudyId(nonExistingStudyId)
        .unrelatedExistingStudyId(unrelatedExistingStudyId)
        .nonExistingAnalysisId(nonExistingAnalysisId)
        .existingAnalysisId(existingAnalysisId)
        .build();
  }

  /**
   * Same as the {@link org.icgc.dcc.song.server.utils.securestudy.SecureAnalysisTester#runSecureAnalysisTest(BiConsumer, AnalysisTypes)}
   * method however the analysisType is randomized
   * @param biConsumer
   */
  public SecureAnalysisTestData runSecureAnalysisTest(BiConsumer<String, String> biConsumer){
    return runSecureAnalysisTest(biConsumer, randomGenerator.randomEnum(AnalysisTypes.class));
  }

  /**
   * Generates data for an input {@code analysisType} and then runs a secure study test for the
   * input {@code biConsumer} which represents the secured service method under test
   * @param biConsumer with the first parameter being the {@code studyId} and the second being the {@code analysisId}
   * @param analysisType is needed to generate the correct data
   */
  public SecureAnalysisTestData runSecureAnalysisTest(BiConsumer<String, String> biConsumer, AnalysisTypes analysisType){
    // Create data
    val data = generateData(analysisType);
    return runSecureAnalysisTest(biConsumer, data);
  }

  /**
   * Runs a secure study test for the input {@code biConsumer} which represents the secured service method under test,
   * using the input {@code data}
   * @param biConsumer with the first parameter being the {@code studyId} and the second being the {@code analysisId}
   * @param data is the input stimulus
   */
  public SecureAnalysisTestData runSecureAnalysisTest(BiConsumer<String, String> biConsumer, SecureAnalysisTestData data){

    // Check data is correct
    assertThat(analysisService.isAnalysisExist(data.getExistingAnalysisId())).isTrue();
    assertThat(analysisService.isAnalysisExist(data.getNonExistingAnalysisId())).isFalse();
    assertThat(studyService.isStudyExist(data.getExistingStudyId())).isTrue();
    assertThat(studyService.isStudyExist(data.getUnrelatedExistingStudyId())).isTrue();
    assertThat(studyService.isStudyExist(data.getNonExistingStudyId())).isFalse();

    // Test if study exists and analysisId DNE
    assertSongError( () -> biConsumer.accept(data.getExistingStudyId(), data.getNonExistingAnalysisId()),
        ANALYSIS_ID_NOT_FOUND);

    // Test if study DNE (does not exist) but analysisId exists
    assertSongError( () -> biConsumer.accept(data.getNonExistingStudyId(), data.getExistingAnalysisId()),
        STUDY_ID_DOES_NOT_EXIST);

    // Test if study DNE (does not exist) and analysisId DNE
    assertSongError( () -> biConsumer.accept(data.getNonExistingStudyId(), data.getNonExistingAnalysisId()),
        STUDY_ID_DOES_NOT_EXIST);

    // Test if correct error thrown is both studyId and analysisId exist but are unrelated
    assertSongError( () -> biConsumer.accept(data.getUnrelatedExistingStudyId(), data.getExistingAnalysisId()),
        ENTITY_NOT_RELATED_TO_STUDY);

    return data;
  }

}
