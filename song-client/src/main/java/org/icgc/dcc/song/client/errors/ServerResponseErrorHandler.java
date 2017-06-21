package org.icgc.dcc.song.client.errors;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.icgc.dcc.song.core.exceptions.ServerException;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.DefaultResponseErrorHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import static org.icgc.dcc.song.core.exceptions.SongError.parseErrorResponse;

@Slf4j
public class ServerResponseErrorHandler extends DefaultResponseErrorHandler{

  @Override
  public void handleError(ClientHttpResponse clientHttpResponse) throws IOException, ServerException {
    val httpStatusCode = clientHttpResponse.getStatusCode();
    val br = new BufferedReader(new InputStreamReader(clientHttpResponse.getBody()));
    val body = br.lines().collect(Collectors.joining("\n"));
    val songError = parseErrorResponse(httpStatusCode,body);
    throw new ServerException(songError);
  }

}
