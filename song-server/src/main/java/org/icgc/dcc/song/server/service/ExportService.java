package org.icgc.dcc.song.server.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.val;
import org.icgc.dcc.common.core.util.stream.Streams;
import org.icgc.dcc.song.server.model.analysis.Analysis;
import org.icgc.dcc.song.server.model.analysis.SequencingReadAnalysis;
import org.icgc.dcc.song.server.model.analysis.VariantCallAnalysis;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static java.lang.String.format;
import static java.util.Arrays.stream;
import static org.icgc.dcc.song.core.utils.JsonUtils.readTree;
import static org.icgc.dcc.song.core.utils.JsonUtils.toPrettyJson;

@Service
public class ExportService {

  @Autowired
  private AnalysisService analysisService;

  @SneakyThrows
  public JsonNode exportPayload(@NonNull String analysisId, boolean includeIds){
    return convertToPayload(analysisService.read(analysisId), includeIds);
  }

  @SneakyThrows
  private static JsonNode convertToPayload(@NonNull Analysis a, boolean includeIds){
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
    if (!includeIds){
      remove(output, "study");
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
