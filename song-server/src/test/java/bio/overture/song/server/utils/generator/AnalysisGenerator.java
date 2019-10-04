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

package bio.overture.song.server.utils.generator;

import static bio.overture.song.core.utils.RandomGenerator.createRandomGenerator;
import static bio.overture.song.server.utils.generator.PayloadGenerator.createPayloadGenerator;
import static bio.overture.song.server.utils.generator.PayloadGenerator.resolveDefaultPayloadFilename;
import static lombok.AccessLevel.PRIVATE;
import static org.junit.Assert.assertNotNull;

import bio.overture.song.core.utils.RandomGenerator;
import bio.overture.song.server.model.analysis.Analysis;
import bio.overture.song.server.model.dto.Payload;
import bio.overture.song.server.service.AnalysisService;
import bio.overture.song.server.utils.TestFiles;
import java.util.function.Supplier;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor(access = PRIVATE)
public class AnalysisGenerator {

  @NonNull private final String studyId;
  @NonNull private final AnalysisService service;
  @NonNull private final PayloadGenerator payloadGenerator;
  @NonNull private final RandomGenerator randomGenerator;

  /**
   * Create a random analysis by specifying the output analysis class type and the payload fixture
   * to load and persist to db
   */
  public Analysis createRandomAnalysis(@NonNull String payloadFilename) {
    return createRandomAnalysis(() -> payloadGenerator.generateRandomPayload(payloadFilename));
  }

  public Analysis createRandomAnalysis(@NonNull Supplier<Payload> payloadSupplier) {
    val payload = payloadSupplier.get();
    // Set analysisId to empty to ensure a randomly generated analysisId, and therefore
    // randomly generated objectId (fileIds)
    payload.setAnalysisId(TestFiles.EMPTY_STRING);
    val analysisId = service.create(studyId, payload, false);
    return service.securedDeepRead(studyId, analysisId);
  }

  public String generateNonExistingAnalysisId() {
    boolean idExists = true;
    String id = null;
    while (idExists) {
      id = randomGenerator.generateRandomUUIDAsString();
      idExists = service.isAnalysisExist(id);
    }
    assertNotNull(id);
    return id;
  }

  /**
   * Creates a default random analysis object in the repository, by loading the default fixture
   * based on the input analysis class type
   */
  public Analysis createDefaultRandomAnalysis(LegacyAnalysisTypeName legacyAnalysisTypeName) {
    val payloadFilename = resolveDefaultPayloadFilename(legacyAnalysisTypeName);
    return createRandomAnalysis(payloadFilename);
  }

  public Analysis createDefaultRandomSequencingReadAnalysis() {
    return createDefaultRandomAnalysis(LegacyAnalysisTypeName.SEQUENCING_READ);
  }

  public Analysis createDefaultRandomVariantCallAnalysis() {
    return createDefaultRandomAnalysis(LegacyAnalysisTypeName.VARIANT_CALL);
  }

  public static AnalysisGenerator createAnalysisGenerator(
      String studyId, AnalysisService service, RandomGenerator randomGenerator) {
    return new AnalysisGenerator(
        studyId, service, createPayloadGenerator(randomGenerator), randomGenerator);
  }

  public static AnalysisGenerator createAnalysisGenerator(
      String studyId, AnalysisService service, String randomGeneratorName) {
    val randomGenerator = createRandomGenerator(randomGeneratorName);
    return createAnalysisGenerator(studyId, service, randomGenerator);
  }
}
