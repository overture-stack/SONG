package bio.overture.song.server.service;

import static bio.overture.song.core.model.enums.AnalysisActions.*;
import static bio.overture.song.core.model.enums.AnalysisStates.UNPUBLISHED;
import static bio.overture.song.core.utils.JsonUtils.toJson;
import static bio.overture.song.core.utils.RandomGenerator.createRandomGenerator;
import static bio.overture.song.server.kafka.AnalysisMessage.createAnalysisMessage;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import bio.overture.song.core.model.enums.AnalysisActions;
import bio.overture.song.core.utils.RandomGenerator;
import bio.overture.song.server.kafka.Sender;
import bio.overture.song.server.model.analysis.Analysis;
import bio.overture.song.server.model.analysis.AnalysisData;
import bio.overture.song.server.model.dto.Payload;
import bio.overture.song.server.model.entity.AnalysisSchema;
import bio.overture.song.server.service.analysis.AnalysisService;
import bio.overture.song.server.service.analysis.AnalysisServiceSender;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Slf4j
@ExtendWith(MockitoExtension.class)
public class AnalysisServiceSenderTest {

  private static final RandomGenerator RANDOM_GENERATOR =
      createRandomGenerator(AnalysisServiceSenderTest.class.getSimpleName());
  private static final String SONG_ID = "some.song.id";
  private static final Payload DUMMY_PAYLOAD = new Payload();
  private static final ResponseEntity<String> DUMMY_RESPONSE = ResponseEntity.ok("some text");

  @Mock private AnalysisService internalAnalysisService;

  /** State */
  private String studyId;

  private String analysisId;

  private Analysis analysis;

  @BeforeEach
  public void beforeTest() {
    this.studyId = RANDOM_GENERATOR.generateRandomAsciiString(10);
    this.analysisId = RANDOM_GENERATOR.generateRandomUUIDAsString();

    val analysisSchema =
        AnalysisSchema.builder()
            .name("test-schema")
            .version(1)
            .schema(new ObjectMapper().createObjectNode())
            .build();
    val analysisData = AnalysisData.builder().data(new ObjectMapper().createObjectNode()).build();
    this.analysis =
        Analysis.builder()
            .analysisId(this.analysisId)
            .studyId(this.studyId)
            .analysisState(UNPUBLISHED.name())
            .analysisSchema(analysisSchema)
            .analysisData(analysisData)
            .build();
    this.studyId = RANDOM_GENERATOR.generateRandomAsciiString(15);
  }

  @Test
  public void testAnalysisCreate() {
    when(internalAnalysisService.create(studyId, DUMMY_PAYLOAD)).thenReturn(analysis);
    val sender = createTestSender(CREATE, analysis);
    val actualAnalysis = createAnalysisServiceSender(sender).create(studyId, DUMMY_PAYLOAD);
    assertEquals(analysisId, actualAnalysis.getAnalysisId());
    sender.assertSentOnce();
  }

  /**
   * Inside a transaction, create waits for the commit and then sends the analysis as re-read from
   * the database, which can differ from the in-memory analysis returned by the create call.
   */
  @Test
  public void testAnalysisCreate_inTransaction_sendsCommittedAnalysisAfterCommit() {
    val committedAnalysis =
        Analysis.builder()
            .analysisId(analysisId)
            .studyId(analysis.getStudyId())
            .analysisState(UNPUBLISHED.name())
            .analysisSchema(analysis.getAnalysisSchema())
            .analysisData(
                AnalysisData.builder()
                    .data(new ObjectMapper().createObjectNode().put("committed", true))
                    .build())
            .build();
    when(internalAnalysisService.create(studyId, DUMMY_PAYLOAD)).thenReturn(analysis);
    when(internalAnalysisService.securedDeepRead(analysis.getStudyId(), analysisId))
        .thenReturn(committedAnalysis);
    val sender = createTestSender(CREATE, committedAnalysis);
    val analysisServiceSender = createAnalysisServiceSender(sender);

    TransactionSynchronizationManager.initSynchronization();
    try {
      analysisServiceSender.create(studyId, DUMMY_PAYLOAD);
      assertEquals(0, sender.getSendCount(), "Message was sent before the commit");

      TransactionSynchronizationManager.getSynchronizations()
          .forEach(TransactionSynchronization::afterCommit);
      sender.assertSentOnce();
    } finally {
      TransactionSynchronizationManager.clearSynchronization();
    }
  }

  @Test
  public void testAnalysisCreate_inTransactionRolledBack_sendsNothing() {
    when(internalAnalysisService.create(studyId, DUMMY_PAYLOAD)).thenReturn(analysis);
    val sender = createTestSender(CREATE, analysis);
    val analysisServiceSender = createAnalysisServiceSender(sender);

    TransactionSynchronizationManager.initSynchronization();
    try {
      analysisServiceSender.create(studyId, DUMMY_PAYLOAD);
      TransactionSynchronizationManager.getSynchronizations()
          .forEach(
              synchronization ->
                  synchronization.afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK));
      assertEquals(0, sender.getSendCount());
    } finally {
      TransactionSynchronizationManager.clearSynchronization();
    }
  }

  @Test
  public void testAnalysisPublish() {
    when(internalAnalysisService.publish(studyId, analysisId, false)).thenReturn(analysis);
    val sender = createTestSender(PUBLISH, analysis);
    val response = createAnalysisServiceSender(sender).publish(studyId, analysisId, false);
    assertEquals(analysis, response);
    sender.assertSentOnce();
  }

  @Test
  public void testAnalysisUnpublish() {
    when(internalAnalysisService.unpublish(studyId, analysisId)).thenReturn(analysis);
    val sender = createTestSender(UNPUBLISH, analysis);
    val response = createAnalysisServiceSender(sender).unpublish(studyId, analysisId);
    assertEquals(analysis, response);
    sender.assertSentOnce();
  }

  @Test
  public void testAnalysisSuppressed() {
    when(internalAnalysisService.suppress(studyId, analysisId)).thenReturn(analysis);
    val sender = createTestSender(SUPPRESS, analysis);
    val response = createAnalysisServiceSender(sender).suppress(studyId, analysisId);
    assertEquals(analysis, response);
    sender.assertSentOnce();
  }

  /** An update sends a message with the PUBLISH action, whatever the analysis state. */
  @Test
  public void testAnalysisUpdate() {
    val request = new ObjectMapper().createObjectNode().put("update", true);
    when(internalAnalysisService.updateAnalysis(studyId, analysisId, request)).thenReturn(analysis);
    val sender = createTestSender(PUBLISH, analysis);
    val response = createAnalysisServiceSender(sender).updateAnalysis(studyId, analysisId, request);
    assertEquals(analysis, response);
    sender.assertSentOnce();
  }

  /** A patch sends a message with the PUBLISH action, whatever the analysis state. */
  @Test
  public void testAnalysisPatchUpdate() {
    val request = new ObjectMapper().createObjectNode().put("patch", true);
    when(internalAnalysisService.patchUpdateAnalysis(studyId, analysisId, request))
        .thenReturn(analysis);
    val sender = createTestSender(PUBLISH, analysis);
    val response =
        createAnalysisServiceSender(sender).patchUpdateAnalysis(studyId, analysisId, request);
    assertEquals(analysis, response);
    sender.assertSentOnce();
  }

  private AnalysisServiceSender createAnalysisServiceSender(TestSender sender) {
    return new AnalysisServiceSender(SONG_ID, sender, internalAnalysisService);
  }

  private static TestSender createTestSender(AnalysisActions action, Analysis expectedAnalysis) {
    val analysisMessage = createAnalysisMessage(action, expectedAnalysis, SONG_ID);
    return new TestSender(toJson(analysisMessage), expectedAnalysis.getAnalysisId());
  }

  /** Checks each message against the expected payload and key, and counts the messages sent. */
  @RequiredArgsConstructor
  public static class TestSender implements Sender {
    @NonNull private final String expectedAnalysisMessage;
    @NonNull private final String expectedKey;
    @Getter private int sendCount = 0;

    @Override
    @SneakyThrows
    public void send(String actualAnalysisMessage, String actualKey) {
      sendCount++;
      assertEquals(expectedAnalysisMessage, actualAnalysisMessage);
      assertEquals(expectedKey, actualKey);
    }

    public void assertSentOnce() {
      assertEquals(1, sendCount, "Expected exactly one message to be sent");
    }
  }
}
