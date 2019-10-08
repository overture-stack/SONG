/*
 * Copyright (c) 2019. Ontario Institute for Cancer Research
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package bio.overture.song.sdk;

import static bio.overture.song.core.utils.JsonUtils.mapper;
import static bio.overture.song.core.utils.JsonUtils.objectToTree;
import static bio.overture.song.core.utils.JsonUtils.toJson;
import static bio.overture.song.sdk.Factory.buildRestClient;
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

import bio.overture.song.core.model.PageDTO;
import bio.overture.song.core.model.Sample;
import bio.overture.song.sdk.config.impl.DefaultRestClientConfig;
import bio.overture.song.sdk.web.RestClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import java.util.List;
import java.util.function.Supplier;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Slf4j
public class RestClientTest {

  /** Constants */
  private static final String ACCESS_TOKEN = "someToken";

  private static final JsonNode EXAMPLE_BODY = mapper().createObjectNode();
  private static final Sample EXAMPLE_SAMPLE1 =
      Sample.builder()
          .sampleId("someSampleId_1")
          .sampleSubmitterId("someSampleSubmitterId_1")
          .sampleType("someSampleType_1")
          .specimenId("someSpecimenId_1")
          .build();
  private static final JsonNode EXAMPLE_SAMPLE1_JSON = objectToTree(EXAMPLE_SAMPLE1);
  private static final Sample EXAMPLE_SAMPLE2 =
      Sample.builder()
          .sampleId("someSampleId_2")
          .sampleSubmitterId("someSampleSubmitterId_2")
          .sampleType("someSampleType_2")
          .specimenId("someSpecimenId_2")
          .build();
  private static final JsonNode EXAMPLE_SAMPLE2_JSON = objectToTree(EXAMPLE_SAMPLE2);
  private static final List<Sample> EXAMPLE_SAMPLES = List.of(EXAMPLE_SAMPLE1, EXAMPLE_SAMPLE2);
  private static final JsonNode EXAMPLE_SAMPLE_ARRAYNODE =
      mapper().createArrayNode().add(EXAMPLE_SAMPLE1_JSON).add(EXAMPLE_SAMPLE2_JSON);
  private static final int OFFSET = 4;
  private static final int LIMIT = 19;
  private static final int COUNT = 7;
  private static final PageDTO<Sample> EXAMPLE_SAMPLE_PAGE =
      PageDTO.<Sample>builder()
          .count(COUNT)
          .limit(LIMIT)
          .offset(OFFSET)
          .resultSet(EXAMPLE_SAMPLES)
          .build();
  private static final JsonNode EXAMPLE_SAMPLE_PAGE_JSON = objectToTree(EXAMPLE_SAMPLE_PAGE);
  private static final String EXAMPLE_ENDPOINT = "/something";

  @Rule public WireMockRule wireMockRule = new WireMockRule(options().dynamicPort());
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
    runSingleTest(GET, () -> restClient.get(EXAMPLE_ENDPOINT, Sample.class));
  }

  @Test
  public void testPost() {
    runSingleTest(POST, () -> restClient.post(EXAMPLE_ENDPOINT, EXAMPLE_BODY, Sample.class));
    runSingleTest(POST, () -> restClient.post(EXAMPLE_ENDPOINT, Sample.class));
  }

  @Test
  public void testPut() {
    runSingleTest(PUT, () -> restClient.put(EXAMPLE_ENDPOINT, EXAMPLE_BODY, Sample.class));
    runSingleTest(PUT, () -> restClient.put(EXAMPLE_ENDPOINT, Sample.class));
  }

  @Test
  public void testGetList() {
    runListTest(GET, () -> restClient.getList(EXAMPLE_ENDPOINT, Sample.class));
  }

  @Test
  public void testPostList() {
    runListTest(POST, () -> restClient.postList(EXAMPLE_ENDPOINT, Sample.class));
    runListTest(POST, () -> restClient.postList(EXAMPLE_ENDPOINT, EXAMPLE_BODY, Sample.class));
  }

  @Test
  public void testPutList() {
    runListTest(PUT, () -> restClient.putList(EXAMPLE_ENDPOINT, Sample.class));
    runListTest(PUT, () -> restClient.putList(EXAMPLE_ENDPOINT, EXAMPLE_BODY, Sample.class));
  }

  @Test
  public void testGetPage() {
    runPageTest(GET, () -> restClient.getPage(EXAMPLE_ENDPOINT, Sample.class));
  }

  @Test
  public void testPostPage() {
    runPageTest(POST, () -> restClient.postPage(EXAMPLE_ENDPOINT, Sample.class));
    runPageTest(POST, () -> restClient.postPage(EXAMPLE_ENDPOINT, EXAMPLE_BODY, Sample.class));
  }

  @Test
  public void testPutPage() {
    runPageTest(PUT, () -> restClient.putPage(EXAMPLE_ENDPOINT, Sample.class));
    runPageTest(PUT, () -> restClient.putPage(EXAMPLE_ENDPOINT, EXAMPLE_BODY, Sample.class));
  }

  private void runPageTest(
      HttpMethod httpMethod, Supplier<ResponseEntity<PageDTO<Sample>>> pageMethodCallback) {
    setupMock(httpMethod, EXAMPLE_ENDPOINT, OK, EXAMPLE_SAMPLE_PAGE_JSON);
    val actualSamples = pageMethodCallback.get().getBody();
    assertEquals(EXAMPLE_SAMPLE_PAGE, actualSamples);
  }

  private void runListTest(
      HttpMethod httpMethod, Supplier<ResponseEntity<List<Sample>>> listMethodCallback) {
    setupMock(httpMethod, EXAMPLE_ENDPOINT, OK, EXAMPLE_SAMPLE_ARRAYNODE);
    val actualSamples = listMethodCallback.get().getBody();
    assertEquals(EXAMPLE_SAMPLES, actualSamples);
  }

  private void runSingleTest(
      HttpMethod httpMethod, Supplier<ResponseEntity<Sample>> singleMethodCallback) {
    setupMock(httpMethod, EXAMPLE_ENDPOINT, OK, EXAMPLE_SAMPLE1_JSON);
    val actualSample = singleMethodCallback.get().getBody();
    assertEquals(EXAMPLE_SAMPLE1, actualSample);
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
