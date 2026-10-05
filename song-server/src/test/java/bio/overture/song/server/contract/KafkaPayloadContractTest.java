package bio.overture.song.server.contract;

import static bio.overture.song.server.utils.contract.GoldenFiles.assertMatchesGolden;
import static org.junit.jupiter.api.Assertions.assertEquals;

import bio.overture.song.server.utils.contract.JsonNormalizer;
import bio.overture.song.server.utils.contract.RawResponse;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

/**
 * Locks the analysis state-change messages sent to Kafka, which Maestro consumes, with golden files
 * under {@code golden/kafka}.
 *
 * <p>Each test performs one action through the HTTP API on the same analysis, in order, and
 * compares every message sent during that action (key and payload) with the golden file. The golden
 * file is a list, so a missing or extra message also fails.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class KafkaPayloadContractTest extends AbstractContractTest {

  private static final String STUDY_ID = "CONTRACT-KAFKA";

  private String analysisId;

  @BeforeAll
  public void setUpStudy() {
    createStudy(STUDY_ID);
    recordingSender.takeSentMessages();
  }

  private JsonNormalizer createMessageNormalizer() {
    return withAnalysisIds(createNormalizer(), "kafka", STUDY_ID, analysisId);
  }

  private String analysisPath() {
    return "/studies/" + STUDY_ID + "/analysis/" + analysisId;
  }

  private static void assertOk(RawResponse response) {
    assertEquals(200, response.getStatus(), response.getBody());
  }

  @SneakyThrows
  private void assertSentMessagesMatchGolden(String goldenName) {
    val normalizer = createMessageNormalizer();
    val messages = MAPPER.createArrayNode();
    for (val sentMessage : recordingSender.takeSentMessages()) {
      val message = messages.addObject();
      message.put("key", sentMessage.getKey());
      message.set("payload", MAPPER.readTree(sentMessage.getPayload()));
    }
    assertMatchesGolden(goldenName, normalizer.normalize(messages));
  }

  @Test
  @Order(1)
  public void create() {
    analysisId = submit(STUDY_ID, readVariantCallPayload(STUDY_ID));
    assertSentMessagesMatchGolden("kafka/create");
  }

  @Test
  @Order(2)
  public void updateAnalysis() {
    val request = MAPPER.createObjectNode();
    request.putObject("analysisType").put("name", "variantCall").put("version", 1);
    request
        .putObject("experiment")
        .put("variantCallingTool", "updated tool")
        .put("matchedNormalSampleSubmitterId", "sample-x24-11a");
    request.putObject("info").put("description", "updated info");
    assertOk(client.put(analysisPath(), request.toString()));
    assertSentMessagesMatchGolden("kafka/update");
  }

  @Test
  @Order(3)
  public void patchUpdateAnalysis() {
    val request = MAPPER.createObjectNode();
    request.putObject("experiment").put("variantCallingTool", "patched tool");
    assertOk(client.patch(analysisPath(), request.toString()));
    assertSentMessagesMatchGolden("kafka/patch");
  }

  @Test
  @Order(4)
  public void publish() {
    stubStorageWithUploadedFiles(STUDY_ID, analysisId);
    assertOk(
        client.send("PUT", "/studies/" + STUDY_ID + "/analysis/publish/" + analysisId, null, null));
    assertSentMessagesMatchGolden("kafka/publish");
  }

  @Test
  @Order(5)
  public void unpublish() {
    assertOk(
        client.send(
            "PUT", "/studies/" + STUDY_ID + "/analysis/unpublish/" + analysisId, null, null));
    assertSentMessagesMatchGolden("kafka/unpublish");
  }

  @Test
  @Order(6)
  public void suppress() {
    assertOk(
        client.send(
            "PUT", "/studies/" + STUDY_ID + "/analysis/suppress/" + analysisId, null, null));
    assertSentMessagesMatchGolden("kafka/suppress");
  }
}
