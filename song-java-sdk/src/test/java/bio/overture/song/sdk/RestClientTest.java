package bio.overture.song.sdk;

import bio.overture.song.core.model.Sample;
import bio.overture.song.sdk.config.impl.DefaultRestClientConfig;
import bio.overture.song.sdk.web.RestClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.request;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static java.lang.String.format;
import static java.util.Objects.isNull;
import static org.junit.Assert.assertEquals;
import static org.springframework.http.HttpHeaders.ACCEPT;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8;
import static bio.overture.song.core.utils.JsonUtils.mapper;
import static bio.overture.song.core.utils.JsonUtils.objectToTree;
import static bio.overture.song.core.utils.JsonUtils.toJson;
import static bio.overture.song.sdk.Factory.buildRestClient;

@Slf4j
public class RestClientTest {

  @Rule public WireMockRule wireMockRule = new WireMockRule(options().dynamicPort());

  private static final String ACCESS_TOKEN = "someToken";

  private static final JsonNode EXAMPLE_SAMPLE1 =
      objectToTree(
          Sample.builder()
              .sampleId("someSampleId_1")
              .sampleSubmitterId("someSampleSubmitterId_1")
              .sampleType("someSampleType_1")
              .specimenId("someSpecimenId_1")
              .build());

  private static final JsonNode EXAMPLE_SAMPLE2 =
      objectToTree(
          Sample.builder()
              .sampleId("someSampleId_2")
              .sampleSubmitterId("someSampleSubmitterId_2")
              .sampleType("someSampleType_2")
              .specimenId("someSpecimenId_2")
              .build());
  private static final JsonNode EXAMPLE_SAMPLE_ARRAY =
      mapper().createArrayNode().add(EXAMPLE_SAMPLE1).add(EXAMPLE_SAMPLE2);

  private RestClient restClient;

  @Before
  public void beforeTest() {
    val testServerUrl = format("http://localhost:%s", wireMockRule.port());
    val restClientConfig =
        DefaultRestClientConfig.builder()
            .serverUrl(testServerUrl)
            .accessToken(ACCESS_TOKEN)
            .build();

    this.restClient = buildRestClient(restClientConfig);
  }

  @Test
  public void testGet() {
    setupMock(GET, "/something", OK, EXAMPLE_SAMPLE1);
    val actualSample = restClient.get("/something", Sample.class).getBody();
    assertEquals(EXAMPLE_SAMPLE1, objectToTree(actualSample));
  }

  @Test
  public void testPost() {
    setupMock(POST, "/something", OK, EXAMPLE_SAMPLE2);
    val actualSample = restClient.post("/something", EXAMPLE_SAMPLE1, Sample.class).getBody();
    assertEquals(EXAMPLE_SAMPLE2, objectToTree(actualSample));

    val actualSample2 = restClient.post("/something", Sample.class).getBody();
    assertEquals(EXAMPLE_SAMPLE2, objectToTree(actualSample2));
  }

  @Test
  public void testPut() {
    setupMock(PUT, "/something", OK, EXAMPLE_SAMPLE2);
    val actualSample = restClient.put("/something", EXAMPLE_SAMPLE1, Sample.class).getBody();
    assertEquals(EXAMPLE_SAMPLE2, objectToTree(actualSample));

    val actualSample2 = restClient.put("/something", Sample.class).getBody();
    assertEquals(EXAMPLE_SAMPLE2, objectToTree(actualSample2));
  }

  @Test
  public void testGetList() {
    setupMock(GET, "/something", OK, EXAMPLE_SAMPLE_ARRAY);
    val actualSamples = restClient.getList("/something", Sample.class).getBody();
    assertEquals(EXAMPLE_SAMPLE_ARRAY, objectToTree(actualSamples));
  }

  @Test
  public void testPostList() {
    setupMock(POST, "/something", OK, EXAMPLE_SAMPLE_ARRAY);
    val actualSamples = restClient.postList("/something", Sample.class).getBody();
    assertEquals(EXAMPLE_SAMPLE_ARRAY, objectToTree(actualSamples));

    val actualSamples2 = restClient.postList("/something", EXAMPLE_SAMPLE1, Sample.class).getBody();
    assertEquals(EXAMPLE_SAMPLE_ARRAY, objectToTree(actualSamples2));
  }

  @Test
  public void testPutList() {
    setupMock(PUT, "/something", OK, EXAMPLE_SAMPLE_ARRAY);
    val actualSamples = restClient.putList("/something", Sample.class).getBody();
    assertEquals(EXAMPLE_SAMPLE_ARRAY, objectToTree(actualSamples));

    val actualSamples2 = restClient.putList("/something", EXAMPLE_SAMPLE1, Sample.class).getBody();
    assertEquals(EXAMPLE_SAMPLE_ARRAY, objectToTree(actualSamples2));
  }

  /** Note: Request body is ignored */
  private void setupMock(
      @NonNull HttpMethod method,
      @NonNull String endpoint,
      @NonNull HttpStatus responseStatus,
      Object responseBody) {
    wireMockRule.resetAll();

    // Setup request matcher
    val request =
        request(method.name(), urlMatching(endpoint))
            .withHeader(ACCEPT, equalTo(APPLICATION_JSON_UTF8.toString()))
            .withHeader(AUTHORIZATION, WireMock.equalTo("Bearer " + ACCESS_TOKEN));

    // Setup expected response
    val response = aResponse().withStatus(responseStatus.value());
    if (!isNull(responseBody)) {
      response
          .withHeader(CONTENT_TYPE, APPLICATION_JSON_UTF8.toString())
          .withBody(toJson(responseBody));
    }

    wireMockRule.stubFor(request.willReturn(response));
  }
}
