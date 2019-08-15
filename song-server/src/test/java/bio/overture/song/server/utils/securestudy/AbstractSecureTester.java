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

package bio.overture.song.server.utils.securestudy;

import static bio.overture.song.core.exceptions.ServerErrors.ENTITY_NOT_RELATED_TO_STUDY;
import static bio.overture.song.core.exceptions.ServerErrors.STUDY_ID_DOES_NOT_EXIST;
import static bio.overture.song.server.utils.generator.StudyGenerator.createStudyGenerator;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import bio.overture.song.core.exceptions.ServerError;
import bio.overture.song.core.testing.SongErrorAssertions;
import bio.overture.song.core.utils.RandomGenerator;
import bio.overture.song.server.service.StudyService;
import java.util.function.BiConsumer;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

@Getter
@RequiredArgsConstructor
public abstract class AbstractSecureTester<C> {

  @NonNull private final RandomGenerator randomGenerator;
  @NonNull private final StudyService studyService;
  @NonNull private final ServerError idNotFoundError;

  /**
   * Reliably generates existing, nonExisting, and unRelated ids for study and entities.
   *
   * @return
   */
  public SecureTestData generateData(C context) {
    val studyGenerator = createStudyGenerator(studyService, randomGenerator);

    val existingStudyId = studyGenerator.createRandomStudy();
    val unrelatedExistingStudyId = studyGenerator.createRandomStudy();
    val nonExistingStudyId = studyGenerator.generateNonExistingStudyId();

    val nonExistingId = randomGenerator.generateRandomUUIDAsString();
    assertFalse(isIdExist(nonExistingId));

    val existingId = createId(existingStudyId, context);

    return SecureTestData.builder()
        .existingStudyId(existingStudyId)
        .nonExistingStudyId(nonExistingStudyId)
        .unrelatedExistingStudyId(unrelatedExistingStudyId)
        .nonExistingId(nonExistingId)
        .existingId(existingId)
        .build();
  }

  /**
   * Generates data for an input {@code context} and then runs a secure study test for the input
   * {@code biConsumer} which represents the secured service method under test
   *
   * @param biConsumer with the first parameter being the {@code studyId} and the second being the
   *     entity's {@code id}
   */
  public SecureTestData runSecureTest(BiConsumer<String, String> biConsumer, C context) {
    // Create data
    val data = generateData(context);
    return runSecureTest(biConsumer, data);
  }

  /**
   * Runs a secure study test for the input {@code biConsumer} which represents the secured service
   * method under test, using the input {@code data}
   *
   * @param biConsumer with the first parameter being the {@code studyId} and the second being the
   *     entity's {@code id}
   * @param data is the input stimulus
   */
  public SecureTestData runSecureTest(BiConsumer<String, String> biConsumer, SecureTestData data) {

    // Check data is correct
    assertTrue(isIdExist(data.getExistingId()));
    assertFalse(isIdExist(data.getNonExistingId()));
    assertTrue(studyService.isStudyExist(data.getExistingStudyId()));
    assertTrue(studyService.isStudyExist(data.getUnrelatedExistingStudyId()));
    assertFalse(studyService.isStudyExist(data.getNonExistingStudyId()));

    // Test if study exists and id DNE
    SongErrorAssertions.assertSongErrorRunnable(
        () -> biConsumer.accept(data.getExistingStudyId(), data.getNonExistingId()),
        idNotFoundError);

    // Test if study DNE (does not exist) but id exists
    SongErrorAssertions.assertSongErrorRunnable(
        () -> biConsumer.accept(data.getNonExistingStudyId(), data.getExistingId()),
        STUDY_ID_DOES_NOT_EXIST);

    // Test if study DNE (does not exist) and id DNE
    SongErrorAssertions.assertSongErrorRunnable(
        () -> biConsumer.accept(data.getNonExistingStudyId(), data.getNonExistingId()),
        STUDY_ID_DOES_NOT_EXIST);

    // Test if correct error thrown is both studyId and id exist but are unrelated
    SongErrorAssertions.assertSongErrorRunnable(
        () -> biConsumer.accept(data.getUnrelatedExistingStudyId(), data.getExistingId()),
        ENTITY_NOT_RELATED_TO_STUDY);

    return data;
  }

  protected abstract boolean isIdExist(String id);

  protected abstract String createId(String existingStudyId, C context);
}
