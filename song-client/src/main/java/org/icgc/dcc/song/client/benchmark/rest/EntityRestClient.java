package org.icgc.dcc.song.client.benchmark.rest;

import lombok.val;
import org.icgc.dcc.song.client.errors.ServerResponseErrorHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.function.Function;

import static org.springframework.http.ResponseEntity.status;

public class EntityRestClient extends AbstractRestClient<ResponseEntity<String>>{

  private final RestTemplate restTemplate;

  public EntityRestClient() {
    this.restTemplate = new RestTemplate();
    this.restTemplate.setErrorHandler(new ServerResponseErrorHandler());
  }

  @Override
  protected <T> ResponseEntity<String> tryRequest(
      Function<RestTemplate, ResponseEntity<T>> restTemplateFunction) {
    val response = restTemplateFunction.apply(restTemplate);
    return status(response.getStatusCode()).body(response.getBody().toString());
  }

}
