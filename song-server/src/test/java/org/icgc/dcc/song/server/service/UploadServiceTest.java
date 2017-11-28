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
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Builder;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.icgc.dcc.id.client.core.IdClient;
import org.icgc.dcc.song.core.exceptions.ServerException;
import org.icgc.dcc.song.core.utils.JsonUtils;
import org.icgc.dcc.song.server.model.Upload;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import java.nio.file.Files;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.icgc.dcc.song.core.utils.JsonUtils.toJson;
import static org.icgc.dcc.song.server.utils.TestFiles.getJsonNodeFromClasspath;
import static org.springframework.http.HttpStatus.OK;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class })
@ActiveProfiles({"dev", "secure", "test"})
public class UploadServiceTest {

  private static final String FILEPATH = "../src/test/resources/fixtures/";
  private static final String DEFAULT_STUDY = "ABC123";
  private static int ANALYSIS_ID_COUNT = 0;

  @Autowired
  UploadService uploadService;

  @Autowired
  IdClient idClient;

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
    val json = readFile(fileName);
    val uploadStatus = uploadService.upload(DEFAULT_STUDY, json, false );
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
    val json = readFile(fileName);
    val uploadStatus = uploadService.upload(DEFAULT_STUDY, json, false );
    val uploadId = fromStatus(uploadStatus,"uploadId");
    val upload = uploadService.read(uploadId);
    assertThat(upload.getState()).isEqualTo("VALIDATED");
  }


  @Test
  public void testASyncUpload(){
    val fileName = "sequencingRead.json";
    val json = readFile(fileName);
    val uploadStatus = uploadService.upload(DEFAULT_STUDY, json, true );
    val uploadId = fromStatus(uploadStatus,"uploadId");
    val upload = uploadService.read(uploadId);
    assertThat(upload.getState()).isEqualTo("CREATED");
  }

  @SneakyThrows
  @Test public void testAsyncUpdate() {
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
    val upload = uploadService.read(uploadId2);
    assertThat(upload.getPayload()).isEqualTo(json2);
    assertThat(upload.getState()).isEqualTo("UPDATED");

    // test validation
    val finalState = validate(uploadId);
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


    val upload = uploadService.read(uploadId2);
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
  private String uploadFromFixtureDir(String study, String fileName, boolean
      isAsyncValidation){
    val json = readFile(fileName);
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
    try{
      val response1 = uploadService.save(DEFAULT_STUDY, uploadId1, false);
      fail("Collision was not detected!");
    } catch (ServerException e){
      val errorId = e.getSongError().getErrorId();
      assertThat(errorId).isEqualTo("analysis.id.already.exists");
      assertThat(e.getSongError().getMessage()).contains("Collision detected");
    }

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
    try{
      val response2 = uploadService.save(DEFAULT_STUDY, uploadId1, true);
      fail(format("Should not be able to create 2 analysis with the same id (%s)!",expectedAnalysisId));
    } catch (ServerException e){
      val errorId = e.getSongError().getErrorId();
      assertThat(errorId).isEqualTo("duplicate.analysis.attempt");
      assertThat(e.getSongError().getMessage()).contains("Attempted to create a duplicate analysis");
    }
  }

  private static String createUniqueAnalysisId(){
    return format("AN-56789-%s",ANALYSIS_ID_COUNT++);
  }

  private static JsonNode updateAnalysisId(JsonNode json, String analysisId){
    val obj = (ObjectNode)json;
    obj.put("analysisId", analysisId);
    return json;
  }

  private static Payload createPayloadWithDifferentAnalysisId(){
    val filename = "documents/sequencingread-valid.json";
    val json = getJsonNodeFromClasspath(filename);
    val analysisId =createUniqueAnalysisId();
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
