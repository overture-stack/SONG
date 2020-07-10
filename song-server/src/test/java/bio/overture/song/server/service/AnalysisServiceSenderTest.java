package bio.overture.song.server.service;

import bio.overture.song.core.model.enums.AnalysisStates;
import bio.overture.song.core.utils.JsonUtils;
import bio.overture.song.core.utils.RandomGenerator;
import bio.overture.song.server.kafka.AnalysisMessage;
import bio.overture.song.server.kafka.Sender;
import bio.overture.song.server.model.dto.Payload;
import bio.overture.song.server.service.analysis.AnalysisService;
import bio.overture.song.server.service.analysis.AnalysisServiceImpl;
import bio.overture.song.server.service.analysis.AnalysisServiceSender;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mockingDetails;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static bio.overture.song.core.model.enums.AnalysisStates.PUBLISHED;
import static bio.overture.song.core.model.enums.AnalysisStates.SUPPRESSED;
import static bio.overture.song.core.model.enums.AnalysisStates.UNPUBLISHED;
import static bio.overture.song.core.utils.JsonUtils.convertValue;
import static bio.overture.song.core.utils.RandomGenerator.createRandomGenerator;
import static bio.overture.song.server.kafka.AnalysisMessage.createAnalysisMessage;

@Slf4j
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(MockitoJUnitRunner.class)
public class AnalysisServiceSenderTest {

  private static final RandomGenerator RANDOM_GENERATOR = createRandomGenerator(AnalysisServiceSenderTest.class.getSimpleName());
  private static final String SONG_ID = "some.song.id";
  private static final Payload DUMMY_PAYLOAD = new Payload();
  private static final ResponseEntity<String> DUMMY_RESPONSE = ResponseEntity.ok("some text");

  @Mock private AnalysisService internalAnalysisService;

  /**
   * State
   */
  private String studyId;
  private String analysisId;

  @Before
  public void beforeTest(){
    this.studyId = RANDOM_GENERATOR.generateRandomAsciiString(10);
    this.analysisId = RANDOM_GENERATOR.generateRandomUUIDAsString();
    this.studyId = RANDOM_GENERATOR.generateRandomAsciiString(15);
  }

  @Test
  public void testAnalysisCreate(){
    when(internalAnalysisService.create(studyId, DUMMY_PAYLOAD)).thenReturn(analysisId);
    val analysisServiceSender = createTestAnalysisServiceSender(UNPUBLISHED);
    val actualAnalysisId = analysisServiceSender.create(studyId, DUMMY_PAYLOAD);
    assertEquals(analysisId, actualAnalysisId);
  }

  @Test
  public void testAnalysisPublish(){
    when(internalAnalysisService.publish(studyId, analysisId, false)).thenReturn(DUMMY_RESPONSE);
    val analysisServiceSender = createTestAnalysisServiceSender(PUBLISHED);
    val response = analysisServiceSender.publish(studyId, analysisId, false);
    assertEquals(DUMMY_RESPONSE, response);
  }

  @Test
  public void testAnalysisUnpublish(){
    when(internalAnalysisService.unpublish(studyId, analysisId)).thenReturn(DUMMY_RESPONSE);
    val analysisServiceSender = createTestAnalysisServiceSender(UNPUBLISHED);
    val response = analysisServiceSender.unpublish(studyId, analysisId);
    assertEquals(DUMMY_RESPONSE, response);
  }

  @Test
  public void testAnalysisSuppressed(){
    when(internalAnalysisService.suppress(studyId, analysisId)).thenReturn(DUMMY_RESPONSE);
    val analysisServiceSender = createTestAnalysisServiceSender(SUPPRESSED);
    val response = analysisServiceSender.unpublish(studyId, analysisId);
    assertEquals(DUMMY_RESPONSE, response);
  }

  private AnalysisServiceSender createTestAnalysisServiceSender(AnalysisStates state){
    val sender = createTestSender(state);
    return new AnalysisServiceSender(SONG_ID, sender, internalAnalysisService);
  }

  private TestSender createTestSender(AnalysisStates state){
    return new TestSender(createExpectedAnalysisMessage(state));
  }

  private AnalysisMessage createExpectedAnalysisMessage(AnalysisStates state){
    return createAnalysisMessage(analysisId, studyId, state, SONG_ID);
  }

  @RequiredArgsConstructor
  public static class TestSender implements Sender {
    @NonNull private final AnalysisMessage expectedAnalysisMessage;

    @Override
    @SneakyThrows
    public void send(String payload) {
      val actualAnalysisMessage = JsonUtils.mapper().readValue(payload, AnalysisMessage.class);
      assertEquals(expectedAnalysisMessage, actualAnalysisMessage);
    }
  }

}
