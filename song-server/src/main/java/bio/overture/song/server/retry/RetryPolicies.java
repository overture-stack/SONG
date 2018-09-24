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
package bio.overture.song.server.retry;

import com.google.common.collect.ImmutableMap;
import lombok.NoArgsConstructor;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.util.Map;

import static java.lang.Boolean.TRUE;
import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public final class RetryPolicies {

  /**
   * Returns a map with exceptions that should be retried by the Spring Retry Framework.
   * 
   * <ul>
   * <li><b>ResourceAccessException</b> - to retry Connection Timeout</li>
   * <li><b>HttpServerErrorException</b> - to retry 503 Service Unavailable</li>
   * </ul>
   */
  public static Map<Class<? extends Throwable>, Boolean> getRetryableExceptions() {
    return ImmutableMap.of(
        ResourceAccessException.class, TRUE,
        HttpServerErrorException.class, TRUE);
  }

}
