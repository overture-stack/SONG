package bio.overture.song.client.config;

import bio.overture.song.sdk.config.RestClientConfig;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomRestClientConfig implements RestClientConfig {

  private String studyId;
  private String accessToken;
  private String serverUrl;
  private String programName;
  private boolean debug;
}
