package bio.overture.song.sdk;

import bio.overture.song.sdk.config.Config;
import bio.overture.song.sdk.factory.SpringRegistryFactory;
import bio.overture.song.sdk.register.Registry;
import bio.overture.song.sdk.util.ManifestClient;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

import static lombok.AccessLevel.PRIVATE;

@Getter
@RequiredArgsConstructor(access = PRIVATE)
public class Toolbox {

  @NonNull private final Registry registry;
  @NonNull private final ManifestClient manifestClient;

  public static Toolbox create(@NonNull Config config){
    val factory = SpringRegistryFactory.builder()
        .restClientConfig(config.getClient())
        .retryConfig(config.getRetry())
        .build();
    val registry = factory.build();
    val manifestClient = new ManifestClient(registry);
    return new Toolbox(registry, manifestClient);
  }

}
