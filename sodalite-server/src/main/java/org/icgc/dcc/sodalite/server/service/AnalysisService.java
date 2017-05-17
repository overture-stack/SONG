package org.icgc.dcc.sodalite.server.service;

import static java.lang.String.format;

import java.util.List;
import java.util.Map;

import org.icgc.dcc.sodalite.server.model.AnalysisObject;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor

public class AnalysisService {

  private void info(String fmt, Object... args) {
    log.info(format(fmt, args));
  }

  public List<AnalysisObject> getAnalysisById(String id) {
    // TODO Auto-generated method stub
    info("Called GetAnalysisById with %s", id);
    return null;
  }

  public List<AnalysisObject> getAnalyses(Map<String, String> params) {
    info("Called getAnalyses with %s", params);
    // TODO Auto-generated method stub
    return null;
  }

  @SneakyThrows
  public String registerAnalysis(String studyId, String json) {
    ObjectMapper mapper = new ObjectMapper();
    JsonNode node = mapper.readTree(json);

    if (node.has("sequencingReadSubmission")) {
      return registerSequencingRead(studyId, node);
    } else if (node.has("variantCallSubmission")) {
      return registerVariantCall(studyId, node);
    }
    return "Register Analysis failed: Unknown Analysis Type";
  }

  String registerSequencingRead(String studyId, JsonNode node) {
    String analysisId = "Mock_SequencingReadId";
    return analysisId;
  }

  String registerVariantCall(String studyId, JsonNode node) {
    String analysisId = "MockVariantCallId";
    return analysisId;
  }

  String updateSequencingRead(String studyId, JsonNode node) {
    String status = "Sequencing Read Updated Successfully";
    return status;
  }

  String updateVariantCall(String studyId, JsonNode node) {
    String status = "Variant Call Updated Successfully";
    return status;
  }

  @SneakyThrows
  public String updateAnalysis(String studyId, String json) {
    ObjectMapper mapper = new ObjectMapper();
    JsonNode node = mapper.readTree(json);

    if (node.has("sequencingReadUpdate")) {
      return updateSequencingRead(studyId, node);
    } else if (node.has("variantUpdateCall")) {
      return updateVariantCall(studyId, node);
    }
    return "Updated Analysis failed: unknown analysis type";
  }

}
