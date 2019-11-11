package bio.overture.song.server.service.id;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@RequiredArgsConstructor
public class FederatedIdService implements IdService {

  /**
   * Dependencies
   */
  @NonNull private final RestTemplate rest;
  @NonNull private final RetryTemplate retry;
  @NonNull private final UriResolver uriResolver;

  @Override
  public Optional<String> resolveFileId(@NonNull String analysisId, @NonNull String fileName) {
    return getString(uriResolver.expandFileUri(analysisId, fileName));
  }

  @Override
  public Optional<String> resolveDonorId(@NonNull String studyId, @NonNull String submitterDonorId) {
    return getString(uriResolver.expandDonorUri(studyId, submitterDonorId));
  }

  @Override
  public Optional<String> resolveSpecimenId(@NonNull String studyId, @NonNull String submitterSpecimenId) {
    return getString(uriResolver.expandSpecimenUri(studyId, submitterSpecimenId));
  }

  @Override
  public Optional<String> resolveSampleId(@NonNull String studyId, @NonNull String submitterSampleId) {
    return getString(uriResolver.expandSampleUri(studyId, submitterSampleId));
  }

  @Override
  public boolean isAnalysisIdExist(@NonNull String analysisId) {
    return get(uriResolver.expandAnalysisExistenceUri(analysisId), Object.class).isPresent();
  }

  @Override
  public Optional<String> uniqueCandidateAnalysisId() {
    return getString(uriResolver.expandAnalysisGenerateUri());
  }

  @Override
  public void saveAnalysisId(@NonNull String analysisId) {
    executeGetRequest(uriResolver.expandAnalysisSaveUri(analysisId), Object.class);
  }

  private Optional<String> getString(String url){
    return get(url, String.class);
  }

  private <T> Optional <T> get(@NonNull String url, @NonNull Class<T> responseType){
    try {
      return Optional.of(executeGetRequest(url, responseType).getBody());
    } catch (HttpStatusCodeException e){
      if (NOT_FOUND.equals(e.getStatusCode())){
        return Optional.empty();
      }
      throw e;
    }
  }

  private <T> ResponseEntity<T> executeGetRequest(String url, Class<T> responseType){
    return retry.execute( retryContext -> {
      val httpEntity = new HttpEntity<>(new HttpHeaders());
      return rest.exchange(url, HttpMethod.GET, httpEntity, responseType);
    });
  }
}
