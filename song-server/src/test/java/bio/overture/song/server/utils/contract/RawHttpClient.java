package bio.overture.song.server.utils.contract;

import static java.util.Objects.isNull;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;

/**
 * Sends HTTP requests to a running server and returns the response without any conversion, so tests
 * see the status, headers and body exactly as a real client would.
 *
 * <p>Uses the JDK HTTP client rather than a Spring client so the request bytes and the response
 * handling do not change when Spring is upgraded. Error statuses are returned, never thrown.
 */
@RequiredArgsConstructor
public class RawHttpClient {

  public static final String APPLICATION_JSON = "application/json";

  // HTTP/1.1 is forced so the client never attempts an h2c upgrade, which Tomcat may answer
  // differently from a plain request.
  private static final HttpClient HTTP_CLIENT =
      HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build();

  /** Server root, such as {@code http://localhost:12345}, with no trailing slash. */
  @NonNull private final String baseUrl;

  public RawResponse get(@NonNull String path) {
    return send("GET", path, null, null);
  }

  public RawResponse delete(@NonNull String path) {
    return send("DELETE", path, null, null);
  }

  public RawResponse post(@NonNull String path, @NonNull String jsonBody) {
    return send("POST", path, APPLICATION_JSON, jsonBody);
  }

  public RawResponse put(@NonNull String path, @NonNull String jsonBody) {
    return send("PUT", path, APPLICATION_JSON, jsonBody);
  }

  public RawResponse patch(@NonNull String path, @NonNull String jsonBody) {
    return send("PATCH", path, APPLICATION_JSON, jsonBody);
  }

  /**
   * Sends a request with {@code Accept: application/json}.
   *
   * @param path appended to the base URL as is, including any query string
   * @param contentType sent as the Content-Type header; omitted when null
   * @param body sent as UTF-8 text; the request has no body when null
   */
  public RawResponse send(
      @NonNull String method, @NonNull String path, String contentType, String body) {
    return send(method, path, contentType, body, APPLICATION_JSON);
  }

  /**
   * Sends a request with the given Accept header.
   *
   * @param path appended to the base URL as is, including any query string
   * @param contentType sent as the Content-Type header; omitted when null
   * @param body sent as UTF-8 text; the request has no body when null
   */
  @SneakyThrows
  public RawResponse send(
      @NonNull String method,
      @NonNull String path,
      String contentType,
      String body,
      @NonNull String accept) {
    val requestBuilder =
        HttpRequest.newBuilder(URI.create(baseUrl + path))
            .header("Accept", accept)
            .method(
                method,
                isNull(body)
                    ? BodyPublishers.noBody()
                    : BodyPublishers.ofString(body, StandardCharsets.UTF_8));
    if (!isNull(contentType)) {
      requestBuilder.header("Content-Type", contentType);
    }
    val response =
        HTTP_CLIENT.send(requestBuilder.build(), BodyHandlers.ofString(StandardCharsets.UTF_8));
    return new RawResponse(
        response.statusCode(),
        response.headers().firstValue("Content-Type").orElse(null),
        response.body());
  }
}
