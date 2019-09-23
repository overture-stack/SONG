package bio.overture.song.sdk.config;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.Properties;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Config {
  private ClientConfig client = new ClientConfig();
  private RetryConfig retry = new RetryConfig();

  private Properties otherProperties = new Properties();

  @JsonAnySetter
  public void setOtherProperty(String key, Object value) {
    otherProperties.put(key, value);
  }

  @JsonAnyGetter
  public Properties getOtherProperties() {
    return otherProperties;
  }
}
