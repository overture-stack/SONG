package org.icgc.dcc.sodalite.server.config;

import java.util.concurrent.Executor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurerSupport;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class AsyncProcessingConfig extends AsyncConfigurerSupport {

  @Value("${validation.threads.core}")
  private int corePoolSize;

  @Value("${validation.threads.max}")
  private int maxPoolSize;

  @Value("${validation.queue.capacity}")
  private int queueCapacity;

  @Override
  public Executor getAsyncExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(corePoolSize);
    executor.setMaxPoolSize(maxPoolSize);
    executor.setQueueCapacity(queueCapacity);
    executor.setThreadNamePrefix("SodaliteMessageValidation-");
    executor.initialize();
    return executor;
  }
}
