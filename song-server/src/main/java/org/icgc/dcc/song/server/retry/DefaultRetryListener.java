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
package org.icgc.dcc.song.server.retry;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.listener.RetryListenerSupport;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.net.ConnectException;

import static lombok.AccessLevel.PRIVATE;
import static org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE;

@Slf4j
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = PRIVATE)
public class DefaultRetryListener extends RetryListenerSupport {

  ClientRetryListener clientRetryListener;

  @Override
  public <T, E extends Throwable> void onError(RetryContext context, RetryCallback<T, E> callback, Throwable throwable) {
    if (clientRetryListener != null) {
      clientRetryListener.onError(context, callback, throwable);
      if (!clientRetryListener.isRetry()) {
        log.debug("The ClientRetryListener requested to disable retries. Skipping the default retry processing...");
        return;
      }
    }

    if (isClientException(throwable)) {
      log.debug("HTTP client related exception detected. Do nothing. The downstream will do the further processing.");
      return;
    }

    if (!(isConnectionTimeout(throwable) || isServiceUnavailable(throwable))) {
      log.info("Detected a connection exception, but it's not the connection timeoutMs or 503 Service Unavailabe. "
          + "Do not retry.");
      context.setExhaustedOnly();
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
    if (throwable instanceof HttpClientErrorException) {
      return true;
    }

    return false;
  }

}
