package bio.overture.song.sdk;

import static lombok.AccessLevel.PRIVATE;

import bio.overture.song.sdk.config.RestClientConfig;
import bio.overture.song.sdk.config.RetryConfig;
import bio.overture.song.sdk.config.SdkConfig;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

@Getter
@RequiredArgsConstructor(access = PRIVATE)
public class Toolbox {

  @NonNull private final SongApi songApi;
  @NonNull private final ManifestClient manifestClient;

  public static Toolbox createToolbox(@NonNull SdkConfig config) {
    return createToolbox(config.getClient(), config.getRetry());
  }

  public static Toolbox createToolbox(
      @NonNull RestClientConfig restClientConfig, @NonNull RetryConfig retryConfig) {
    val factory =
        Factory.builder().retryConfig(retryConfig).restClientConfig(restClientConfig).build();
    val songApi = factory.buildSongApi();
    val manifestClient = new ManifestClient(songApi);
    return new Toolbox(songApi, manifestClient);
  }
}
