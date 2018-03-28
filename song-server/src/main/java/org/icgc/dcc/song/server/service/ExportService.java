package org.icgc.dcc.song.server.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableList;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.val;
import org.icgc.dcc.common.core.util.stream.Streams;
import org.icgc.dcc.song.core.model.ExportedPayload;
import org.icgc.dcc.song.server.model.analysis.Analysis;
import org.icgc.dcc.song.server.model.analysis.SequencingReadAnalysis;
import org.icgc.dcc.song.server.model.analysis.VariantCallAnalysis;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.groupingBy;
import static org.icgc.dcc.common.core.util.stream.Collectors.toImmutableList;
import static org.icgc.dcc.song.core.model.ExportedPayload.createExportedPayload;
import static org.icgc.dcc.song.core.utils.JsonUtils.readTree;
import static org.icgc.dcc.song.core.utils.JsonUtils.toPrettyJson;

@Service
public class ExportService {

  @Autowired
  private AnalysisService analysisService;

  @SneakyThrows
  public List<ExportedPayload> exportPayload(@NonNull List<String> analysisIds,
      boolean includeAnalysisId, boolean includeOtherIds){
    val analysisMap = aggregateByStudy(analysisIds);
    return analysisMap.entrySet().stream()
        .map(e -> buildExportedPayload(e.getKey(), e.getValue(), includeAnalysisId, includeOtherIds))
        .collect(toImmutableList());
  }

  private static ExportedPayload buildExportedPayload(String studyId, List<Analysis> analyses,
      boolean includeAnalysisId, boolean includeOtherIds){
    val payloads = analyses.stream()
        .map(x -> convertToPayload(x, includeAnalysisId, includeOtherIds))
        .collect(toImmutableList());
    return createExportedPayload(studyId, payloads);
  }

  private Map<String, List<Analysis>> aggregateByStudy(List<String> analysisIds){
    return analysisIds.stream()
        .map(x -> analysisService.read(x))
        .collect(groupingBy(Analysis::getStudy));
  }

  @SneakyThrows
  public List<ExportedPayload> exportPayloadsForStudy(@NonNull String studyId,
      boolean includeAnalysisId, boolean includeOtherIds){
    val payloads = analysisService.getAnalysis(studyId).stream()
        .map(x -> convertToPayload(x, includeAnalysisId, includeOtherIds))
        .collect(toImmutableList());
    return ImmutableList.of(createExportedPayload(studyId, payloads));
  }

  @SneakyThrows
  private static JsonNode convertToPayload(@NonNull Analysis a, boolean includeAnalysisId, boolean includeOtherIds){
    JsonNode output;
    if (a.getAnalysisType().equals("sequencingRead")){
      val seqRead = (SequencingReadAnalysis)a;
      output = readTree(toPrettyJson(seqRead));
    } else if (a.getAnalysisType().equals("variantCall")){
      val varCall = (VariantCallAnalysis)a;
      output = readTree(toPrettyJson(varCall));
    } else {
      throw new IllegalStateException(
          format("Should not be here, unsupported analysisType '%s'",
              a.getAnalysisType()));
    }
    if (!includeAnalysisId){
      remove(output, "analysisId");
    }
    if (!includeOtherIds){
      remove(output, "study");
      remove(output, "analysisState");
      remove(output.path("experiment"), "analysisId");
      Streams.stream(output.path("sample").iterator())
          .forEach(x -> remove(x, "sampleId", "specimenId" ));
      Streams.stream(output.path("sample").iterator())
          .forEach(x -> {
            remove(x, "sampleId", "specimenId" );
            remove(x.path("specimen"), "donorId", "specimenId");
            remove(x.path("donor"), "studyId", "donorId");
          });
      Streams.stream(output.path("file").iterator()).forEach(x -> remove(x, "objectId", "studyId", "analysisId"));
    }
    return output;
  }

  private static void remove(JsonNode path, String fieldName){
    val o = (ObjectNode)path;
    o.remove(fieldName);
  }

  private static void remove(JsonNode path, String...fieldNames){
    stream(fieldNames).forEach(x -> remove(path, x));
  }


}
