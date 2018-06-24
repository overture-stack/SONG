/*
 * Copyright (c) 2018. Ontario Institute for Cancer Research
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

package org.icgc.dcc.song.server.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.icgc.dcc.song.core.utils.JsonUtils;
import org.icgc.dcc.song.core.utils.RandomGenerator;
import org.icgc.dcc.song.server.model.ScoreObject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.INVALID_SCORE_DOWNLOAD_RESPONSE;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.STORAGE_OBJECT_NOT_FOUND;
import static org.icgc.dcc.song.core.testing.SongErrorAssertions.assertSongError;
import static org.icgc.dcc.song.core.utils.JsonUtils.toJson;
import static org.icgc.dcc.song.core.utils.RandomGenerator.createRandomGenerator;
import static org.icgc.dcc.song.server.service.ScoreService.createScoreService;
import static org.springframework.http.HttpStatus.OK;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles("dev")
public class ScoreServiceTest {

  private static final String DEFAULT_ACCESS_TOKEN = "anyToken";

  @Autowired
  RetryTemplate retryTemplate;

  @Autowired
  ValidationService validationService;

  private ScoreService scoreService;
  private final RandomGenerator randomGenerator = createRandomGenerator(ScoreServiceTest.class.getSimpleName());

  @Rule
  public WireMockRule wireMockRule = new WireMockRule(options().dynamicPort());

  @Before
  public void beforeTest(){
    val testStorageUrl = format("http://localhost:%s", wireMockRule.port());
    this.scoreService = createScoreService(new RestTemplate(), retryTemplate, testStorageUrl, validationService);
  }

  @Test
  public void testObjectExistence(){
    val expectedScoreObject =  ScoreObject.builder()
        .objectId(randomGenerator.generateRandomUUIDAsString())
        .fileMd5sum(randomGenerator.generateRandomMD5())
        .fileSize((long)randomGenerator.generateRandomIntRange(1,100000))
        .build();
    val scoreResponse = toNode(convertToObjectSpec(expectedScoreObject));

    // Test nonexisting
    val nonExistingConfig = ScoreBehaviourConfig.builder()
        .objectExists(false)
        .expectedScoreResponse(scoreResponse)
        .build();
    setupScoreMockService(expectedScoreObject.getObjectId(), nonExistingConfig);
    val result = scoreService.isObjectExist(DEFAULT_ACCESS_TOKEN, expectedScoreObject.getObjectId());
    assertThat(result).isFalse();

    // Test existing
    val existingConfig = ScoreBehaviourConfig.builder()
        .objectExists(true)
        .expectedScoreResponse(scoreResponse)
        .build();
    setupScoreMockService(expectedScoreObject.getObjectId(), existingConfig);
    val result2 = scoreService.isObjectExist(DEFAULT_ACCESS_TOKEN, expectedScoreObject.getObjectId());
    assertThat(result2).isTrue();
  }

  @Test
  public void testNonExistingDownloadScoreObject_noMd5(){
    runDownloadScoreObjectTest(false, false);
  }

  @Test
  public void testNonExistingDownloadScoreObject_withMd5(){
    runDownloadScoreObjectTest(false, true);
  }

  @Test
  public void testExistingDownloadScoreObject_noMd5(){
    runDownloadScoreObjectTest(true, false);
  }

  @Test
  public void testExistingDownloadScoreObject_withMd5(){
    runDownloadScoreObjectTest(true, true);
  }

  @Test
  public void testInvalidScoreDownloadResponse_invalidMd5Length(){
    runInvalidScoreDownloadResponse(randomGenerator.generateRandomMD5()+3, 10000);
  }

  @Test
  public void testInvalidScoreDownloadResponse_invalidMd5Characters(){
    runInvalidScoreDownloadResponse(randomGenerator.generateRandomMD5().replaceFirst(".", "q"),
        10000);
  }

  @Test
  public void testInvalidScoreDownloadResponse_invalidSize0() {
    runInvalidScoreDownloadResponse(randomGenerator.generateRandomMD5(), 0L);
  }

  @Test
  public void testInvalidScoreDownloadResponse_invalidSizeMinusOne() {
    runInvalidScoreDownloadResponse(randomGenerator.generateRandomMD5(), -1L);
  }

  private void runInvalidScoreDownloadResponse(String expectedMd5, long expectedSize){
    val expectedResponse =  ScoreObject.builder()
        .objectId(randomGenerator.generateRandomUUIDAsString())
        .fileMd5sum(expectedMd5)
        .fileSize(expectedSize)
        .build();
    val scoreResponse = toNode(convertToObjectSpec(expectedResponse));
    val existingConfig = ScoreBehaviourConfig.builder()
        .objectExists(true)
        .expectedScoreResponse(scoreResponse)
        .build();
    setupScoreMockService(expectedResponse.getObjectId(),existingConfig);
    assertSongError(
        () -> scoreService.downloadObject(DEFAULT_ACCESS_TOKEN, expectedResponse.getObjectId()),
        INVALID_SCORE_DOWNLOAD_RESPONSE);
  }

  private void runDownloadScoreObjectTest(boolean exists, boolean md5Defined){
    val expectedResponse =  ScoreObject.builder()
        .objectId(randomGenerator.generateRandomUUIDAsString())
        .fileMd5sum(md5Defined ? randomGenerator.generateRandomMD5() : null)
        .fileSize((long)randomGenerator.generateRandomIntRange(1,100000))
        .build();
    val scoreResponse = toNode(convertToObjectSpec(expectedResponse));
    val existingConfig = ScoreBehaviourConfig.builder()
        .objectExists(exists)
        .expectedScoreResponse(scoreResponse)
        .build();
    setupScoreMockService(expectedResponse.getObjectId(),existingConfig);
    if (exists){
      val result = scoreService.downloadObject(DEFAULT_ACCESS_TOKEN, expectedResponse.getObjectId());
      assertThat(result).isEqualTo(expectedResponse);
    } else {
      assertSongError(
          () -> scoreService.downloadObject(DEFAULT_ACCESS_TOKEN, expectedResponse.getObjectId()),
          STORAGE_OBJECT_NOT_FOUND);
    }
  }

  private void setupScoreMockService(String objectId, ScoreBehaviourConfig config){
    wireMockRule.resetAll();
    wireMockRule.stubFor(get(urlMatching(format("/upload/%s", objectId)))
        .willReturn(aResponse()
            .withStatus(OK.value())
            .withBody(Boolean.toString(config.isObjectExists()))));

    wireMockRule.stubFor(get(urlMatching(format("/download/%s\\?offset=0&length=-1", objectId)))
        .willReturn(aResponse()
            .withStatus(OK.value())
            .withBody(toJson(config.getExpectedScoreResponse()))));
  }

  private static ObjectSpecification convertToObjectSpec(ScoreObject scoreObject){
    return ObjectSpecification.builder()
        .objectId(scoreObject.getObjectId())
        .objectMd5(scoreObject.getFileMd5sum())
        .objectSize(scoreObject.getFileSize())
        .build();
  }

  private static JsonNode toNode(Object o){
    return JsonUtils.mapper().valueToTree(o);
  }

  @Value
  @Builder
  public static class ScoreBehaviourConfig {
    private final boolean objectExists;
    @NonNull private final JsonNode expectedScoreResponse;
  }

  @Value
  @Builder
  public static class ObjectSpecification{
    @NonNull private final String objectId;
    private final String objectMd5;
    @NonNull private final Long objectSize;
  }

}
