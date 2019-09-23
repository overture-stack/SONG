package bio.overture.song.core.web;

import static bio.overture.song.core.utils.CollectionUtils.isArrayBlank;
import static bio.overture.song.core.utils.CollectionUtils.isCollectionBlank;
import static bio.overture.song.core.utils.Joiners.PATH;
import static bio.overture.song.core.utils.JsonUtils.toJson;
import static bio.overture.song.core.web.QueryParam.createQueryParam;
import static com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static java.lang.String.format;
import static java.util.Objects.isNull;
import static java.util.stream.Collectors.toUnmodifiableSet;
import static org.apache.commons.lang.StringUtils.isBlank;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

@Slf4j
@RequiredArgsConstructor
public abstract class AbstractWebResource<W extends AbstractWebResource<W>> implements WebResource {
  private static final ObjectMapper REGULAR_MAPPER = new ObjectMapper();
  private static final ObjectMapper PRETTY_MAPPER = new ObjectMapper();
  private static final Joiner AMPERSAND = Joiner.on("&");

  static {
    PRETTY_MAPPER.enable(INDENT_OUTPUT);
  }

  @NonNull private final String serverUrl;

  private String endpoint;
  private Set<QueryParam> queryParams = newHashSet();
  private Object body;
  private HttpHeaders headers;
  private boolean enableLogging = false;
  private boolean pretty = false;

  private ResponseOption createResponseOption(ResponseEntity<String> responseEntity) {
    return new ResponseOption(responseEntity);
  }

  @SuppressWarnings("unchecked")
  private W thisInstance() {
    return (W) this;
  }

  public W endpoint(String formattedEndpoint, Object... args) {
    this.endpoint = format(formattedEndpoint, args);
    // ensure there is only one leading slash
    if (isBlank(serverUrl)) {
      this.endpoint = this.endpoint.replaceAll("^\\/*", "/");
    }
    return thisInstance();
  }

  public W body(Object body) {
    this.body = body;
    return thisInstance();
  }

  public W headers(HttpHeaders httpHeaders) {
    this.headers = httpHeaders;
    return thisInstance();
  }

  public W logging() {
    return configLogging(true, false);
  }

  public W prettyLogging() {
    return configLogging(true, true);
  }

  public W optionalQueryParamCollection(String key, Collection values) {
    if (!isCollectionBlank(values)) {
      return queryParam(key, values);
    }
    return thisInstance();
  }

  public W optionalQuerySingleParam(String key, Object value) {
    if (!isNull(value)) {
      return querySingleParam(key, value);
    }
    return thisInstance();
  }

  public W querySingleParam(String key, Object value) {
    return queryParam(key, ImmutableList.of(value));
  }

  public W optionalQueryParamArray(String key, Object[] values) {
    if (!isArrayBlank(values)) {
      return optionalQueryParamCollection(key, newArrayList(values));
    }
    return thisInstance();
  }

  public W optionalQueryParamMulti(String key, Object... values) {
    if (!isArrayBlank(values)) {
      return optionalQueryParamCollection(key, newArrayList(values));
    }
    return thisInstance();
  }

  public W queryParam(String key, Collection values) {
    queryParams.add(createQueryParam(key, values));
    return thisInstance();
  }

  private W configLogging(boolean enable, boolean pretty) {
    this.enableLogging = enable;
    this.pretty = pretty;
    return thisInstance();
  }

  public ResponseEntity<String> get() {
    return doRequest(null, HttpMethod.GET);
  }

  public ResponseEntity<String> put() {
    return doRequest(this.body, HttpMethod.PUT);
  }

  public ResponseEntity<String> post() {
    return doRequest(this.body, HttpMethod.POST);
  }

  public ResponseEntity<String> delete() {
    return doRequest(null, HttpMethod.DELETE);
  }

  public ResponseOption deleteAnd() {
    return createResponseOption(delete());
  }

  public ResponseOption getAnd() {
    return createResponseOption(get());
  }

  public ResponseOption putAnd() {
    return createResponseOption(put());
  }

  public ResponseOption postAnd() {
    return createResponseOption(post());
  }

  private Optional<String> getQuery() {
    val queryStrings = queryParams.stream().map(QueryParam::toString).collect(toUnmodifiableSet());
    return queryStrings.isEmpty() ? Optional.empty() : Optional.of(AMPERSAND.join(queryStrings));
  }

  private String getUrl() {
    val prefix = isBlank(serverUrl) ? endpoint : PATH.join(serverUrl, endpoint);
    return prefix + getQuery().map(x -> "?" + x).orElse("");
  }

  private ResponseEntity<String> doRequest(Object objectBody, HttpMethod httpMethod) {
    return doRequest(toJson(objectBody), httpMethod);
  }

  @SneakyThrows
  private ResponseEntity<String> doRequest(String stringBody, HttpMethod httpMethod) {
    logRequest(enableLogging, pretty, httpMethod, getUrl(), stringBody);
    val response = executeRequest(httpMethod, getUrl(), headers, stringBody);
    logResponse(enableLogging, pretty, response);
    return response;
  }

  @SneakyThrows
  private static void logRequest(
      boolean enable, boolean pretty, HttpMethod httpMethod, String url, Object body) {
    if (enable) {
      if (isNull(body)) {
        log.info("[REQUEST] {} {}", httpMethod, url);
      } else {
        if (pretty) {
          log.info(
              "[REQUEST] {} {} < \n{}", httpMethod, url, PRETTY_MAPPER.writeValueAsString(body));
        } else {
          log.info(
              "[REQUEST] {} {} < {}", httpMethod, url, REGULAR_MAPPER.writeValueAsString(body));
        }
      }
    }
  }

  @SneakyThrows
  private static <T> void logResponse(boolean enable, boolean pretty, ResponseEntity<T> response) {
    if (enable) {
      val output =
          CleanResponse.builder()
              .body(response.hasBody() ? response.getBody() : null)
              .statusCodeName(response.getStatusCode().name())
              .statusCodeValue(response.getStatusCodeValue())
              .build();
      if (pretty) {
        log.info("[RESPONSE] > \n{}", PRETTY_MAPPER.writeValueAsString(output));
      } else {
        log.info("[RESPONSE] > {}", REGULAR_MAPPER.writeValueAsString(output));
      }
    }
  }
}
