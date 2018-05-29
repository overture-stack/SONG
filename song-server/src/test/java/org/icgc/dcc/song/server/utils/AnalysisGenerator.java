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
import org.icgc.dcc.song.server.model.analysis.AbstractAnalysis;
import org.icgc.dcc.song.server.model.analysis.SequencingReadAnalysis;
import org.icgc.dcc.song.server.model.analysis.VariantCallAnalysis;
import org.icgc.dcc.song.server.service.AnalysisService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.icgc.dcc.song.server.utils.PayloadGenerator.createPayloadGenerator;
import static org.icgc.dcc.song.server.utils.PayloadGenerator.resolveDefaultPayloadFilename;
import static org.icgc.dcc.song.server.utils.TestFiles.EMPTY_STRING;

@RequiredArgsConstructor
public class AnalysisGenerator {

  @NonNull private final String studyId;
  @NonNull private final AnalysisService service;
  @NonNull private final PayloadGenerator payloadGenerator;


  /**
   * Create a random analysis by specifying the output analysis class type and the payload fixture to load and
   * persist to db
   */
  public <T extends AbstractAnalysis> T createRandomAnalysis(Class<T> analysisClass, String payloadFilename){
    val analysis = payloadGenerator.generateRandomPayload(analysisClass, payloadFilename);
    // Set analysisId to empty to ensure a randomly generated analysisId, and therefore
    // randomly generated objectId (fileIds)
    analysis.setAnalysisId(EMPTY_STRING);
    val analysisId = service.create(studyId, analysis, false);
    val out = analysisClass.cast(service.deepRead(analysisId));
    assertThat(analysis).isExactlyInstanceOf(analysisClass);
    return out;
  }

  /**
   * Creates a default random analysis object in the repository, by loading the default fixture based on the input
   * analysis
   * class type
   */
  public <T extends AbstractAnalysis> T createDefaultRandomAnalysis(Class<T> analysisClass){
    val payloadFilename = resolveDefaultPayloadFilename(analysisClass);
    return createRandomAnalysis(analysisClass, payloadFilename);
  }

  public SequencingReadAnalysis createDefaultRandomSequencingReadAnalysis(){
    return createDefaultRandomAnalysis(SequencingReadAnalysis.class);
  }

  public VariantCallAnalysis createDefaultRandomVariantCallAnalysis(){
    return createDefaultRandomAnalysis(VariantCallAnalysis.class);
  }

  public static AnalysisGenerator createAnalysisGenerator(String studyId, AnalysisService service, PayloadGenerator
      payloadGenerator) {
    return new AnalysisGenerator(studyId, service, payloadGenerator);
  }

  public static AnalysisGenerator createAnalysisGenerator(String studyId, AnalysisService service, RandomGenerator
      randomGenerator) {
    return createAnalysisGenerator(studyId, service, createPayloadGenerator(randomGenerator));
  }

  public static AnalysisGenerator createAnalysisGenerator(String studyId, AnalysisService service, String
      randomGeneratorName) {
    return createAnalysisGenerator(studyId, service, createPayloadGenerator(randomGeneratorName));
  }
}
