package org.icgc.dcc.song.server.service.export;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableList;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.val;
import org.icgc.dcc.song.core.model.ExportedPayload;
import org.icgc.dcc.song.server.model.analysis.Analysis;
import org.icgc.dcc.song.server.model.analysis.SequencingReadAnalysis;
import org.icgc.dcc.song.server.model.analysis.VariantCallAnalysis;
import org.icgc.dcc.song.server.service.AnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static java.util.stream.Collectors.groupingBy;
import static org.icgc.dcc.common.core.util.stream.Collectors.toImmutableList;
import static org.icgc.dcc.song.core.model.ExportedPayload.createExportedPayload;
import static org.icgc.dcc.song.core.utils.JsonUtils.readTree;
import static org.icgc.dcc.song.core.utils.JsonUtils.toPrettyJson;
import static org.icgc.dcc.song.server.model.enums.AnalysisTypes.SEQUENCING_READ;
import static org.icgc.dcc.song.server.model.enums.AnalysisTypes.VARIANT_CALL;
import static org.icgc.dcc.song.server.model.enums.AnalysisTypes.resolveAnalysisType;
import static org.icgc.dcc.song.server.service.export.PayloadConverter.createPayloadConverter;
import static org.icgc.dcc.song.server.service.export.PayloadParser.createPayloadParser;

@Service
public class ExportService {

  @Autowired
  private AnalysisService analysisService;

  @SneakyThrows
  public List<ExportedPayload> exportPayload(@NonNull List<String> analysisIds,
      boolean includeAnalysisId){
    val analysisMap = aggregateByStudy(analysisIds);
    return analysisMap.entrySet().stream()
        .map(e -> buildExportedPayload(e.getKey(), e.getValue(), includeAnalysisId))
        .collect(toImmutableList());
  }

  @SneakyThrows
  public List<ExportedPayload> exportPayloadsForStudy(@NonNull String studyId,
      boolean includeAnalysisId){
    val payloads = analysisService.getAnalysis(studyId).stream()
        .map(x -> convertToPayload(x, includeAnalysisId))
        .collect(toImmutableList());
    return ImmutableList.of(createExportedPayload(studyId, payloads));
  }

  private Map<String, List<Analysis>> aggregateByStudy(List<String> analysisIds){
    return analysisIds.stream()
        .map(x -> analysisService.read(x))
        .collect(groupingBy(Analysis::getStudy));
  }

  private static ExportedPayload buildExportedPayload(String studyId, List<Analysis> analyses,
      boolean includeAnalysisId){
    val payloads = analyses.stream()
        .map(x -> convertToPayload(x, includeAnalysisId))
        .collect(toImmutableList());
    return createExportedPayload(studyId, payloads);
  }

  @SneakyThrows
  private static JsonNode convertToPayload(@NonNull Analysis a, boolean includeAnalysisId) {
    JsonNode output;
    val analysisType = resolveAnalysisType(a.getAnalysisType());
    if (analysisType == SEQUENCING_READ) {
      val seqRead = (SequencingReadAnalysis) a;
      output = readTree(toPrettyJson(seqRead));
    } else if (analysisType == VARIANT_CALL) {
      val varCall = (VariantCallAnalysis) a;
      output = readTree(toPrettyJson(varCall));
    } else {
      throw new IllegalStateException(
          format("Should not be here, unsupported analysisType '%s'",
              a.getAnalysisType()));
    }

    val payloadConverter = createPayloadConverter(includeAnalysisId);
    val payloadParser = createPayloadParser(output);
    return payloadConverter.convert(payloadParser);
  }

}
