/*
 * Copyright (c) 2017 The Ontario Institute for Cancer Research. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the GNU Public License v3.0.
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
 * SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER
 * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */
package org.icgc.dcc.song.server.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.icgc.dcc.song.core.utils.JsonUtils;
import org.icgc.dcc.song.server.model.Upload;
import org.icgc.dcc.song.server.model.analysis.Analysis;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import java.io.InputStream;
import java.nio.file.Files;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.OK;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class })
@ActiveProfiles({"dev", "secure", "test"})
public class UploadServiceTest {

  private static final String FILEPATH = "../src/test/resources/fixtures/";

  @Autowired
  UploadService uploadService;

  @Autowired
  AnalysisService analysisService;

  @Test
  public void testAsyncSequencingRead(){
    testSequencingRead(true);
  }

  @Test
  public void testSyncSequencingRead(){
    testSequencingRead(false);
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
    val study="ABC123";
    val json = readFile(fileName);
    val uploadStatus = uploadService.upload(study, json, false );
    log.info(format("Got uploadStatus='%s'",uploadStatus));
    val uploadId = fromStatus(uploadStatus,"uploadId");

    val upload = uploadService.read(uploadId);

    assertThat(upload.getState()).isNotEqualTo("CREATED"); //Since validation done synchronously, validation cannot ever return with the CREATED state
  }

  @SneakyThrows
  public String fromStatus( ResponseEntity<String> uploadStatus, String key) {
    val uploadId = JsonUtils.readTree(uploadStatus.getBody()).at("/"+key).asText("");
    return uploadId;
  }

  @Test
  public void testSyncUpload(){
    val fileName = "sequencingRead.json";
    val study="ABC123";
    val json = readFile(fileName);
    val uploadStatus = uploadService.upload(study, json, false );
    val uploadId = fromStatus(uploadStatus,"uploadId");
    val upload = uploadService.read(uploadId);
    assertThat(upload.getState()).isEqualTo("VALIDATED");
  }


  @Test
  public void testASyncUpload(){
    val fileName = "sequencingRead.json";
    val study="ABC123";
    val json = readFile(fileName);
    val uploadStatus = uploadService.upload(study, json, true );
    val uploadId = fromStatus(uploadStatus,"uploadId");
    val upload = uploadService.read(uploadId);
    assertThat(upload.getState()).isEqualTo("CREATED");
  }

  @SneakyThrows
  @Test public void testAsyncUpdate() {
    val fileName= "updateAnalysisTest.json";
    val study="ABC123";
    val json = readFile(fileName);
    val uploadStatus = uploadService.upload(study, json, false );
    val uploadId = fromStatus(uploadStatus,"uploadId");
    log.info(format("UploadStatus='%s'",uploadStatus));

    val json2 = json.replace("MUSE variant call pipeline","Muslix popcorn");
    assertThat(json).isNotEqualTo(json2);
    val uploadStatus2 = uploadService.upload(study, json2, true);
    val uploadId2 =  fromStatus(uploadStatus,"uploadId");
    val status2 = fromStatus(uploadStatus2, "status");
    val replaced = fromStatus(uploadStatus2, "replaced");

    assertThat(replaced).isEqualTo(json);
    assertThat(uploadId).isEqualTo(uploadId2);
    assertThat(status2).isEqualTo("WARNING: replaced content for analysisSubmitterId 'A0001'");
    val upload = uploadService.read(uploadId2);
    assertThat(upload.getPayload()).isEqualTo(json2);
    assertThat(upload.getState()).isEqualTo("UPDATED");

    // test validation
    val finalState = validate(uploadId);
    assertThat(finalState).isEqualTo("VALIDATED");

    // test save
    val response = uploadService.save(study,uploadId);
    assertThat(response.getStatusCode()).isEqualTo(OK);
  }


  @Test
  @Ignore
  public void testDonorUpdate(){
    val referencePayloadFilename = "documents/updating/with-analysisSubmitterId/variantcall-reference.json";
    val updatedPayloadFilename =
        "documents/updating/with-analysisSubmitterId/variantcall-updated-donorSubmitterId.json";
    val analysisPair = updatePayload(referencePayloadFilename, updatedPayloadFilename);
    log.info("done");
  }

  @Test
  @Ignore
  public void testSpecimenUpdate(){
    val referencePayloadFilename = "documents/updating/without-analysisSubmitterId/variantcall-reference.json";
    val updatedPayloadFilename =
        "documents/updating/without-analysisSubmitterId/variantcall-updated-specimenSubmitterId.json";
    val analysisPair = updatePayload(referencePayloadFilename, updatedPayloadFilename);
    log.info("done");
  }

  @Test
  @Ignore
  public void testSampleUpdate(){
    val referencePayloadFilename = "documents/updating/without-analysisSubmitterId/variantcall-reference.json";
    val updatedPayloadFilename =
        "documents/updating/without-analysisSubmitterId/variantcall-updated-sampleSubmitterId.json";
    val analysisPair = updatePayload(referencePayloadFilename, updatedPayloadFilename);
    log.info("done");

  }

  @SneakyThrows
  private AnalysisPair updatePayload(String referencePayloadFilename, String updatedPayloadFilename ){
    val study="ABC123";
    val referencePayload = getJsonNodeFromClasspath(referencePayloadFilename);
    val updatedPayload = getJsonNodeFromClasspath(updatedPayloadFilename);

    // Upload reference
    val referenceUploadStatus = uploadService.upload(study, JsonUtils.toJson(referencePayload), false );
    val referenceStatus = fromStatus(referenceUploadStatus, "status");
    val referenceUploadId = fromStatus(referenceUploadStatus,"uploadId");
    assertThat(referenceStatus).isEqualTo("ok");

    // Upload updated
    val updatedUploadStatus = uploadService.upload(study,JsonUtils.toJson(updatedPayload), false);
    val updatedStatus = fromStatus(updatedUploadStatus, "status");
    val updatedUploadId = fromStatus(updatedUploadStatus, "uploadId");
    if (updatedUploadId.equals(referenceUploadId)){
      assertThat(updatedStatus).contains("WARNING: replaced content for");
    } else {
      assertThat(updatedStatus).isEqualTo("ok");
    }

    // Read updatedUploadId and verify
//    val readUpdatedUpload = JsonUtils.readTree(uploadService.read(updatedUploadId).getPayload());

    // Save reference upload
    val referenceAnalysisStatus = uploadService.save(study,referenceUploadId);
    val referenceAnalysisId = fromStatus(referenceAnalysisStatus, "analysisId");
    val referenceAnalysis =  analysisService.read(referenceAnalysisId);

    // Save updated upload
    val updatedAnalysisStatus = uploadService.save(study,updatedUploadId);
    val updatedAnalysisId = fromStatus(updatedAnalysisStatus, "analysisId");
    val updatedAnalysis =  analysisService.read(updatedAnalysisId);
    return new AnalysisPair(referenceUploadId, updatedUploadId,referenceAnalysis,updatedAnalysis);
  }

  @Value
  public static class AnalysisPair {
    @NonNull private final String referenceUploadId;
    @NonNull private final String updatedUploadId;
    @NonNull private final Analysis referenceAnalysis;
    @NonNull private final Analysis updatedAnalysis;
  }

  protected JsonNode getJsonNodeFromClasspath(String name) throws Exception {
    InputStream is1 = Thread.currentThread().getContextClassLoader().getResourceAsStream(name);
    ObjectMapper mapper = new ObjectMapper();
    JsonNode node = mapper.readTree(is1);
    return node;
  }


  private static String getDonorSubmitterId(JsonNode j, int sampleIdx){
   return j.path("sample").path(sampleIdx).path("donor").path("donorSubmitterId").textValue();
  }

  private static JsonNode updateDonorSubmitterId(JsonNode j, int sampleIdx, String newDonorSubmitterId){
    val node = (ObjectNode)j.path("sample").path(sampleIdx).path("donor");
    node.put("donorSubmitterId", newDonorSubmitterId);
    return j;
  }

  @SneakyThrows
  private static String updateDonorSubmitterIdString(String json, int sampleIdx, String newDonorSubmittedId){
    val j = JsonUtils.readTree(json);
    updateDonorSubmitterId(j, 0, newDonorSubmittedId);
    return JsonUtils.toJson(j);
  }

  @SneakyThrows
  @Test public void testSyncUpdate() {
    val fileName = "variantCallWithSubmitterId.json";
    val study="ABC123";
    val json = readFile(fileName);
    val uploadStatus = uploadService.upload(study, json, false );
    log.info(format("UploadStatus='%s'",uploadStatus));
    val uploadId = fromStatus(uploadStatus,"uploadId");
    val status = fromStatus(uploadStatus, "status");
    assertThat(status).isEqualTo("ok");

    val json2 = json.replace("silver bullet","golden hammer");
    assertThat(json).isNotEqualTo(json2);
    val uploadStatus2 = uploadService.upload(study, json2, false);
    val uploadId2 =  fromStatus(uploadStatus2,"uploadId");
    val status2 = fromStatus(uploadStatus2, "status");
    assertThat(status2).isEqualTo("WARNING: replaced content for analysisSubmitterId 'VariantCall X24Alpha'");


    val upload = uploadService.read(uploadId2);
    assertThat(upload.getPayload()).isEqualTo(json2);
    assertThat(upload.getState()).isEqualTo("VALIDATED");

    // test save
    val response = uploadService.save(study,uploadId);
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

  private String read(String uploadId) {
    Upload status = uploadService.read(uploadId);
    return status.getState();
  }


  private String validate(String uploadId) throws InterruptedException {
    String state=read(uploadId);
    // wait for the server to finish
    while(state.equals("CREATED") || state.equals("UPDATED")) {
      Thread.sleep(50);
      state=read(uploadId);
    }
    return state;
  }

  @SneakyThrows
  private void test(String fileName, boolean isAsyncValidation) {
    val study="ABC123";
    val json = readFile(fileName);

    // test upload
    val uploadStatus=uploadService.upload(study, json, isAsyncValidation);
    assertThat(uploadStatus.getStatusCode()).isEqualTo(OK);
    val uploadId= fromStatus(uploadStatus,"uploadId");
    log.info(format("UploadId='%s'",uploadId));
    assertThat(uploadId.startsWith("UP")).isTrue();

    val initialState = read(uploadId);
    if (isAsyncValidation){
      // test create for Asynchronous case
      assertThat(initialState).isEqualTo("CREATED");
    } else {
      assertThat(initialState).isEqualTo("VALIDATED"); //Synchronous should always return VALIDATED
    }

    // test validation
    val finalState = validate(uploadId);
    assertThat(finalState).isEqualTo("VALIDATED");

    // test save
   val response = uploadService.save(study,uploadId);
   assertThat(response.getStatusCode()).isEqualTo(OK);
  }

}
