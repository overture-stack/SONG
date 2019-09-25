package bio.overture.song.sdk.config;

public interface RetryConfig {

  Integer getMaxRetries();

  Long getInitialBackoff();

  Double getMultiplier();
}
