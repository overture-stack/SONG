package bio.overture.song.server.service.id;

import bio.overture.song.core.utils.RandomGenerator;
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

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static bio.overture.song.core.exceptions.ServerErrors.ID_SERVICE_ERROR;
import static bio.overture.song.core.testing.SongErrorAssertions.assertSongError;
import static bio.overture.song.core.utils.RandomGenerator.createRandomGenerator;
import static bio.overture.song.server.service.id.FederatedIdServiceTest.MODE.ERROR;
import static bio.overture.song.server.service.id.FederatedIdServiceTest.MODE.GOOD;
import static bio.overture.song.server.service.id.FederatedIdServiceTest.MODE.NOT_FOUND;

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
  private static final String DONOR_URL = "https://example.org/donor/id";
  private static final String SPECIMEN_URL = "https://example.org/specimen/id";
  private static final String SAMPLE_URL = "https://example.org/sample/id";

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
  public void getDonor_existing_id() {
    setupDonor(GOOD);
    val donorResult = idService.getDonorId(DEFAULT_STUDY_ID, DEFAULT_SUBMITTER_ID);
    assertTrue(donorResult.isPresent());
    assertEquals(donorResult.get(), DEFAULT_ID);
  }

  @Test
  public void getDonor_nonExisting_emptyResult() {
    setupDonor(NOT_FOUND);
    val donorResult = idService.getDonorId(DEFAULT_STUDY_ID, DEFAULT_SUBMITTER_ID);
    assertFalse(donorResult.isPresent());
  }

  @Test
  public void getDonor_otherError_IdServiceError() {
    setupDonor(ERROR);
    assertSongError(
        () -> idService.getDonorId(DEFAULT_STUDY_ID, DEFAULT_SUBMITTER_ID), ID_SERVICE_ERROR);
  }

  @Test
  public void getSpecimen_existing_id() {
    setupSpecimen(GOOD);
    val specimenResult = idService.getSpecimenId(DEFAULT_STUDY_ID, DEFAULT_SUBMITTER_ID);
    assertTrue(specimenResult.isPresent());
    assertEquals(specimenResult.get(), DEFAULT_ID);
  }

  @Test
  public void getSpecimen_nonExisting_emptyResult() {
    setupSpecimen(NOT_FOUND);
    val specimenResult = idService.getSpecimenId(DEFAULT_STUDY_ID, DEFAULT_SUBMITTER_ID);
    assertFalse(specimenResult.isPresent());
  }

  @Test
  public void getSpecimen_otherError_IdServiceError() {
    setupSpecimen(ERROR);
    assertSongError(
        () -> idService.getSpecimenId(DEFAULT_STUDY_ID, DEFAULT_SUBMITTER_ID), ID_SERVICE_ERROR);
  }

  @Test
  public void getSample_existing_id() {
    setupSample(GOOD);
    val sampleResult = idService.getSampleId(DEFAULT_STUDY_ID, DEFAULT_SUBMITTER_ID);
    assertTrue(sampleResult.isPresent());
    assertEquals(sampleResult.get(), DEFAULT_ID);
  }

  @Test
  public void getSample_nonExisting_emptyResult() {
    setupSample(NOT_FOUND);
    val sampleResult = idService.getSampleId(DEFAULT_STUDY_ID, DEFAULT_SUBMITTER_ID);
    assertFalse(sampleResult.isPresent());
  }

  @Test
  public void getSample_otherError_IdServiceError() {
    setupSample(ERROR);
    assertSongError(
        () -> idService.getSampleId(DEFAULT_STUDY_ID, DEFAULT_SUBMITTER_ID), ID_SERVICE_ERROR);
  }

  @Test
  public void testObjectId(){
    val expectedObjectId = UUID.randomUUID().toString();
    when(localIdService.getObjectId(DEFAULT_ANALYSIS_ID, DEFAULT_FILE_NAME)).thenReturn(Optional.of(expectedObjectId));
    val result = idService.getObjectId(DEFAULT_ANALYSIS_ID, DEFAULT_FILE_NAME);
    assertTrue(result.isPresent());
    val actualObjectId = result.get();
    assertEquals(actualObjectId, expectedObjectId);
    verify(localIdService, times(1)).getObjectId(DEFAULT_ANALYSIS_ID, DEFAULT_FILE_NAME);
  }

  @Test
  public void testUniqueAnalysisId() {
    when(localIdService.generateAnalysisId()).thenReturn("AN1", "AN2");
    val id1 = idService.generateAnalysisId();
    val id2 = idService.generateAnalysisId();
    assertNotEquals(id1, id2);
  }

  private void setupDonor(MODE mode) {
    baseSetup(mode, DONOR_URL);
    when(uriResolver.expandDonorUri(anyString(), anyString())).thenReturn(DONOR_URL);
  }

  private void setupSpecimen(MODE mode) {
    baseSetup(mode, SPECIMEN_URL);
    when(uriResolver.expandSpecimenUri(anyString(), anyString())).thenReturn(SPECIMEN_URL);
  }

  private void setupSample(MODE mode) {
    baseSetup(mode, SAMPLE_URL);
    when(uriResolver.expandSampleUri(anyString(), anyString())).thenReturn(SAMPLE_URL);
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
