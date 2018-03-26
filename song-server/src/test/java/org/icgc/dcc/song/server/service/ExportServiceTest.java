package org.icgc.dcc.song.server.service;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.icgc.dcc.song.core.utils.JsonUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class})
@ActiveProfiles("dev")
public class ExportServiceTest {

  @Autowired
  private ExportService exportService;

  @Autowired
  private UploadService uploadService;

  @Autowired
  private AnalysisService analysisService;


  @Test
  @SneakyThrows
  public void testFullLoop(){
    /*
    val generator = Generators.randomBasedGenerator();

    val analysisPayload = new SequencingReadAnalysis();
    analysisPayload.setStudy("ABC123");
    val f = File.create(null, null, "myFile.bam", "AB");

    val json = exportService.exportPayload("AN2", true);
    ((ObjectNode)json).put("analysisId", "AN_ROB_1");

    val payload = JsonUtils.toJson(json);
    val uploadStatus = uploadService.upload("ABC123", payload, false);
    val status1 = fromStatus(uploadStatus, "status");
    assertThat(status1).isEqualTo("ok");
    val uploadId = fromStatus(uploadStatus, "uploadId");
    val statusResponse = uploadService.read(uploadId);
    val uploadState = resolveState(statusResponse.getState());
    assertThat(uploadState).isEqualTo(UploadStates.VALIDATED);
    val analysisResponse = uploadService.save("ABC123", uploadId, false);
    val status2 = fromStatus(analysisResponse, "status");
    val analysisId = fromStatus(analysisResponse, "analysisId");
    val analysis = analysisService.read(analysisId);
    log.info("sdf");
    */
  }

  @SneakyThrows
  public String fromStatus( ResponseEntity<String> uploadStatus, String key) {
    val uploadId = JsonUtils.readTree(uploadStatus.getBody()).at("/"+key).asText("");
    return uploadId;
  }


}
