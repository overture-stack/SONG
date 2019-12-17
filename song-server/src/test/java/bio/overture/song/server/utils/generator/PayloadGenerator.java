/*
 * Copyright (c) 2019. Ontario Institute for Cancer Research
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

import bio.overture.song.core.utils.RandomGenerator;
import bio.overture.song.server.model.dto.Payload;
import bio.overture.song.server.utils.TestFiles;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

import static lombok.AccessLevel.PRIVATE;
import static org.junit.Assert.fail;
import static bio.overture.song.core.utils.JsonUtils.fromJson;
import static bio.overture.song.core.utils.RandomGenerator.createRandomGenerator;
import static bio.overture.song.server.model.enums.ModelAttributeNames.STUDY_ID;
import static bio.overture.song.server.utils.generator.LegacyAnalysisTypeName.SEQUENCING_READ;
import static bio.overture.song.server.utils.generator.LegacyAnalysisTypeName.VARIANT_CALL;

@RequiredArgsConstructor(access = PRIVATE)
public class PayloadGenerator {

  @NonNull private final RandomGenerator randomGenerator;

  /**
   * Generates a random analysis by using a template payload json, and then randomizing the business
   * keys, which are the sampleSubmitterId, donorSubmitterId, and specimenSubmitterId. It also sets
   * the ana
   */
  public Payload generateRandomPayload(String payloadFilename) {
    val json = TestFiles.getJsonStringFromClasspath(payloadFilename);
    val payload = fromJson(json, Payload.class);
    payload
        .getSample()
        .forEach(
            x -> {
              x.setSampleSubmitterId(randomGenerator.generateRandomUUID().toString());
              x.getSpecimen()
                  .setSpecimenSubmitterId(randomGenerator.generateRandomUUID().toString());
              x.getDonor().setDonorSubmitterId(randomGenerator.generateRandomUUID().toString());
            });
    return payload;
  }

  /** Based on the input analysis class type, the correct payload fixture filename is returned. */
  public static String resolveDefaultPayloadFilename(
      LegacyAnalysisTypeName legacyAnalysisTypeName) {
    String payloadFilename = null;
    if (legacyAnalysisTypeName == SEQUENCING_READ) {
      payloadFilename = "documents/sequencingread-valid.json";
    } else if (legacyAnalysisTypeName == VARIANT_CALL) {
      payloadFilename = "documents/variantcall-valid.json";
    } else {
      fail("Shouldnt be here");
    }
    return payloadFilename;
  }

  /**
   * Loads at default fixture depending on the input analysis class type, and returns it as an
   * analysis object of the correct type. The returned analysis is not persisted in the repository
   * nor is it complete
   */
  public Payload generateDefaultRandomPayload(LegacyAnalysisTypeName legacyAnalysisTypeName) {
    val payloadFilename = PayloadGenerator.resolveDefaultPayloadFilename(legacyAnalysisTypeName);
    return generateRandomPayload(payloadFilename);
  }

  public static PayloadGenerator createPayloadGenerator(RandomGenerator randomGenerator) {
    return new PayloadGenerator(randomGenerator);
  }

  public static PayloadGenerator createPayloadGenerator(String randomGeneratorName) {
    return new PayloadGenerator(createRandomGenerator(randomGeneratorName));
  }

  public static void updateStudyInPayload(JsonNode payload, String studyId) {
    ((ObjectNode) payload).put(STUDY_ID, studyId);
  }
}
