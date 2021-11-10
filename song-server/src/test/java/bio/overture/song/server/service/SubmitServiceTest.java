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
    val submitResponse = submitService.submit(DEFAULT_STUDY, jsonPayload);
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
    val submitResponse2 = submitService.submit(DEFAULT_STUDY, jsonPayload2);
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
    assertSongError(() -> submitService.submit(DEFAULT_STUDY, corruptedPayload), PAYLOAD_PARSING);
  }

  @Test
  @SneakyThrows
  public void submit_AnalysisIdInPayload_SchemaValidationError() {
    val p = createPayloadWithDifferentAnalysisId();
    val invalidPayload = (ObjectNode) new ObjectMapper().readTree(p.getJsonPayload());
    invalidPayload.put(ANALYSIS_ID, p.getAnalysisId());
    assertSongError(
        () -> submitService.submit(DEFAULT_STUDY, invalidPayload.toString()), SCHEMA_VIOLATION);
  }

  @Test
  @Transactional
  public void testSaveIdMismatchAllSame() {
    // existing sample (same sample id, specimen id, donor id)
    // should give us a new analysis for our existing data
    val studyId = randomStudy();
    val payload = randomPayload();
    val analysisId = submitAnalysis(studyId, payload);
    val payload2 = getModifiedPayload(payload, true, true, true);

    val result = submitAnalysis(studyId, payload2);
    assertFalse("No error results expected", result.startsWith("ERR:"));
    assertNotEquals("New analysisId expected", analysisId, result);
  }

  @Test
  @Transactional
  public void testSaveIdMismatchDifferentDonor() {
    // same sample, same specimen, different donor Id
    // specimen to donor id mis-match
    val studyId = randomStudy();
    val payload = randomPayload();
    submitAnalysis(studyId, payload);
    val payload2 = getModifiedPayload(payload, true, true, false);
    val result = submitAnalysis(studyId, payload2);
    assertTrue(
        "Donor Id mismatch expected",
        result.startsWith("ERR: [CompositeEntityService::specimen.to.donor.id.mismatch]"));
  }

  @Test
  @Transactional
  public void testSaveIdMismatchDifferentSpecimen() {
    // same sample has a different specimen id
    // sample id to specimen id mis-match
    val studyId = randomStudy();
    val payload = randomPayload();
    val analysisId = submitAnalysis(studyId, payload);
    val payload2 = getModifiedPayload(payload, true, false, true);
    val result = submitAnalysis(studyId, payload2);
    assertTrue(
        "Specimen Id mismatch expected",
        result.startsWith("ERR: [CompositeEntityService::sample.to.specimen.id.mismatch]"));
  }

  @Test
  @Transactional
  public void testSaveIdMismatchDifferentSpecimenAndDonor() {
    // same sample has a different specimen id and donor id
    // sapleId to specimenId *and* specimenId to donorId mis-match
    val studyId = randomStudy();
    val payload = randomPayload();
    val analysisId = submitAnalysis(studyId, payload);
    val payload2 = getModifiedPayload(payload, true, false, false);
    val result = submitAnalysis(studyId, payload2);
    assertTrue(
        "Specimen Id mismatch expected",
        result.startsWith("ERR: [CompositeEntityService::sample.to.specimen.id.mismatch]"));
  }

  @Test
  @Transactional
  public void testSaveIdMismatchDifferentSample() {
    // new sample, existing specimen and donor
    // new analysisId
    val studyId = randomStudy();
    val payload = randomPayload();
    val analysisId = submitAnalysis(studyId, payload);
    val payload2 = getModifiedPayload(payload, false, true, true);
    val result = submitAnalysis(studyId, payload);
    assertFalse("No error results expected", result.startsWith("ERR"));
    assertNotEquals("New analysisId expected", analysisId, result);
  }

  @Test
  @Transactional
  public void testSaveIdMismatchDifferentSampleDifferentDonor() {
    // new sample, existing specimen, different donor
    // specimen to donor Id mis-match
    val studyId = randomStudy();
    val payload = randomPayload();
    val analysisId = submitAnalysis(studyId, payload);
    assertFalse("Initial submit analysis should not have errors", analysisId.startsWith("ERR:"));
    val payload2 = getModifiedPayload(payload, false, true, false);

    val sample1 = payload.getSamples().get(0);
    val sample2 = payload2.getSamples().get(0);

    assertNotEquals(
        "Payloads should have different submitter sample ids",
        sample1.getSubmitterSampleId(),
        sample2.getSubmitterSampleId());
    assertEquals(
        "Payloads should have same specimenIds",
        sample1.getSpecimen().getSubmitterSpecimenId(),
        sample2.getSpecimen().getSubmitterSpecimenId());
    assertNotEquals(
        "Payloads should have different donors",
        sample1.getDonor().getSubmitterDonorId(),
        sample2.getDonor().getSubmitterDonorId());

    val result = submitAnalysis(studyId, payload2);
    assertTrue(
        "Donor Id mismatch expected",
        result.startsWith("ERR: [CompositeEntityService::specimen.to.donor.id.mismatch]"));
  }

  @Test
  @Transactional
  public void testSaveIdMismatchDifferentSampleDifferentSpecimen() {
    // new sample, new specimen, same donor
    // new analysisId
    val studyId = randomStudy();
    val payload = randomPayload();
    val analysisId = submitAnalysis(studyId, payload);
    val payload2 = getModifiedPayload(payload, false, false, true);
    val result = submitAnalysis(studyId, payload2);
    assertFalse("No error results expected", result.startsWith("ERR"));
    assertNotEquals("New AnalysisId expected", analysisId, result);
  }

  @Test
  @Transactional
  public void testSaveIdMismatchAllDifferent() {
    // new sample, new specimen, new donor
    // new analysisId
    val studyId = randomStudy();
    val payload = randomPayload();
    val analysisId = submitAnalysis(studyId, payload);
    val payload2 = getModifiedPayload(payload, false, false, false);
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

  private Payload getModifiedPayload(
      Payload payload, boolean sameSample, boolean sameSpecimen, boolean sameDonor) {
    val payload2 = Payload.parse(toJson(payload));
    val samplePayload = modifySample(payload2, sameSample);
    val specimenPayload = modifySpecimen(samplePayload, sameSpecimen);
    val donorPayload = modifyDonor(specimenPayload, sameDonor);
    return donorPayload;
  }

  private String submitAnalysis(String studyId, Payload payload) {
    String actual;
    payload.setStudyId(studyId);
    try {
      actual = submitService.submit(studyId, toJson(payload)).getAnalysisId();
    } catch (Throwable throwable) {
      actual = "ERR: " + throwable.getMessage();
    }
    return actual;
  }

  private Payload modifySample(Payload payload, Boolean sameSample) {
    if (!sameSample) {
      payload
          .getSamples()
          .forEach(x -> x.setSubmitterSampleId(randomGenerator.generateRandomUUIDAsString()));
    }
    return payload;
  }

  private Payload modifySpecimen(Payload payload, Boolean sameSpecimen) {
    if (!sameSpecimen) {
      payload
          .getSamples()
          .forEach(
              sample ->
                  sample
                      .getSpecimen()
                      .setSubmitterSpecimenId(randomGenerator.generateRandomUUIDAsString()));
    }
    return payload;
  }

  private Payload modifyDonor(Payload payload, Boolean sameDonor) {
    if (!sameDonor) {
      payload
          .getSamples()
          .forEach(
              sample ->
                  sample
                      .getDonor()
                      .setSubmitterDonorId(randomGenerator.generateRandomUUIDAsString()));
    }
    return payload;
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
