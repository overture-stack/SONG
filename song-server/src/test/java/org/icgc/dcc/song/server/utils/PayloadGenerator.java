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

package org.icgc.dcc.song.server.utils;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.icgc.dcc.song.core.utils.RandomGenerator;
import org.icgc.dcc.song.server.model.analysis.Analysis;
import org.icgc.dcc.song.server.model.analysis.SequencingReadAnalysis;
import org.icgc.dcc.song.server.model.analysis.VariantCallAnalysis;

import static lombok.AccessLevel.PRIVATE;
import static org.assertj.core.api.Assertions.fail;
import static org.icgc.dcc.song.core.utils.JsonUtils.fromJson;
import static org.icgc.dcc.song.core.utils.RandomGenerator.createRandomGenerator;
import static org.icgc.dcc.song.server.utils.TestFiles.EMPTY_STRING;
import static org.icgc.dcc.song.server.utils.TestFiles.getJsonStringFromClasspath;

@RequiredArgsConstructor(access = PRIVATE)
public class PayloadGenerator {

  @NonNull private final RandomGenerator randomGenerator;

  /**
   * Generates a random analysis by using a template payload json, and then
   * randomizing the business keys, which are the sampleSubmitterId, donorSubmitterId, and specimenSubmitterId.
   * It also sets the ana
   */
  public <T extends Analysis> T generateRandomPayload(Class<T> analysisClass, String payloadFilename){
    val json = getJsonStringFromClasspath(payloadFilename);
    val analysis = fromJson(json, analysisClass);
    analysis.setAnalysisId(EMPTY_STRING);
    analysis.getSample().forEach(x -> {
      x.setSampleSubmitterId(randomGenerator.generateRandomUUID().toString());
      x.getSpecimen().setSpecimenSubmitterId(randomGenerator.generateRandomUUID().toString());
      x.getDonor().setDonorSubmitterId(randomGenerator.generateRandomUUID().toString());
    });
    return analysis;
  }

  /**
   * Based on the input analysis class type, the correct payload fixture filename is returned.
   */
  public static <T extends Analysis> String resolveDefaultPayloadFilename(Class<T> analysisClass){
    String payloadFilename = null;
    if (analysisClass.equals(SequencingReadAnalysis.class)){
      payloadFilename = "documents/sequencingread-valid.json";
    } else if (analysisClass.equals(VariantCallAnalysis.class)) {
      payloadFilename = "documents/variantcall-valid.json";
    } else {
      fail("Shouldnt be here");
    }
    return payloadFilename;
  }

  /**
   * Loads at default fixture depending on the input analysis class type, and returns it as an analysis object
   * of the correct type. The returned analysis is not persisted in the repository nor is it complete
   */
  public <T extends Analysis> T generateDefaultRandomPayload(Class<T> analysisClass ){
    val payloadFilename = PayloadGenerator.resolveDefaultPayloadFilename(analysisClass);
    return generateRandomPayload(analysisClass, payloadFilename);
  }

  public static PayloadGenerator createPayloadGenerator(RandomGenerator randomGenerator) {
    return new PayloadGenerator(randomGenerator);
  }

  public static PayloadGenerator createPayloadGenerator(String randomGeneratorName) {
    return new PayloadGenerator(createRandomGenerator(randomGeneratorName));
  }

}
