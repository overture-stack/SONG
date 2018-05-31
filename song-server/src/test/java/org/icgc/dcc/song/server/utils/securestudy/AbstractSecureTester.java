package org.icgc.dcc.song.server.utils.securestudy;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.icgc.dcc.song.core.exceptions.ServerError;
import org.icgc.dcc.song.core.utils.RandomGenerator;
import org.icgc.dcc.song.server.service.StudyService;

import java.util.function.BiConsumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.ENTITY_NOT_RELATED_TO_STUDY;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.STUDY_ID_DOES_NOT_EXIST;
import static org.icgc.dcc.song.core.testing.SongErrorAssertions.assertSongError;
import static org.icgc.dcc.song.server.utils.StudyGenerator.createStudyGenerator;

@Getter
@RequiredArgsConstructor
public abstract class AbstractSecureTester<C> {

  @NonNull private final RandomGenerator randomGenerator;
  @NonNull private final StudyService studyService;
  @NonNull private final ServerError idNotFoundError;

  /**
   * Reliably generates existing, nonExisting, and unRelated ids for study and analysis entities.
   * @return
   */
  public SecureTestData generateData(C context){
    val studyGenerator = createStudyGenerator(studyService, randomGenerator);

    val existingStudyId = studyGenerator.createRandomStudy();
    val unrelatedExistingStudyId = studyGenerator.createRandomStudy();
    val nonExistingStudyId = studyGenerator.generateNonExistingStudyId();


    val nonExistingAnalysisId = randomGenerator.generateRandomUUIDAsString();
    assertThat(isIdExist(nonExistingAnalysisId)).isFalse();

    val existingAnalysisId = createId(existingStudyId, context);

    return SecureTestData.builder()
        .existingStudyId(existingStudyId)
        .nonExistingStudyId(nonExistingStudyId)
        .unrelatedExistingStudyId(unrelatedExistingStudyId)
        .nonExistingId(nonExistingAnalysisId)
        .existingId(existingAnalysisId)
        .build();
  }

  /**
   * Generates data for an input {@code analysisType} and then runs a secure study test for the
   * input {@code biConsumer} which represents the secured service method under test
   * @param biConsumer with the first parameter being the {@code studyId} and the second being the {@code analysisId}
   */
  public SecureTestData runSecureTest(BiConsumer<String, String> biConsumer, C context){
    // Create data
    val data = generateData(context);
    return runSecureTest(biConsumer, data);
  }

  /**
   * Runs a secure study test for the input {@code biConsumer} which represents the secured service method under test,
   * using the input {@code data}
   * @param biConsumer with the first parameter being the {@code studyId} and the second being the {@code analysisId}
   * @param data is the input stimulus
   */
  public SecureTestData runSecureTest(BiConsumer<String, String> biConsumer, SecureTestData data){

    // Check data is correct
    assertThat(isIdExist(data.getExistingId())).isTrue();
    assertThat(isIdExist(data.getNonExistingId())).isFalse();
    assertThat(studyService.isStudyExist(data.getExistingStudyId())).isTrue();
    assertThat(studyService.isStudyExist(data.getUnrelatedExistingStudyId())).isTrue();
    assertThat(studyService.isStudyExist(data.getNonExistingStudyId())).isFalse();

    // Test if study exists and analysisId DNE
    assertSongError( () -> biConsumer.accept(data.getExistingStudyId(), data.getNonExistingId()),
        idNotFoundError);

    // Test if study DNE (does not exist) but analysisId exists
    assertSongError( () -> biConsumer.accept(data.getNonExistingStudyId(), data.getExistingId()),
        STUDY_ID_DOES_NOT_EXIST);

    // Test if study DNE (does not exist) and analysisId DNE
    assertSongError( () -> biConsumer.accept(data.getNonExistingStudyId(), data.getNonExistingId()),
        STUDY_ID_DOES_NOT_EXIST);

    // Test if correct error thrown is both studyId and analysisId exist but are unrelated
    assertSongError( () -> biConsumer.accept(data.getUnrelatedExistingStudyId(), data.getExistingId()),
        ENTITY_NOT_RELATED_TO_STUDY);

    return data;
  }

  protected abstract boolean isIdExist(String id);

  protected abstract String createId(String existingStudyId, C context);
}
