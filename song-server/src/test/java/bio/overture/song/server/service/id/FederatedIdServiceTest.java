package bio.overture.song.server.service.id;

import static bio.overture.song.core.utils.RandomGenerator.createRandomGenerator;
import static bio.overture.song.server.service.id.FederatedIdServiceTest.MODE.ERROR;
import static bio.overture.song.server.service.id.FederatedIdServiceTest.MODE.GOOD;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import bio.overture.song.core.utils.RandomGenerator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.val;
import org.apache.commons.lang.NotImplementedException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;

@RunWith(MockitoJUnitRunner.class)
public class FederatedIdServiceTest {

  /** Constants */
  enum MODE {
    ERROR,
    GOOD,
    NOT_FOUND;
  }

  private static final String DEFAULT_FILE_NAME = "someFileName";
  private static final String DEFAULT_ANALYSIS_ID = "someAnalysisId";
  private static final String DEFAULT_STUDY_ID = "someStudyId";
  private static final String DEFAULT_SUBMITTER_ID = "someSubId";
  private static final String DEFAULT_ID = "someId";

  /** Mocks */
  @Mock private RestClient restClient;

  @Mock private UriResolver uriResolver;
  @Mock private LocalIdService localIdService;
  @InjectMocks private FederatedIdService idService;

  /** State */
  private RandomGenerator randomGenerator;

  @Before
  public void beforeTest() {
    randomGenerator = createRandomGenerator(getClass().getSimpleName());
    reset(restClient, uriResolver);
  }

  @Test
  public void testFileId() {
    val expectedObjectId = UUID.randomUUID().toString();
    when(localIdService.getFileId(DEFAULT_ANALYSIS_ID, DEFAULT_FILE_NAME))
        .thenReturn(Optional.of(expectedObjectId));
    val result = idService.getFileId(DEFAULT_ANALYSIS_ID, DEFAULT_FILE_NAME);
    assertTrue(result.isPresent());
    val actualObjectId = result.get();
    assertEquals(actualObjectId, expectedObjectId);
    verify(localIdService, times(1)).getFileId(DEFAULT_ANALYSIS_ID, DEFAULT_FILE_NAME);
  }

  @Test
  public void testUniqueAnalysisId() {
    when(localIdService.generateAnalysisId()).thenReturn("AN1", "AN2");
    val id1 = idService.generateAnalysisId();
    val id2 = idService.generateAnalysisId();
    assertNotEquals(id1, id2);
  }

  private HttpStatusCodeException generateNonNotFoundStatusCodeException() {
    val nonNotFoundError =
        randomGenerator.shuffleList(List.of(HttpStatus.values())).stream()
            .filter(HttpStatus::isError)
            .filter(x -> x.value() != HttpStatus.NOT_FOUND.value())
            .findFirst()
            .get();
    return new HttpStatusCodeException(nonNotFoundError) {
      @Override
      public HttpStatus getStatusCode() {
        return nonNotFoundError;
      }
    };
  }

  private void baseSetup(MODE mode, String url) {
    if (mode == GOOD) {
      when(restClient.get(url, String.class)).thenReturn(ResponseEntity.ok(DEFAULT_ID));
    } else if (mode == MODE.NOT_FOUND) {
      when(restClient.get(url, String.class))
          .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));
    } else if (mode == ERROR) {
      val ex = generateNonNotFoundStatusCodeException();
      when(restClient.get(url, String.class)).thenThrow(ex);
    } else {
      throw new NotImplementedException("Cannot processes mode == " + mode);
    }

    when(restClient.getString(url)).thenCallRealMethod();
    when(restClient.getObject(url, String.class)).thenCallRealMethod();
  }
}
