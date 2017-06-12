package org.icgc.dcc.song.server.service;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.assertj.core.util.Lists;
import org.assertj.core.util.Sets;
import org.icgc.dcc.song.server.config.RetryConfig;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.LinkedHashSet;
import java.util.List;

import static java.lang.String.format;
import static java.lang.Thread.sleep;
import static lombok.AccessLevel.PRIVATE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.util.Sets.newLinkedHashSet;
import static org.icgc.dcc.song.server.service.ExistenceService.createExistenceService;
import static org.icgc.dcc.song.server.service.ExistenceServiceTest.MockDccStorageHandler.createFailingMockDccStorageHandler;
import static org.icgc.dcc.song.server.service.ExistenceServiceTest.MockDccStorageHandler.createNormalMockDccStorageHandler;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@SpringBootTest
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = RetryConfig.class)
@ActiveProfiles({"dev", "secure"})
//@ActiveProfiles(profiles = {"dev","test", "secure"})
public class ExistenceServiceTest {

  @Autowired
  private RetryTemplate retryTemplate;

  private static final int PORT = 8080;
  private static final String HOSTNAME = "localhost";
  private static final String MOCK_SERVER_URL = format("http://%s:%s",HOSTNAME, PORT);
  private static final String OBJECT_ID1 = "qwertyObjectId1";
  private static final String OBJECT_ID2 = "qwertyObjectId2";
  private static final String TOKEN = "12345678";
  private static final int REQUEST_CON_TIMEOUT = 2;
  private static final long MS_UNTIL_KILL = (REQUEST_CON_TIMEOUT + 1)*1000L;

  private HttpServer mockServer;

  @Before
  @SneakyThrows
  public void beforeTest(){
    this.mockServer = HttpServer.create(new InetSocketAddress(PORT), 0);
  }

  @Test
  @SneakyThrows
  public void testIsObjectExists(){

    //Create HttpServer that mocks dcc-storage /upload endpoint
    mockServer.createContext(format("/upload/%s",OBJECT_ID1), createNormalMockDccStorageHandler(true, TOKEN));
    mockServer.createContext(format("/upload/%s",OBJECT_ID2), createNormalMockDccStorageHandler(false, TOKEN ));
    mockServer.start();

    val exi = createExistenceService(retryTemplate,MOCK_SERVER_URL, REQUEST_CON_TIMEOUT);
    assertThat(exi.isObjectExist(TOKEN,OBJECT_ID1)).isTrue();
    assertThat(exi.isObjectExist(TOKEN,OBJECT_ID2)).isFalse();
    val seconds = 1;
    mockServer.stop(seconds);
  }

  @Test
  @SneakyThrows
  public void testExistanceRetryKilledOnce(){
    val handler = createFailingMockDccStorageHandler(true, TOKEN, MS_UNTIL_KILL);
    runFailTest(handler);
  }

  @Test
  @SneakyThrows
  public void testExistanceRetryKilled0123(){
    val killList = Lists.newArrayList(0,1,2,3);
    val handler = createFailingMockDccStorageHandler(true, TOKEN, MS_UNTIL_KILL, killList);
    runFailTest(handler);
  }

  private void runFailTest(MockDccStorageHandler handler){
    //Create HttpServer that mocks dcc-storage /upload endpoint
    mockServer.createContext(format("/upload/%s",OBJECT_ID1), handler);
    mockServer.start();
    val exi = createExistenceService(retryTemplate,MOCK_SERVER_URL, REQUEST_CON_TIMEOUT);
    assertThat(exi.isObjectExist(TOKEN,OBJECT_ID1)).isTrue();
    val seconds = 1;
    mockServer.stop(seconds);
  }

  @RequiredArgsConstructor(access =  PRIVATE)
  @Slf4j
  public static class MockDccStorageHandler implements HttpHandler{

    private final boolean result;
    @NonNull private final String expectedToken;
    @NonNull private final HttpStatus errorStatus;
    private final long msUntilKill;
    private final LinkedHashSet<Integer> retryNumbersToKill;

    private int retryNum = 0;


    @SneakyThrows
    @Override public void handle(HttpExchange httpExchange) throws IOException {
      val headers = httpExchange.getRequestHeaders();
      val actualToken = headers.get(HttpHeaders.AUTHORIZATION).get(0);
      val shouldKill = retryNumbersToKill.contains(retryNum);
      if (shouldKill) {
        sleep(msUntilKill);
      }else {
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
      ++retryNum;
    }

    public static MockDccStorageHandler createFailingMockDccStorageHandler(boolean result,
        String expectedToken, long msUntilKill) {
      return new MockDccStorageHandler(result, expectedToken,INTERNAL_SERVER_ERROR, msUntilKill, newLinkedHashSet(0));
    }

    public static MockDccStorageHandler createFailingMockDccStorageHandler(boolean result,
        String expectedToken, long msUntilKill, List<Integer> retryNumbersToKill) {

      val set = Sets.<Integer>newLinkedHashSet();
      set.addAll(retryNumbersToKill);
      return new MockDccStorageHandler(result, expectedToken, INTERNAL_SERVER_ERROR, msUntilKill, set);
    }

    public static MockDccStorageHandler createNormalMockDccStorageHandler(boolean result,
        String expectedToken) {
      return new MockDccStorageHandler(result, expectedToken, INTERNAL_SERVER_ERROR ,-1, newLinkedHashSet());
    }
  }

}
