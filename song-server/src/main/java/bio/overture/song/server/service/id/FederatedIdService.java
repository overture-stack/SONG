package bio.overture.song.server.service.id;

import static bio.overture.song.core.exceptions.ServerErrors.ID_SERVICE_ERROR;
import static bio.overture.song.core.exceptions.ServerException.buildServerException;

import java.util.Optional;
import java.util.function.Function;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.web.client.HttpStatusCodeException;

/** Implementation that calls an external service for ID federation */
@RequiredArgsConstructor
public class FederatedIdService implements IdService {

  /** Dependencies */
  @NonNull private final RestClient rest;

  @NonNull private final IdService localIdService;
  @NonNull private final UriResolver uriResolver;

  /** Always generate the analysisId locally */
  @Override
  public String generateAnalysisId() {
    return localIdService.generateAnalysisId();
  }

  // Always generate the objectId locally
  @Override
  public Optional<String> getFileId(@NonNull String analysisId, @NonNull String fileName) {
    return localIdService.getFileId(analysisId, fileName);
  }

  /**
   * This method calls the callback function with the input url, and if successful (1xx/2xx/3xx
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
