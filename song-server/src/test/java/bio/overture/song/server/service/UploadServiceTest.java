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
package bio.overture.song.server.service;

import bio.overture.song.core.testing.SongErrorAssertions;
import bio.overture.song.core.utils.JsonUtils;
import bio.overture.song.core.utils.RandomGenerator;
import bio.overture.song.server.model.Upload;
import bio.overture.song.server.model.analysis.AbstractAnalysis;
import bio.overture.song.server.model.analysis.SequencingReadAnalysis;
import bio.overture.song.server.model.entity.Sample;
import bio.overture.song.server.service.export.ExportService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Builder;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.assertj.core.api.Assertions;
import org.icgc.dcc.id.client.core.IdClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Files;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.util.Lists.newArrayList;
import static org.icgc.dcc.common.core.util.stream.Collectors.toImmutableSet;
import static org.springframework.http.HttpStatus.OK;
import static bio.overture.song.core.exceptions.ServerErrors.ANALYSIS_ID_COLLISION;
import static bio.overture.song.core.exceptions.ServerErrors.DUPLICATE_ANALYSIS_ATTEMPT;
import static bio.overture.song.core.exceptions.ServerErrors.STUDY_ID_DOES_NOT_EXIST;
import static bio.overture.song.core.exceptions.ServerErrors.UPLOAD_ID_NOT_FOUND;
import static bio.overture.song.core.exceptions.ServerErrors.UPLOAD_ID_NOT_VALIDATED;
import static bio.overture.song.core.utils.JsonUtils.readTree;
import static bio.overture.song.core.utils.JsonUtils.toJson;
import static bio.overture.song.core.utils.RandomGenerator.createRandomGenerator;
import static bio.overture.song.server.utils.PayloadGenerator.createPayloadGenerator;
import static bio.overture.song.server.utils.StudyGenerator.createStudyGenerator;
import static bio.overture.song.server.utils.TestFiles.getJsonNodeFromClasspath;
import static bio.overture.song.server.utils.TestFiles.getJsonStringFromClasspath;
import static bio.overture.song.server.utils.securestudy.impl.SecureUploadTester.createSecureUploadTester;


@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class })
@ActiveProfiles({"dev", "secure", "async-test"})
@Transactional
public class UploadServiceTest {

  private static final String FILEPATH = "../src/test/resources/fixtures/";
  private static final String DEFAULT_STUDY = "ABC123";
  private static int ANALYSIS_ID_COUNT = 0;

  @Autowired
  UploadService uploadService;

  @Autowired
  AnalysisService analysisService;

  @Autowired
  ExportService exportService;

  @Autowired
  StudyService studyService;

  @Autowired
  IdClient idClient;

  private final RandomGenerator randomGenerator = createRandomGenerator(UploadServiceTest.class.getSimpleName());

  @Test
  public void testAsyncSequencingRead(){
    testSequencingRead(true);
  }

  @Test
  public void testSyncSequencingRead(){
    testSequencingRead(false);
  }

  @Test
  public void testNullSyncSequencingRead(){
    val filename1 = "documents/deserialization/sequencingread-deserialize1.json";
    val uploadId1 = uploadFromTestDir(DEFAULT_STUDY, filename1, false);
    val saveStatus1 = uploadService.save(DEFAULT_STUDY, uploadId1, false);
    val analysisStatus1 = fromStatus(saveStatus1, "status");
    assertThat(analysisStatus1).isEqualTo("ok");
    val analysisId1 = fromStatus(saveStatus1, "analysisId");
    val a1 =  analysisService.securedDeepRead(DEFAULT_STUDY, analysisId1);
    val sa1 = ((SequencingReadAnalysis) a1).getExperiment();
    Assertions.assertThat(sa1.getAligned()).isNull();
    Assertions.assertThat(sa1.getAlignmentTool()).isNull();
    Assertions.assertThat(sa1.getInsertSize()).isNull();
    Assertions.assertThat(sa1.getLibraryStrategy()).isEqualTo("WXS");
    Assertions.assertThat(sa1.getPairedEnd()).isNull();
    Assertions.assertThat(sa1.getReferenceGenome()).isNull();
    assertThat(sa1.getInfo().get("random")).isNull();

    val filename2 = "documents/deserialization/sequencingread-deserialize2.json";
    val uploadId2 = uploadFromTestDir(DEFAULT_STUDY, filename2, false);
    val saveStatus2 = uploadService.save(DEFAULT_STUDY, uploadId2, false);
    val analysisStatus2 = fromStatus(saveStatus2, "status");
    assertThat(analysisStatus2).isEqualTo("ok");
    val analysisId2 = fromStatus(saveStatus2, "analysisId");
    val a2 =  analysisService.securedDeepRead(DEFAULT_STUDY, analysisId2);
    val sa2 = ((SequencingReadAnalysis) a2).getExperiment();
    Assertions.assertThat(sa2.getAligned()).isNull();
    Assertions.assertThat(sa2.getAlignmentTool()).isNull();
    Assertions.assertThat(sa2.getInsertSize()).isNull();
    Assertions.assertThat(sa2.getLibraryStrategy()).isEqualTo("WXS");
    Assertions.assertThat(sa2.getPairedEnd()).isTrue();
    Assertions.assertThat(sa2.getReferenceGenome()).isNull();
  }

  @Test
  public void testAsyncVariantCall(){
    testVariantCall(true);
  }

  @Test
  public void testSyncVariantCall(){
    testVariantCall(false);
  }

  @Test
  public void testSyncUploadNoCreated(){
    val fileName = "sequencingRead.json";
    val json = readFile(fileName);
    val uploadStatus = uploadService.upload(DEFAULT_STUDY, json, false );
    log.info(format("Got uploadStatus='%s'",uploadStatus));
    val uploadId = fromStatus(uploadStatus,"uploadId");

    val upload = uploadService.securedRead(DEFAULT_STUDY, uploadId);

    assertThat(upload.getState()).isNotEqualTo("CREATED"); //Since validation done synchronously, validation cannot ever return with the CREATED state
  }

  @SneakyThrows
  public String fromStatus( ResponseEntity<String> uploadStatus, String key) {
    val uploadId = readTree(uploadStatus.getBody()).at("/"+key).asText("");
    return uploadId;
  }

  @Test
  public void testSyncUpload(){
    val fileName = "sequencingRead.json";
    val json = readFile(fileName);
    val uploadStatus = uploadService.upload(DEFAULT_STUDY, json, false );
    val uploadId = fromStatus(uploadStatus,"uploadId");
    val upload = uploadService.securedRead(DEFAULT_STUDY, uploadId);
    assertThat(upload.getState()).isEqualTo("VALIDATED");
  }


  @Test
  public void testASyncUpload(){
    val fileName = "sequencingRead.json";
    val json = readFile(fileName);
    val uploadStatus = uploadService.upload(DEFAULT_STUDY, json, true );
    val uploadId = fromStatus(uploadStatus,"uploadId");
    val upload = uploadService.securedRead(DEFAULT_STUDY, uploadId);
    assertThat(upload.getState()).isEqualTo("CREATED");
  }

  @SneakyThrows
  @Test
  public void testAsyncUpdate() {
    val fileName= "updateAnalysisTest.json";
    val json = readFile(fileName);
    val uploadStatus = uploadService.upload(DEFAULT_STUDY, json, false );
    val uploadId = fromStatus(uploadStatus,"uploadId");
    log.info(format("UploadStatus='%s'",uploadStatus));

    val json2 = json.replace("MUSE variant call pipeline","Muslix popcorn");
    assertThat(json).isNotEqualTo(json2);
    val uploadStatus2 = uploadService.upload(DEFAULT_STUDY, json2, true);
    val uploadId2 =  fromStatus(uploadStatus,"uploadId");
    val status2 = fromStatus(uploadStatus2, "status");
    val replaced = fromStatus(uploadStatus2, "replaced");

    assertThat(replaced).isEqualTo(json);
    assertThat(uploadId).isEqualTo(uploadId2);
    assertThat(status2).isEqualTo("WARNING: replaced content for analysisId 'A0001'");
    val upload = uploadService.securedRead(DEFAULT_STUDY, uploadId2);
    assertThat(upload.getPayload()).isEqualTo(json2);
    assertThat(upload.getState()).isEqualTo("UPDATED");

    // test validation
    val finalState = validate(DEFAULT_STUDY, uploadId);
    assertThat(finalState).isEqualTo("VALIDATED");

    // test save
    val response = uploadService.save(DEFAULT_STUDY,uploadId, false);
    assertThat(response.getStatusCode()).isEqualTo(OK);
  }

  @SneakyThrows
  @Test public void testSyncUpdate() {
    val fileName = "variantCallWithSubmitterId.json";
    val json = readFile(fileName);
    val uploadStatus = uploadService.upload(DEFAULT_STUDY, json, false );
    log.info(format("UploadStatus='%s'",uploadStatus));
    val uploadId = fromStatus(uploadStatus,"uploadId");
    val status = fromStatus(uploadStatus, "status");
    assertThat(status).isEqualTo("ok");

    val json2 = json.replace("silver bullet","golden hammer");
    assertThat(json).isNotEqualTo(json2);
    val uploadStatus2 = uploadService.upload(DEFAULT_STUDY, json2, false);
    val uploadId2 =  fromStatus(uploadStatus2,"uploadId");
    val status2 = fromStatus(uploadStatus2, "status");
    assertThat(status2).isEqualTo("WARNING: replaced content for analysisId 'VariantCall-X24Alpha'");


    val upload = uploadService.securedRead(DEFAULT_STUDY, uploadId2);
    assertThat(upload.getPayload()).isEqualTo(json2);
    assertThat(upload.getState()).isEqualTo("VALIDATED");

    // test save
    val response = uploadService.save(DEFAULT_STUDY,uploadId, false);
    assertThat(response.getStatusCode()).isEqualTo(OK);
  }

  @SneakyThrows
  private void testSequencingRead(final boolean isAsyncValidation) {
    test("sequencingRead.json", isAsyncValidation);
  }

  @SneakyThrows
  private void testVariantCall(final boolean isAsyncValidation) {
    test("variantCall.json", isAsyncValidation);
  }

  @SneakyThrows
  private String readFile(String name) {
    return new String(Files.readAllBytes(new java.io.File(FILEPATH, name).toPath()));
  }

  private String read(String studyId, String uploadId) {
    Upload status = uploadService.securedRead(studyId, uploadId);
    return status.getState();
  }


  private String validate(String studyId, String uploadId) throws InterruptedException {
    String state=read(studyId, uploadId);
    // wait for the server to finish
    while(state.equals("CREATED") || state.equals("UPDATED")) {
      Thread.sleep(50);
      state=read(studyId, uploadId);
    }
    return state;
  }

  @SneakyThrows
  private String uploadFromFixtureDir(String study, String fileName, boolean
      isAsyncValidation){
    val json = readFile(fileName);
    return upload(study, json, isAsyncValidation);
  }

  @SneakyThrows
  private String uploadFromTestDir(String study, String fileName, boolean isAsyncValidation){
    val json = getJsonStringFromClasspath(fileName);
    return upload(study, json, isAsyncValidation);
  }

  @SneakyThrows
  private String upload(String study, String json, boolean isAsyncValidation){
    // test upload
    val uploadStatus=uploadService.upload(study, json, isAsyncValidation);
    assertThat(uploadStatus.getStatusCode()).isEqualTo(OK);
    val uploadId= fromStatus(uploadStatus,"uploadId");
    log.info(format("UploadId='%s'",uploadId));
    assertThat(uploadId.startsWith("UP")).isTrue();

    val initialState = read(study, uploadId);
    if (isAsyncValidation){
      // test create for Asynchronous case
      assertThat(initialState).isEqualTo("CREATED");
    } else {
      assertThat(initialState).isEqualTo("VALIDATED"); //Synchronous should always return VALIDATED
    }

    // test validation
    val finalState = validate(study, uploadId);
    assertThat(finalState).isEqualTo("VALIDATED");
    return uploadId;
  }

  @SneakyThrows
  private void test(String fileName, boolean isAsyncValidation) {
    val uploadId = uploadFromFixtureDir(DEFAULT_STUDY, fileName, isAsyncValidation);

    // test save
   val response = uploadService.save(DEFAULT_STUDY,uploadId, false);
   assertThat(response.getStatusCode()).isEqualTo(OK);
  }

  @Test
  public void testAnalysisIdCollision(){
    // Read test payload.json file, and add analysisId
    val payload = createPayloadWithDifferentAnalysisId();
    val expectedAnalysisId = payload.getAnalysisId();
    log.info("Testing for analysisId: {}", expectedAnalysisId);
    val jsonPayload = payload.getJsonPayload();

    // Create an analysisId in the IdService database
    assertThat(idClient.getAnalysisId(expectedAnalysisId)).isNotPresent();
    idClient.createAnalysisId(expectedAnalysisId);
    assertThat(idClient.getAnalysisId(expectedAnalysisId)).isPresent();

    // Upload1 of jsonPayload
    val uploadId1 = upload(DEFAULT_STUDY,jsonPayload, false);

    // Save1 - should detect that the analysisId already exists in the IdService
    SongErrorAssertions.assertSongError(() ->
      uploadService.save(DEFAULT_STUDY, uploadId1, false), ANALYSIS_ID_COLLISION,
      "Collision was not detected!");

    // Save2 - same as save1 except ignoreAnalysisIdCollisions = true, which will successfully save the payload
    val response2 = uploadService.save(DEFAULT_STUDY, uploadId1, true);
    assertThat(response2.getStatusCode()).isEqualTo(OK);
  }

  @Test
  public void testDuplicateAnalysisIdDetection(){
    // Read test payload.json file, and add analysisId
    val payload = createPayloadWithDifferentAnalysisId();
    val expectedAnalysisId = payload.getAnalysisId();
    log.info("Testing for analysisId: {}", expectedAnalysisId);
    val jsonPayload = payload.getJsonPayload();


    // Ensure the analysisId doesnt already exist in the IdService
    assertThat(idClient.getAnalysisId(expectedAnalysisId)).isNotPresent();

    // Upload1 of jsonPayload
    val uploadId1 = upload(DEFAULT_STUDY, jsonPayload, false );
    assertThat(idClient.getAnalysisId(expectedAnalysisId)).isNotPresent();

    // Save1 - saves the current jsonPayload...normal operation
    val response1 = uploadService.save(DEFAULT_STUDY,uploadId1, false);
    assertThat(idClient.getAnalysisId(expectedAnalysisId)).isPresent();
    assertThat(response1.getStatusCode()).isEqualTo(OK);

    // Save2 - should detect that an analysis with the same analysisId was already save in the song database
    SongErrorAssertions.assertSongError(() -> uploadService.save(DEFAULT_STUDY, uploadId1, true),
        DUPLICATE_ANALYSIS_ATTEMPT,
      "Should not be able to create 2 analysis with the same id (%s)!",expectedAnalysisId);
  }

  @Test
  public void testStudyDNEException(){
    val payload = createPayloadWithDifferentAnalysisId();
    val nonExistentStudyId = randomGenerator.generateRandomAsciiString(8);
    SongErrorAssertions.assertSongError( () -> uploadService.upload(nonExistentStudyId, payload.getJsonPayload(), false),
        STUDY_ID_DOES_NOT_EXIST);
  }

  @Test
  public void testSaveExceptions(){
    val payload = createPayloadWithDifferentAnalysisId();
    val nonExistentStudyId = randomGenerator.generateRandomAsciiString(8);
    val nonExistentUploadId = randomGenerator.generateRandomAsciiString(29);
    SongErrorAssertions.assertSongError( () -> uploadService.save(nonExistentStudyId, nonExistentUploadId, false), STUDY_ID_DOES_NOT_EXIST);
    SongErrorAssertions.assertSongError( () -> uploadService.save(DEFAULT_STUDY, nonExistentUploadId, false),
        UPLOAD_ID_NOT_FOUND);

    // Upload data and test nonExistentStudy with existent upload
    val uploadResponse = uploadService.upload(DEFAULT_STUDY, payload.getJsonPayload(), false);
    val uploadId = fromStatus(uploadResponse,"uploadId");
    SongErrorAssertions.assertSongError( () -> uploadService.save(nonExistentStudyId, uploadId, false),
        STUDY_ID_DOES_NOT_EXIST);
  }

  @Test
  public void testSaveValidatedException(){
    val payload = createPayloadWithDifferentAnalysisId();
    val corruptedJsonPayload = payload.getJsonPayload().replace('{', ' ');
    val uploadResponse = uploadService.upload(DEFAULT_STUDY, corruptedJsonPayload, false);
    val uploadId = fromStatus(uploadResponse,"uploadId");
    SongErrorAssertions.assertSongError( () -> uploadService.save(DEFAULT_STUDY, uploadId, false),
        UPLOAD_ID_NOT_VALIDATED);
  }

  @Test
  public void testUploadExistence(){
    val randomUploadId = randomGenerator.generateRandomUUIDAsString();
    assertThat(uploadService.isUploadExist(randomUploadId)).isFalse();

    val payload = createPayloadWithDifferentAnalysisId();
    val jsonPayload = payload.getJsonPayload();
    val uploadId1 = upload(DEFAULT_STUDY, jsonPayload, false );
    assertThat(uploadService.isUploadExist(uploadId1)).isTrue();
  }

  @Test
  @Transactional
  public void testCheckFileUnrelatedToStudy(){
    val secureUploadTester = createSecureUploadTester(randomGenerator, studyService, uploadService);
    secureUploadTester.runSecureTest((s,u) -> uploadService.checkUploadRelatedToStudy(s, u));
    secureUploadTester.runSecureTest((s,u) -> uploadService.securedRead(s, u));
    secureUploadTester.runSecureTest((s,u) -> uploadService.save(s, u, false));
  }

  @Test
  @Transactional
  public void testSave2PayloadsWithSameSpecimen(){
    // Set up generators
    val studyGenerator = createStudyGenerator(studyService, randomGenerator);
    val payloadGenerator = createPayloadGenerator(randomGenerator);

    // Create new unique study
    val studyId = studyGenerator.createRandomStudy();

    // Create payload1 and save it
    val payload1 = payloadGenerator.generateDefaultRandomPayload(SequencingReadAnalysis.class);
    val previousSampleSubmitterIds =  payload1.getSample().stream().map(Sample::getSampleSubmitterId).collect( toImmutableSet());
    val an1 = analysisService.create(studyId, payload1, false);

    // Export the previously uploaded payload using the analysis id
    val exportedPayloads = exportService.exportPayload(newArrayList(an1), false);
    assertThat(exportedPayloads).hasSize(1);
    val exportedPayload = exportedPayloads.get(0);
    assertThat(exportedPayload.getStudyId()).isEqualTo(studyId);
    assertThat(exportedPayload.getPayloads()).hasSize(1);
    val jsonPayload = exportedPayload.getPayloads().get(0);

    // Create payload 2
    val payload2 = JsonUtils.fromJson(jsonPayload, AbstractAnalysis.class);

    // Modify the exported payload with a different sampleSubmmiterId
    payload2.getSample().forEach(x -> x.setSampleSubmitterId(randomGenerator.generateRandomUUIDAsString()));
    payload2.getSample().get(0).setSampleSubmitterId(randomGenerator.generateRandomUUIDAsString());

    // Assert that none of the sampleSubmmiterIds between payload1 and payload2 match
    val currentSampleSubmitterIds =  payload2.getSample().stream().map(Sample::getSampleSubmitterId).collect( toImmutableSet());
    val hasMatch = previousSampleSubmitterIds.stream().anyMatch(currentSampleSubmitterIds::contains);
    assertThat(hasMatch).isFalse();

    // Save payload 2
    val an2 = analysisService.create(studyId, payload2, false);

    // Validate both analysis have the same specimen and donor submitterIds, and studies, but different analysisIds and sample submitterIds
    val a1 = analysisService.unsecuredDeepRead(an1);
    val a2 = analysisService.unsecuredDeepRead(an2);
    assertThat(a1.getStudy()).isEqualTo(studyId);
    assertThat(a2.getStudy()).isEqualTo(studyId);
    assertThat(a1.getAnalysisId()).isNotEqualTo(a2.getAnalysisId());
    assertThat(a1.getSample()).hasSize(1);
    assertThat(a2.getSample()).hasSize(1);
    assertThat(a1.getSample().get(0).getDonor().getDonorId()).isEqualTo(a2.getSample().get(0).getDonor().getDonorId());
    assertThat(a1.getSample().get(0).getSpecimen().getSpecimenId()).isEqualTo(a2.getSample().get(0).getSpecimen().getSpecimenId());
    assertThat(a1.getSample().get(0).getSampleId()).isNotEqualTo(a2.getSample().get(0).getSampleId());
  }


  private String createUniqueAnalysisId(){
    return format("AN-56789-%s",ANALYSIS_ID_COUNT++);
  }

  private static JsonNode updateAnalysisId(JsonNode json, String analysisId){
    val obj = (ObjectNode)json;
    obj.put("analysisId", analysisId);
    return json;
  }

  private Payload createPayloadWithDifferentAnalysisId(){
    val filename = "documents/sequencingread-valid.json";
    val json = getJsonNodeFromClasspath(filename);
    val analysisId = createUniqueAnalysisId();
    val jsonPayload = toJson(updateAnalysisId(json, analysisId));
    return Payload.builder()
        .analysisId(analysisId)
        .jsonPayload(jsonPayload)
        .build();
  }

  @Value
  @Builder
  private static class Payload{
    @NonNull private final String analysisId;
    @NonNull private final String jsonPayload;
  }

}
