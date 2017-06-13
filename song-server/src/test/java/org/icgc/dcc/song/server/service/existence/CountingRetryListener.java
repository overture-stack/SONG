package org.icgc.dcc.song.server.service.existence;

import lombok.Getter;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.listener.RetryListenerSupport;

public class CountingRetryListener extends RetryListenerSupport {

  @Getter
  private int errorCount = 0;

  @Override public <T, E extends Throwable> void onError(RetryContext retryContext,
      RetryCallback<T, E> retryCallback,
      Throwable throwable) {
    errorCount++;
  }

}
