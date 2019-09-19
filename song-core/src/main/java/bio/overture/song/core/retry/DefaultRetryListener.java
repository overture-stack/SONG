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
package bio.overture.song.core.retry;

import static org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE;

import java.net.ConnectException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.listener.RetryListenerSupport;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

@Slf4j
@RequiredArgsConstructor
public class DefaultRetryListener extends RetryListenerSupport {

  private final boolean retryOnAllErrors;

  @Override
  public <T, E extends Throwable> void onError(
      RetryContext context, RetryCallback<T, E> callback, Throwable throwable) {
    if (retryOnAllErrors) {
      log.info("Retrying after detecting error: {}", throwable.getMessage());
    } else {
      if (isClientException(throwable)) {
        log.debug(
            "HTTP client related exception detected. Do nothing. The downstream will do the further processing.");
        return;
      }

      if (!(isConnectionTimeout(throwable) || isServiceUnavailable(throwable))) {
        log.info(
            "Detected a connection exception, but it's not the connection timeoutMs or 503 Service Unavailable. "
                + "Do not retry.");
        context.setExhaustedOnly();
      }
    }
  }

  private static boolean isConnectionTimeout(Throwable throwable) {
    if (!(throwable instanceof ResourceAccessException)) {
      return false;
    }

    val cause = throwable.getCause();
    if (cause instanceof ConnectException && cause.getMessage().equals("Operation timed out")) {
      log.debug("Operation timed out. Retrying...");
      return true;
    }

    return false;
  }

  private static boolean isServiceUnavailable(Throwable throwable) {
    if (!(throwable instanceof HttpServerErrorException)) {
      return false;
    }

    val e = (HttpServerErrorException) throwable;
    if (e.getStatusCode() == SERVICE_UNAVAILABLE) {
      log.warn("Service unavailable. Retrying...");
      return true;
    }

    return false;
  }

  private static boolean isClientException(Throwable throwable) {
    return throwable instanceof HttpClientErrorException;
  }
}
