package bio.overture.song.server.utils.contract;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.Value;
import lombok.val;

/**
 * A stand-in for the API key introspection endpoint, built on the JDK HTTP server.
 *
 * <p>WireMock 2.x fails on the multipart/form-data requests the introspector sends, so this server
 * reads the raw request body instead. Each response is chosen by finding a registered API key in
 * the body; requests with no registered key get a 404. Every request is recorded.
 */
public class IntrospectionServer {

  public static final String PATH = "/apikey/check_api_key/";

  private final HttpServer server;
  private final Map<String, StubbedResponse> responsesByApiKey = new ConcurrentHashMap<>();
  private final List<RecordedRequest> recordedRequests = new CopyOnWriteArrayList<>();

  @SneakyThrows
  public IntrospectionServer() {
    this.server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
    server.createContext(
        PATH,
        exchange -> {
          val body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
          recordedRequests.add(
              new RecordedRequest(
                  exchange.getRequestHeaders().getFirst("Authorization"),
                  exchange.getRequestHeaders().getFirst("Content-Type"),
                  body));
          val response =
              responsesByApiKey.entrySet().stream()
                  .filter(entry -> body.contains(entry.getKey()))
                  .map(Map.Entry::getValue)
                  .findFirst()
                  .orElse(new StubbedResponse(404, "{}"));
          val responseBytes = response.getBody().getBytes(StandardCharsets.UTF_8);
          exchange.getResponseHeaders().add("Content-Type", "application/json");
          exchange.sendResponseHeaders(response.getStatus(), responseBytes.length);
          try (val output = exchange.getResponseBody()) {
            output.write(responseBytes);
          } catch (IOException exception) {
            // The client closed the connection; nothing more to send.
          }
        });
  }

  public void start() {
    server.start();
  }

  public void stop() {
    server.stop(0);
  }

  public String url() {
    return "http://localhost:" + server.getAddress().getPort() + PATH;
  }

  /**
   * Answers every request whose body contains {@code apiKey} with {@code status} and {@code body}.
   */
  public void stub(@NonNull String apiKey, int status, @NonNull String body) {
    responsesByApiKey.put(apiKey, new StubbedResponse(status, body));
  }

  public List<RecordedRequest> getRecordedRequests() {
    return List.copyOf(recordedRequests);
  }

  @Value
  private static class StubbedResponse {
    int status;
    String body;
  }

  @Value
  public static class RecordedRequest {
    String authorization;
    String contentType;
    String body;
  }
}
