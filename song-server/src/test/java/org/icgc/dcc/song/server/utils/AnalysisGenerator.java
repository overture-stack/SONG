package org.icgc.dcc.song.server.utils;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.icgc.dcc.song.core.utils.RandomGenerator;
import org.icgc.dcc.song.server.model.analysis.Analysis;
import org.icgc.dcc.song.server.model.analysis.SequencingReadAnalysis;
import org.icgc.dcc.song.server.model.analysis.VariantCallAnalysis;
import org.icgc.dcc.song.server.service.AnalysisService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.icgc.dcc.song.server.utils.PayloadGenerator.createPayloadGenerator;
import static org.icgc.dcc.song.server.utils.PayloadGenerator.resolveDefaultPayloadFilename;

@RequiredArgsConstructor
public class AnalysisGenerator {

  @NonNull private final String studyId;
  @NonNull private final AnalysisService service;
  @NonNull private final PayloadGenerator payloadGenerator;


  /**
   * Create a random analysis by specifying the output analysis class type and the payload fixture to load and
   * persist to db
   */
  public <T extends Analysis> T createRandomAnalysis(Class<T> analysisClass, String payloadFilename){
    val analysis = payloadGenerator.generateRandomPayload(analysisClass, payloadFilename);
    // Set analysisId to null to ensure a randomly generated analysisId, and therefore
    // randomly generated objectId (fileIds)
    analysis.setAnalysisId(null);
    val analysisId = service.create(studyId, analysis, false);
    val out = analysisClass.cast(service.read(analysisId));
    assertThat(analysis).isExactlyInstanceOf(analysisClass);
    return out;
  }

  /**
   * Creates a default random analysis object in the repository, by loading the default fixture based on the input
   * analysis
   * class type
   */
  public <T extends Analysis> T createDefaultRandomAnalysis(Class<T> analysisClass){
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
