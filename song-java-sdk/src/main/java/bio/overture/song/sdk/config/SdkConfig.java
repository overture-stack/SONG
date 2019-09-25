package bio.overture.song.sdk.config;

import bio.overture.song.sdk.config.impl.DefaultRestClientConfig;
import bio.overture.song.sdk.config.impl.DefaultRetryConfig;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public final class SdkConfig {

  @Builder.Default private RestClientConfig client = new DefaultRestClientConfig();
  @Builder.Default private RetryConfig retry = new DefaultRetryConfig();
}
