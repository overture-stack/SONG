package org.icgc.dcc.song.server.service.existence;

import com.sun.net.httpserver.HttpServer;
import lombok.SneakyThrows;
import lombok.Synchronized;
import lombok.val;
import org.icgc.dcc.song.server.config.RetryConfig;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.HttpServerErrorException;

import java.net.InetSocketAddress;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.util.Lists.newArrayList;
import static org.icgc.dcc.song.server.service.ExistenceService.createExistenceService;
import static org.icgc.dcc.song.server.service.existence.MockDccStorageHandler.createFailingMockDccStorageHandler;
import static org.icgc.dcc.song.server.service.existence.MockDccStorageHandler.createNormalMockDccStorageHandler;
import static org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE;

@SpringBootTest
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = RetryConfig.class)
@ActiveProfiles("dev")
public class ExistenceServiceTest {
  private static final String HOSTNAME = "localhost";
  private static final String OBJECT_ID1 = "qwertyObjectId1";
  private static final String OBJECT_ID2 = "qwertyObjectId2";
  private static final String TOKEN = "12345678";
  private static final int REQUEST_CON_TIMEOUT = 200;
  private static final long MS_UNTIL_ERROR =  REQUEST_CON_TIMEOUT + 100L;

  // We'll give each test it's own port number, in case they're running in parallel
  private static int freePort=9876;

  String mockServerUrl;

  @Autowired
  private RetryTemplate retryTemplate;

  private CountingRetryListener countingRetryListener;
  private HttpServer mockServer;

  @Before
  @SneakyThrows
  public void beforeTest() {
    val port = getPort();
    mockServerUrl = format("http://%s:%s", HOSTNAME, port);
    System.err.printf("Creating mock server on port %d\n", port);
    this.mockServer = HttpServer.create(new InetSocketAddress(port), 0);
    this.countingRetryListener = new CountingRetryListener();
    this.retryTemplate.registerListener(countingRetryListener);
  }

  @Synchronized
  public int getPort() {
    freePort +=1;
    return freePort;
  }

  @Test
  @SneakyThrows
  public void testNormalOperation() {
    //Create HttpServer that mocks dcc-storage /upload endpoint
    mockServer.createContext(format("/upload/%s", OBJECT_ID1), createNormalMockDccStorageHandler(true, TOKEN));
    mockServer.createContext(format("/upload/%s", OBJECT_ID2), createNormalMockDccStorageHandler(false, TOKEN));
    mockServer.start();
    val exi = createExistenceService(retryTemplate, mockServerUrl);
    assertThat(exi.isObjectExist(TOKEN, OBJECT_ID1)).isTrue();
    assertThat(exi.isObjectExist(TOKEN, OBJECT_ID2)).isFalse();
    assertThat(countingRetryListener.getErrorCount()).isEqualTo(0);
    val seconds = 1;
    mockServer.stop(seconds);
  }

  @Test
  @SneakyThrows
  @Ignore("Needs to be fixed, but not critical for now")
  public void testTimeout() {
    val handler = createFailingMockDccStorageHandler(true, TOKEN, SERVICE_UNAVAILABLE, MS_UNTIL_ERROR);
    runFailTest(handler);
    assertThat(countingRetryListener.getErrorCount()).isEqualTo(1);
  }

  @Test
  @SneakyThrows
  public void test1RetryOnServiceUnavailable() {
    val handler = createFailingMockDccStorageHandler(true, TOKEN, SERVICE_UNAVAILABLE, 0);
    runFailTest(handler);
    assertThat(countingRetryListener.getErrorCount()).isEqualTo(1);
  }

  @Test
  @SneakyThrows
  public void testMaxRetriesOnServiceUnavailable() {
    val killList = newArrayList(0, 1, 2, 3);
    val handler = createFailingMockDccStorageHandler(true, TOKEN, SERVICE_UNAVAILABLE, 0, killList);
    runFailTest(handler);
    assertThat(countingRetryListener.getErrorCount()).isEqualTo(4);
  }

  @Test
  @SneakyThrows
  public void testFailingAllRetriesOnServiceUnavailable() {
    val killList = newArrayList(0, 1, 2, 3, 4);
    val handler = createFailingMockDccStorageHandler(true, TOKEN, SERVICE_UNAVAILABLE, 0, killList);
    val thrown = catchThrowable(() -> runFailTest(handler));
    assertThat(thrown)
        .isInstanceOf(HttpServerErrorException.class)
        .hasMessageContaining(Integer.toString(SERVICE_UNAVAILABLE.value()));
    assertThat(countingRetryListener.getErrorCount()).isEqualTo(5);
  }

  private void runFailTest(MockDccStorageHandler handler) {
    //Create HttpServer that mocks dcc-storage /upload endpoint
    mockServer.createContext(format("/upload/%s", OBJECT_ID1), handler);
    mockServer.start();
    val exi = createExistenceService(retryTemplate, mockServerUrl);
    assertThat(exi.isObjectExist(TOKEN, OBJECT_ID1)).isTrue();
    val seconds = 1;
    mockServer.stop(seconds);
  }

}
