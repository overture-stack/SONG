package org.icgc.dcc.sodalite.server.service;

import static java.lang.String.format;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.icgc.dcc.sodalite.server.model.entity.File;
import org.icgc.dcc.sodalite.server.model.enums.AnalysisType;
import org.icgc.dcc.sodalite.server.model.enums.IdPrefix;
import org.icgc.dcc.sodalite.server.repository.AnalysisRepository;
import org.icgc.dcc.sodalite.server.utils.JsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalysisService {

  @Autowired
  private final AnalysisRepository repository;
  @Autowired
  private final IdService idService;
  @Autowired
  private final EntityService entityService;

  @SneakyThrows
  public AnalysisType getAnalysisType(String json) {
    return getAnalysisType(JsonUtils.readTree(json));
  }

  public AnalysisType getAnalysisType(JsonNode node) {
    for (val type : AnalysisType.values()) {
      log.info("Checking analysis type " + type.toString());
      if (node.has(type.toString())) {
        return type;
      }
    }
    return null;
  }

  void createAnalysis(String id, String studyId, AnalysisType type) {
    repository.createAnalysis(id, studyId, type.toString());
  }

  @SneakyThrows
  public String create(String studyId, String json) {
    val node = JsonUtils.readTree(json);
    val type = getAnalysisType(node);

    val id = idService.generate(IdPrefix.Analysis);

    createAnalysis(id, studyId, type);

    val study = node.get("study");
    val fileIds = saveStudy(studyId, study);

    for (val f : fileIds) {
      addFile(id, f);
    }

    val analysis = node.get(type.toString());
    switch (type) {
    case sequencingRead:
      return createSequencingRead(id, analysis);
    case variantCall:
      return createVariantCall(id, analysis);
    default:
      return "Upload Analysis failed: Unknown Analysis Type";
    }
  }

  void addFile(String id, String fileId) {
    repository.addFile(id, fileId);
  }

  ObjectNode get(JsonNode root, String key) {
    val node = root.get(key);
    if (node.isObject()) {
      return (ObjectNode) node;
    }
    throw new IllegalArgumentException(format("node '%s'{%s} is not an object node", node, key));
  }

  @SneakyThrows
  Collection<String> saveStudy(String studyId, JsonNode study) {
    val fileIds = new HashSet<String>();

    val donor = get(study, "donor");
    val specimen = donor.get("specimen");
    val sample = specimen.get("sample");
    val files = sample.get("files");

    donor.put("studyId", studyId);
    donor.remove("specimen");
    val donorId = entityService.saveDonor(studyId, donor);

    val specimenId = entityService.saveSpecimen(studyId, donorId, specimen);

    val sampleId = entityService.saveSample(studyId, specimenId, sample);

    for (val file : files) {
      val fileId = entityService.saveFile(studyId, sampleId, file);
      fileIds.add(fileId);
    }

    return fileIds;
  }

  String createSequencingRead(String id, JsonNode node) {
    val strategy = node.get("libraryStrategy").asText();
    val isPaired = node.get("pairedEnd").asBoolean();
    val size = node.get("insertSize").asLong();
    val isAligned = node.get("aligned").asBoolean();
    val tool = node.get("alignmentTool").asText();
    val genome = node.get("referenceGenome").asText();

    repository.createSequencingRead(id, strategy, isPaired, size, isAligned, tool, genome);

    return id;
  }

  String createVariantCall(String id, JsonNode node) {
    val tool = node.get("variantCallingTool").asText();
    val tumorId = node.get("tumourSampleSubmitterId").asText();
    val normalId = node.get("matchedNormalSampleSubmitterId").asText();
    repository.createVariantCall(id, tool, tumorId, normalId);
    return id;
  }

  public List<String> getAnalyses(Map<String, String> params) {
    // TODO Auto-generated method stub
    return null;
  }

  public String getAnalysisById(String id) {
    // TODO Auto-generated method stub
    return null;
  }

  public String updateAnalysis(String studyId, String json) {
    // TODO Auto-generated method stub
    return null;
  }

  public List<File> readFilesByAnalysisId(String id) {
    return repository.getFilesById(id);
  }

}
