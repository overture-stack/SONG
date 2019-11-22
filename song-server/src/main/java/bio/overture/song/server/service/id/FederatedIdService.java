package bio.overture.song.server.service.id;

import static bio.overture.song.core.exceptions.ServerErrors.ID_SERVICE_ERROR;
import static bio.overture.song.core.exceptions.ServerException.buildServerException;
import static com.fasterxml.uuid.Generators.randomBasedGenerator;

import com.fasterxml.uuid.impl.RandomBasedGenerator;
import java.util.Optional;
import java.util.function.Function;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.web.client.HttpStatusCodeException;

/** Implementation that calls an external service for ID federation */
@RequiredArgsConstructor
public class FederatedIdService implements IdService {

  private static final RandomBasedGenerator RANDOM_UUID_GENERATOR = randomBasedGenerator();

  /** Dependencies */
  @NonNull private final RestClient rest;

  @NonNull private final UriResolver uriResolver;

  @Override
  public Optional<String> getFileId(@NonNull String analysisId, @NonNull String fileName) {
    return handleIdServiceGetRequest(
        uriResolver.expandFileUri(analysisId, fileName), rest::getString);
  }

  @Override
  public Optional<String> getDonorId(@NonNull String studyId, @NonNull String submitterDonorId) {
    return handleIdServiceGetRequest(
        uriResolver.expandDonorUri(studyId, submitterDonorId), rest::getString);
  }

  @Override
  public Optional<String> getSpecimenId(
      @NonNull String studyId, @NonNull String submitterSpecimenId) {
    return handleIdServiceGetRequest(
        uriResolver.expandSpecimenUri(studyId, submitterSpecimenId), rest::getString);
  }

  @Override
  public Optional<String> getSampleId(@NonNull String studyId, @NonNull String submitterSampleId) {
    return handleIdServiceGetRequest(
        uriResolver.expandSampleUri(studyId, submitterSampleId), rest::getString);
  }

  @Override
  public String generateAnalysisId() {
    return RANDOM_UUID_GENERATOR.generate().toString();
  }

  /**
   * This method calls the callback function with the input url, and if successfull (1xx/2xx/3xx
   * status code) returns the result, otherwise throws a ServerException
   */
  private static <T> T handleIdServiceGetRequest(String url, Function<String, T> restCallback) {
    try {
      return restCallback.apply(url);
    } catch (HttpStatusCodeException e) {
      val status = e.getStatusCode();
      val name = status.name();
      val code = status.value();
      throw buildServerException(
          FederatedIdService.class,
          ID_SERVICE_ERROR,
          "The request 'GET %s' failed with HttpStatus '%s[%s]' and message: %s",
          url,
          name,
          code,
          e.getMessage());
    }
  }
}
