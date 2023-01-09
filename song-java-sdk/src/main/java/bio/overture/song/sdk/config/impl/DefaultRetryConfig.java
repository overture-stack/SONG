/*
 * Copyright (c) 2019. Ontario Institute for Cancer Research
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
package bio.overture.song.sdk.config.impl;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.springframework.retry.backoff.ExponentialBackOffPolicy.DEFAULT_MULTIPLIER;

import bio.overture.song.sdk.config.RetryConfig;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DefaultRetryConfig implements RetryConfig {

  private static final int DEFAULT_MAX_RETRIES = 5;
  private static final long DEFAULT_INITIAL_BACKOFF_INTERVAL = SECONDS.toMillis(15L);

  @Builder.Default private Integer maxRetries = DEFAULT_MAX_RETRIES;

  @Builder.Default private Long initialBackoff = DEFAULT_INITIAL_BACKOFF_INTERVAL;

  @Builder.Default private Double multiplier = DEFAULT_MULTIPLIER;
}
