package org.icgc.dcc.song.server.service;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.net.InetSocketAddress;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;

public class ExistenceServiceTest {

  @Test
  @SneakyThrows
  public void testIsObjectExists(){
    val hostname = "localhost";
    val port = 8080;
    val mockServerUrl = format("http://%s:%s",hostname, port);

    val objectId1 = "qwertyObjectId1";
    val objectId2 = "qwertyObjectId2";
    val token = "12345678";

    //Create HttpServer that mocks dcc-storage /upload endpoint
    val mockServer = HttpServer.create(new InetSocketAddress(port), 0);
    mockServer.createContext(format("/upload/%s",objectId1),new MockDccStorageHandler(true, token));
    mockServer.createContext(format("/upload/%s",objectId2),new MockDccStorageHandler(false, token));
    mockServer.start();

    val exi = ExistenceService.createExistenceService(mockServerUrl);
    assertThat(exi.isObjectExist(token,objectId1)).isTrue();
    assertThat(exi.isObjectExist(token,objectId2)).isFalse();
    val seconds = 1;
    mockServer.stop(seconds);
  }

  @RequiredArgsConstructor
  @Slf4j
  public static class MockDccStorageHandler implements HttpHandler{

    private final boolean result;
    @NonNull private final String expectedToken;

    @Override public void handle(HttpExchange httpExchange) throws IOException {
      val headers = httpExchange.getRequestHeaders();
      val actualToken = headers.get(HttpHeaders.AUTHORIZATION).get(0);
      if (actualToken.equals(expectedToken)){
        val response = Boolean.toString(result);
        httpExchange.sendResponseHeaders(HttpStatus.OK.value(), response.length());
        val os = httpExchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
      } else {
        httpExchange.sendResponseHeaders(HttpStatus.UNAUTHORIZED.value(), 0);
      }
    }

  }


}
