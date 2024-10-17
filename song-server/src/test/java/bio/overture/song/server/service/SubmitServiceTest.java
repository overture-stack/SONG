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
package bio.overture.song.server.service;

import static bio.overture.song.core.exceptions.ServerErrors.PAYLOAD_PARSING;
import static bio.overture.song.core.exceptions.ServerErrors.SCHEMA_VIOLATION;
import static bio.overture.song.core.testing.SongErrorAssertions.assertSongError;
import static bio.overture.song.core.utils.JsonUtils.fromJson;
import static bio.overture.song.core.utils.JsonUtils.toJson;
import static bio.overture.song.core.utils.RandomGenerator.createRandomGenerator;
import static bio.overture.song.server.model.enums.ModelAttributeNames.ANALYSIS_ID;
import static bio.overture.song.server.utils.TestAnalysis.extractBoolean;
import static bio.overture.song.server.utils.TestAnalysis.extractNode;
import static bio.overture.song.server.utils.TestAnalysis.extractString;
import static bio.overture.song.server.utils.TestFiles.getJsonNodeFromClasspath;
import static bio.overture.song.server.utils.TestFiles.getJsonStringFromClasspath;
import static bio.overture.song.server.utils.generator.LegacyAnalysisTypeName.SEQUENCING_READ;
import static bio.overture.song.server.utils.generator.PayloadGenerator.createPayloadGenerator;
import static bio.overture.song.server.utils.generator.StudyGenerator.createStudyGenerator;
import static java.lang.String.format;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import bio.overture.song.core.utils.RandomGenerator;
import bio.overture.song.core.utils.Responses;
import bio.overture.song.server.model.dto.Payload;
import bio.overture.song.server.repository.UploadRepository;
import bio.overture.song.server.service.analysis.AnalysisService;
import bio.overture.song.server.service.id.IdService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import javax.transaction.Transactional;
import lombok.Builder;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class})
@ActiveProfiles({"test", "async-test"})
@Transactional
public class SubmitServiceTest {

  private static final String DEFAULT_STUDY = "ABC123";
  private static int ANALYSIS_ID_COUNT = 0;

  @Autowired SubmitService submitService;

  @Autowired AnalysisService analysisService;

  @Autowired ExportService exportService;

  @Autowired StudyService studyService;

  @Autowired IdService idService;
  @Autowired ValidationService validationService;
  @Autowired UploadRepository uploadRepository;
  @Autowired AnalysisTypeService analysisTypeService;

  private final RandomGenerator randomGenerator =
      createRandomGenerator(SubmitServiceTest.class.getSimpleName());

  @Test
  public void testNullSyncSequencingRead() {
    val filename1 = "documents/deserialization/sequencingread-deserialize1.json";
    val jsonPayload = getJsonStringFromClasspath(filename1);
    val submitResponse = submitService.submit(DEFAULT_STUDY, jsonPayload, false);
    assertEquals(Responses.OK, submitResponse.getStatus());
    val analysisId1 = submitResponse.getAnalysisId();
    val a1 = analysisService.securedDeepRead(DEFAULT_STUDY, analysisId1);
    val experimentNode1 = extractNode(a1, "experiment");
    assertFalse(experimentNode1.has("aligned"));
    assertFalse(experimentNode1.has("alignmentTool"));
    assertFalse(experimentNode1.has("insertSize"));
    assertEquals(extractString(a1, "experiment", "libraryStrategy"), "WXS");
    assertFalse(experimentNode1.hasNonNull("pairedEnd"));
    assertFalse(experimentNode1.hasNonNull("referenceGenome"));
    assertFalse(a1.getAnalysisData().getData().has("random"));

    val filename2 = "documents/deserialization/sequencingread-deserialize2.json";
    val jsonPayload2 = getJsonStringFromClasspath(filename2);
    val submitResponse2 = submitService.submit(DEFAULT_STUDY, jsonPayload2, false);
    assertEquals(Responses.OK, submitResponse2.getStatus());
    val analysisId2 = submitResponse2.getAnalysisId();
    val a2 = analysisService.securedDeepRead(DEFAULT_STUDY, analysisId2);
    val experimentNode2 = extractNode(a2, "experiment");
    assertFalse(experimentNode2.has("aligned"));
    assertFalse(experimentNode2.has("alignmentTool"));
    assertFalse(experimentNode2.hasNonNull("insertSize"));
    assertEquals(extractString(a2, "experiment", "libraryStrategy"), "WXS");
    assertTrue(extractBoolean(a2, "experiment", "pairedEnd"));
    assertFalse(experimentNode2.hasNonNull("referenceGenome"));
  }

  @Test
  @SneakyThrows
  public void submit_CorruptedPayload_PayloadParsingError() {
    val payload = createPayloadWithDifferentAnalysisId();
    val corruptedPayload = payload.getJsonPayload().replace('{', '}');
    assertSongError(
        () -> submitService.submit(DEFAULT_STUDY, corruptedPayload, false), PAYLOAD_PARSING);
  }

  @Test
  @SneakyThrows
  public void submit_AnalysisIdInPayload_SchemaValidationError() {
    val p = createPayloadWithDifferentAnalysisId();
    val invalidPayload = (ObjectNode) new ObjectMapper().readTree(p.getJsonPayload());
    invalidPayload.put(ANALYSIS_ID, p.getAnalysisId());
    assertSongError(
        () -> submitService.submit(DEFAULT_STUDY, invalidPayload.toString(), false),
        SCHEMA_VIOLATION);
  }

  @Test
  @Transactional
  public void testSaveIdMismatchAllSame() {
    // should give us a new analysis for our existing data
    val studyId = randomStudy();
    val payload = randomPayload();
    val analysisId = submitAnalysis(studyId, payload);
    val payload2 = getModifiedPayload(payload);

    val result = submitAnalysis(studyId, payload2);
    assertFalse("No error results expected", result.startsWith("ERR:"));
    assertNotEquals("New analysisId expected", analysisId, result);
  }

  @Test
  @Transactional
  public void testSaveIdMismatchAllDifferent() {
    // new analysisId
    val studyId = randomStudy();
    val payload = randomPayload();
    val analysisId = submitAnalysis(studyId, payload);
    val payload2 = getModifiedPayload(payload);
    val result = submitAnalysis(studyId, payload2);
    assertFalse("No error results expected", result.startsWith("ERR"));
    assertNotEquals("New analysisId expected", analysisId, result);
  }

  private String randomStudy() {
    val studyGenerator = createStudyGenerator(studyService, randomGenerator);
    return studyGenerator.createRandomStudy();
  }

  private Payload randomPayload() {
    val payloadGenerator = createPayloadGenerator(randomGenerator);
    return payloadGenerator.generateDefaultRandomPayload(SEQUENCING_READ);
  }

  private Payload getModifiedPayload(Payload payload) {
    val payload2 = fromJson(toJson(payload), Payload.class);
    return payload2;
  }

  private String submitAnalysis(String studyId, Payload payload) {
    String actual;
    payload.setStudyId(studyId);
    try {
      actual = submitService.submit(studyId, toJson(payload), false).getAnalysisId();
    } catch (Throwable throwable) {
      actual = "ERR: " + throwable.getMessage();
    }
    return actual;
  }

  private String createUniqueAnalysisId() {
    return format("AN-56789-%s", ANALYSIS_ID_COUNT++);
  }

  private static JsonNode updateAnalysisId(JsonNode json, String analysisId) {
    val obj = (ObjectNode) json;
    obj.put("analysisId", analysisId);
    return json;
  }

  private InternalPayload createPayloadWithDifferentAnalysisId() {
    val filename = "documents/sequencingread-valid.json";
    val json = getJsonNodeFromClasspath(filename);
    val analysisId = createUniqueAnalysisId();
    val jsonPayload = toJson(updateAnalysisId(json, analysisId));
    return InternalPayload.builder().analysisId(analysisId).jsonPayload(jsonPayload).build();
  }

  @Value
  @Builder
  private static class InternalPayload {
    @NonNull private final String analysisId;
    @NonNull private final String jsonPayload;
  }
}
