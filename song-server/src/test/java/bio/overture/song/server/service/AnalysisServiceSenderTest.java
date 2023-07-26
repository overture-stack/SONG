package bio.overture.song.server.service;

import static bio.overture.song.core.model.enums.AnalysisActions.*;
import static bio.overture.song.core.model.enums.AnalysisStates.UNPUBLISHED;
import static bio.overture.song.core.utils.JsonUtils.toJson;
import static bio.overture.song.core.utils.RandomGenerator.createRandomGenerator;
import static bio.overture.song.server.kafka.AnalysisMessage.createAnalysisMessage;
import static bio.overture.song.server.utils.generator.AnalysisGenerator.createAnalysisGenerator;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import bio.overture.song.core.model.enums.AnalysisActions;
import bio.overture.song.core.utils.RandomGenerator;
import bio.overture.song.server.kafka.AnalysisMessage;
import bio.overture.song.server.kafka.Sender;
import bio.overture.song.server.model.analysis.Analysis;
import bio.overture.song.server.model.analysis.AnalysisData;
import bio.overture.song.server.model.dto.Payload;
import bio.overture.song.server.model.entity.AnalysisSchema;
import bio.overture.song.server.service.analysis.AnalysisService;
import bio.overture.song.server.service.analysis.AnalysisServiceSender;
import bio.overture.song.server.utils.generator.AnalysisGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.ResponseEntity;

@Slf4j
@RunWith(MockitoJUnitRunner.class)
public class AnalysisServiceSenderTest {

  private static final RandomGenerator RANDOM_GENERATOR =
      createRandomGenerator(AnalysisServiceSenderTest.class.getSimpleName());
  private static final String SONG_ID = "some.song.id";
  private static final Payload DUMMY_PAYLOAD = new Payload();
  private static final ResponseEntity<String> DUMMY_RESPONSE = ResponseEntity.ok("some text");

  @Mock private AnalysisService internalAnalysisService;

  @Mock private FileService fileService;

  /** State */
  private String studyId;

  private String analysisId;

  private AnalysisGenerator analysisGenerator;
  private Analysis analysis;

  @Before
  public void beforeTest() {
    this.studyId = RANDOM_GENERATOR.generateRandomAsciiString(10);
    this.analysisId = RANDOM_GENERATOR.generateRandomUUIDAsString();

    analysisGenerator = createAnalysisGenerator(studyId, internalAnalysisService, RANDOM_GENERATOR);
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
    val analysisServiceSender = createTestAnalysisServiceSender(CREATE);
    val actualAnalysis = analysisServiceSender.create(studyId, DUMMY_PAYLOAD);
    assertEquals(analysisId, actualAnalysis.getAnalysisId());
  }

  @Test
  public void testAnalysisPublish() {
    when(internalAnalysisService.publish(studyId, analysisId, false)).thenReturn(analysis);
    val analysisServiceSender = createTestAnalysisServiceSender(PUBLISH);
    val response = analysisServiceSender.publish(studyId, analysisId, false);
    assertEquals(analysis, response);
  }

  @Test
  public void testAnalysisUnpublish() {
    when(internalAnalysisService.unpublish(studyId, analysisId)).thenReturn(analysis);
    val analysisServiceSender = createTestAnalysisServiceSender(UNPUBLISH);
    val response = analysisServiceSender.unpublish(studyId, analysisId);
    assertEquals(analysis, response);
  }

  @Test
  public void testAnalysisSuppressed() {
    when(internalAnalysisService.suppress(studyId, analysisId)).thenReturn(analysis);
    val analysisServiceSender = createTestAnalysisServiceSender(SUPPRESS);
    val response = analysisServiceSender.suppress(studyId, analysisId);
    assertEquals(analysis, response);
  }

  private AnalysisServiceSender createTestAnalysisServiceSender(AnalysisActions action) {
    val sender = createTestSender(action);
    return new AnalysisServiceSender(SONG_ID, sender, internalAnalysisService, fileService);
  }

  private TestSender createTestSender(AnalysisActions action) {
    val analysisMessage = createExpectedAnalysisMessage(action);
    return new TestSender(toJson((analysisMessage)));
  }

  private AnalysisMessage createExpectedAnalysisMessage(AnalysisActions action) {
    return createAnalysisMessage(action, this.analysis, SONG_ID);
  }

  @RequiredArgsConstructor
  public static class TestSender implements Sender {
    @NonNull private final String expectedAnalysisMessage;

    @Override
    @SneakyThrows
    public void send(String actualAnalysisMessage, String _key) {
      assertEquals(expectedAnalysisMessage, actualAnalysisMessage);
    }
  }
}
