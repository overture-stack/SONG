/*
 * Copyright (c) 2018. Ontario Institute for Cancer Research
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package bio.overture.song.server.config;

import lombok.Data;
import lombok.val;
import org.icgc.dcc.id.client.core.IdClient;
import org.icgc.dcc.id.client.http.HttpIdClient;
import org.icgc.dcc.id.client.http.webclient.WebClientConfig;
import org.icgc.dcc.id.client.util.HashIdClient;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
@ConfigurationProperties(prefix = "id")
public class IdConfig {
  private static final int DEFAULT_MAX_RETRIES = 10;
  private static final float DEFAULT_MULTIPLIER = 2;
  private static final int DEFAULT_INITIAL_BACKOFF_SECONDS = 2;

  private String idUrl;
  private String authToken;
  private boolean realIds;
  private boolean persistInMemory = false;
  private int maxRetries = DEFAULT_MAX_RETRIES;
  private float multiplier =  DEFAULT_MULTIPLIER;
  private int initialBackoffSeconds = DEFAULT_INITIAL_BACKOFF_SECONDS;

  @Bean
  public IdClient createIdClient() {
    val idClientConfig = WebClientConfig.builder()
        .serviceUrl(idUrl)
        .authToken(authToken)
        .release("")
        .maxRetries(maxRetries)
        .retryMultiplier(multiplier)
        .waitBeforeRetrySeconds(initialBackoffSeconds)
        .build();

    // [SONG-167]: Temporarily removed cacheId client due to bug in DCC-ID-12: https://github.com/icgc-dcc/dcc-id/issues/12
    return realIds ? new HttpIdClient(idClientConfig) : new HashIdClient(persistInMemory);
//    return realIds ? new CachingIdClient(new HttpIdClient(idUrl, "", authToken)) : new HashIdClient(persistInMemory);
  }

}
