package org.icgc.dcc.sodalite.server.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.InputStream;

import org.flywaydb.test.annotation.FlywayTest;
import org.flywaydb.test.junit.FlywayTestExecutionListener;
import org.icgc.dcc.sodalite.server.model.analysis.AnalysisType;
import org.icgc.dcc.sodalite.server.utils.JsonUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

import lombok.SneakyThrows;
import lombok.val;

@SpringBootTest
@RunWith(SpringRunner.class)
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, FlywayTestExecutionListener.class })
@FlywayTest
@ActiveProfiles("dev")
public class AnalysisServiceTest {

  @Autowired
  AnalysisService service;

  @Test
  public void testGetAnalysisType_SequencingRead() {
    val s = JsonUtils.jsonResponse("sequencingRead", "{}");
    val result = service.getAnalysisType(s);
    assertThat(result == AnalysisType.sequencingRead);
  }

  @Test
  public void testGetAnalysisType_VariantCall() {
    val s = JsonUtils.jsonResponse("variantCall", "{}");
    val result = service.getAnalysisType(s);
    assertThat(result == AnalysisType.variantCall);
  }

  @Test
  public void testAddFile() {
    val id = "AN1";
    val fileId = "FI3";

    service.addFile(id, fileId);
    // TODO: verify record was added to FileSet table
    assertThat(true); // we didn't crash
  }

  @Test
  public void testCreateAnalysis() {
    val id = "AN3";
    val studyId = "ABC123";
    val type = AnalysisType.sequencingRead;

    service.createAnalysis(id, studyId, type);
    // TODO: verify record was added to Analysis table

    assertThat(true); // we didn't crash
  }

  @SneakyThrows
  @Test
  public void testCreateSequencingRead() {
    val id = "AN3";

    val node = JsonNodeFactory.instance.objectNode().put("libraryStrategy", "WXS").put("pairedEnd", false)
        .put("insertSize", 900L).put("aligned", true).put("alignmentTool", "Muse variant call pipeline")
        .put("referenceGenome", "hs37d5");

    service.createSequencingRead(id, node);
    // TODO: Verify record was added to SequencingRead table

    assertThat(true); // we didn't crash
  }

  @SneakyThrows
  @Test
  public void testCreateVariantCall() {
    val id = "AN4";
    val studyId = "ABC123";
    val type = AnalysisType.variantCall;

    val node = JsonNodeFactory.instance.objectNode().put("variantCallingTool", "silver bullet")
        .put("tumourSampleSubmitterId", "tumor1A").put("matchedNormalSampleSubmitterId", "reference2B");

    service.createAnalysis(id, studyId, type);
    service.createVariantCall(id, node);

    // TODO: Verify record was added to VariantCallTable
    assertThat(true); // no crash yet
  }

  @SneakyThrows
  @Test
  public void testSaveStudy() {
    val fileName = "documents/upload-sequencingread-valid.json";
    val studyId = "ABC123";

    String json = getJsonNodeFromClasspath(fileName);
    val study = JsonUtils.getTree(json).get("study");

    val fileIds = service.saveStudy(studyId, study);
    // TODO: 1) Verify that the correct records were added to / updated in the Donor, Specimen, Sample, and File tables
    // 2) Verify that the fileIds that were returned were the correct
    assertThat(fileIds.size() == 2);
  }

  @SneakyThrows
  @Test
  public void testCreate() {
    val fileName = "documents/upload-sequencingread-valid.json";
    val studyId = "ABC123";
    String json = getJsonNodeFromClasspath(fileName);
    service.create(studyId, json);
    // TODO: Verify that the study AND the correct type of analysis was created
    assertThat(true); // no crashes yet

  }

  public String getJsonNodeFromClasspath(String name) throws Exception {
    InputStream is1 = Thread.currentThread().getContextClassLoader().getResourceAsStream(name);
    ObjectMapper mapper = new ObjectMapper();
    JsonNode node = mapper.readTree(is1);
    return mapper.writeValueAsString(node);
  }

}
