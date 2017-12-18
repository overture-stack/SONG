package org.icgc.dcc.song.client.benchmark.rest;

import lombok.val;
import org.icgc.dcc.song.client.cli.Status;
import org.icgc.dcc.song.client.errors.ServerResponseErrorHandler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.function.Function;

public class StatusRestClient extends AbstractRestClient<Status>{

  private final RestTemplate restTemplate;

  public StatusRestClient() {
    this.restTemplate = new RestTemplate();
    this.restTemplate.setErrorHandler(new ServerResponseErrorHandler());
  }

  @Override
  protected <T> Status tryRequest(Function<RestTemplate, ResponseEntity<T>> restTemplateFunction) {
    Status status = new Status();
    val response = restTemplateFunction.apply(restTemplate);
    if (response.getStatusCode() == HttpStatus.OK) {
      if (response.getBody() == null) {
        status.err("[SONG_CLIENT_ERROR]: Null response from server: %s", response.toString());
      } else {
        status.output(response.getBody().toString());
      }
    } else {
      status.err("[%s]: %s",response.getStatusCode().value(),response.toString());
    }
    return status;
  }
}
