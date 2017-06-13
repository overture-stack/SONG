package org.icgc.dcc.song.server.service.existence;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.assertj.core.util.Sets;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;

import static java.lang.String.format;
import static java.lang.Thread.sleep;
import static lombok.AccessLevel.PRIVATE;
import static org.assertj.core.util.Sets.newLinkedHashSet;
import static org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE;

@Slf4j
@RequiredArgsConstructor(access = PRIVATE)
public class MockDccStorageHandler implements HttpHandler {

  private final boolean result;
  @NonNull private final String expectedToken;
  @NonNull private final HttpStatus errorStatus;
  private final long msUntilError;
  private final LinkedHashSet<Integer> retryNumbersToKill;

  private int retryNum = 0;

  @SneakyThrows
  @Override public void handle(HttpExchange httpExchange) throws IOException {
    val headers = httpExchange.getRequestHeaders();
    val actualToken = headers.get(HttpHeaders.AUTHORIZATION).get(0);
    val shouldErrorOut = retryNumbersToKill.contains(retryNum);
    if (shouldErrorOut) {
      sleep(msUntilError);
      val response =
          format("[%s] -- %s : %s", errorStatus.toString(), errorStatus.value(), errorStatus.getReasonPhrase());
      httpExchange.sendResponseHeaders(errorStatus.value(), response.length());
      val os = httpExchange.getResponseBody();
      os.write(response.getBytes());
      os.close();
    } else {
      if (actualToken.equals(expectedToken)) {
        val response = Boolean.toString(result);
        httpExchange.sendResponseHeaders(HttpStatus.OK.value(), response.length());
        val os = httpExchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
      } else {
        httpExchange.sendResponseHeaders(HttpStatus.UNAUTHORIZED.value(), 0);
      }
    }
    ++retryNum;
  }

  public static MockDccStorageHandler createFailingMockDccStorageHandler(boolean result,
      String expectedToken, HttpStatus errorStatus, long msUntilError) {
    return new MockDccStorageHandler(result, expectedToken, errorStatus, msUntilError, newLinkedHashSet(0));
  }

  public static MockDccStorageHandler createFailingMockDccStorageHandler(boolean result,
      String expectedToken, HttpStatus errorStatus, long msUntilError, List<Integer> retryNumbersToKill) {

    val set = Sets.<Integer>newLinkedHashSet();
    set.addAll(retryNumbersToKill);
    return new MockDccStorageHandler(result, expectedToken, errorStatus, msUntilError, set);
  }

  public static MockDccStorageHandler createNormalMockDccStorageHandler(boolean result,
      String expectedToken) {
    return new MockDccStorageHandler(result, expectedToken, SERVICE_UNAVAILABLE, -1, newLinkedHashSet());
  }
}
