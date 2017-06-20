package org.icgc.dcc.song.client.errors;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.DefaultResponseErrorHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

/**
 * - testing client
 * - server side exception handling/wrapping
 * -
 */
@Slf4j
public class ServerResponseErrorHandler extends DefaultResponseErrorHandler{


  @Override public void handleError(ClientHttpResponse clientHttpResponse) throws IOException {
    val statusCode = clientHttpResponse.getStatusCode();
    val serverExceptionBuilder = ServerException.builder()
        .status(statusCode)
        .message(clientHttpResponse.getBody().toString());

    val br = new BufferedReader(new InputStreamReader(clientHttpResponse.getBody()));
    val body = br.lines().collect(Collectors.joining("\n"));

    System.out.println(body);

//    switch(statusCode) {
//    case UNAUTHORIZED:
//      throw serverExceptionBuilder
//          .id(TOKEN_UNAUTHORIZED.getId())
//          .message("The input token is not authorized, or not specified")
//          .build();
//    case CONFLICT:
//      throw serverExceptionBuilder
//          .id(SAVE_CONFLICT.getId())
//          .message("There was a conflict while saving")
//          .build();
//    default:
//      throw serverExceptionBuilder
//          .id(Integer.toString(clientHttpResponse.getStatusCode().value()))
//          .build();
//    }
  }

}
