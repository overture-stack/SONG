package bio.overture.song.client.config;

import bio.overture.song.sdk.config.impl.DefaultRetryConfig;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Config {

  @Builder.Default private CustomRestClientConfig client = new CustomRestClientConfig();
  @Builder.Default private DefaultRetryConfig retry = new DefaultRetryConfig();
}
