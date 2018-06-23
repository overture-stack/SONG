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

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
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

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
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

  private ScoreService scoreService;
  private final RandomGenerator randomGenerator = createRandomGenerator(ScoreServiceTest.class.getSimpleName());

  @Rule
  public WireMockRule wireMockRule = new WireMockRule(options().dynamicPort());

  @Before
  public void beforeTest(){
    val testStorageUrl = format("http://localhost:%s", wireMockRule.port());
    this.scoreService = createScoreService(retryTemplate, testStorageUrl);
  }

  @Test
  public void testObjectExistence(){
    val expectedScoreObject =  ScoreObject.builder()
        .objectId(randomGenerator.generateRandomUUIDAsString())
        .fileMd5sum(randomGenerator.generateRandomMD5())
        .fileSize((long)randomGenerator.generateRandomIntRange(1,100000))
        .build();
    val scoreResponse = convertToObjectSpec(expectedScoreObject);

    // Test nonexisting
    val nonExistingConfig = ScoreBehaviourConfig.builder()
        .objectExists(false)
        .expectedObjectSpec(scoreResponse)
        .build();
    setupScoreMockService(nonExistingConfig);
    val result = scoreService.isObjectExist(DEFAULT_ACCESS_TOKEN, expectedScoreObject.getObjectId());
    assertThat(result).isFalse();

    // Test existing
    val existingConfig = ScoreBehaviourConfig.builder()
        .objectExists(true)
        .expectedObjectSpec(scoreResponse)
        .build();
    setupScoreMockService(existingConfig);
    val result2 = scoreService.isObjectExist(DEFAULT_ACCESS_TOKEN, expectedScoreObject.getObjectId());
    assertThat(result2).isTrue();
  }

  @Test
  public void testExistingDownloadScoreObject(){
    val expectedResponse =  ScoreObject.builder()
        .objectId(randomGenerator.generateRandomUUIDAsString())
        .fileSize((long)randomGenerator.generateRandomIntRange(1,100000))
        .build();
    val scoreResponse = convertToObjectSpec(expectedResponse);
    val existingConfig1 = ScoreBehaviourConfig.builder()
        .objectExists(true)
        .expectedObjectSpec(scoreResponse)
        .build();
    setupScoreMockService(existingConfig1);
    val result = scoreService.downloadObject(DEFAULT_ACCESS_TOKEN, expectedResponse.getObjectId());
    assertThat(result).isEqualTo(expectedResponse);
  }

  @Test
  public void testNonExistingDownloadScoreObject(){
    val expectedResponse =  ScoreObject.builder()
        .objectId(randomGenerator.generateRandomUUIDAsString())
        .fileSize((long)randomGenerator.generateRandomIntRange(1,100000))
        .build();
    val scoreResponse = convertToObjectSpec(expectedResponse);
    val existingConfig = ScoreBehaviourConfig.builder()
        .objectExists(false)
        .expectedObjectSpec(scoreResponse)
        .build();
    setupScoreMockService(existingConfig);
    assertSongError(
        () -> scoreService.downloadObject(DEFAULT_ACCESS_TOKEN, expectedResponse.getObjectId()),
        STORAGE_OBJECT_NOT_FOUND);
  }

  private void setupScoreMockService(ScoreBehaviourConfig config){
    val objectId = config.getExpectedObjectSpec().getObjectId();
    wireMockRule.resetAll();
    wireMockRule.stubFor(get(urlMatching(format("/upload/%s", objectId)))
        .willReturn(aResponse()
            .withStatus(OK.value())
            .withBody(Boolean.toString(config.isObjectExists()))));

    wireMockRule.stubFor(get(urlMatching(format("/download/%s\\?offset=0&length=-1", objectId)))
        .willReturn(aResponse()
            .withStatus(OK.value())
            .withBody(toJson(config.getExpectedObjectSpec()))));
  }

  @Value
  @Builder
  public static class ScoreBehaviourConfig {
    private final boolean objectExists;
    @NonNull private final ObjectSpecification expectedObjectSpec;
  }

  @Value
  @Builder
  public static class ObjectSpecification{
    @NonNull private final String objectId;
    private final String objectMd5;
    @NonNull private final Long objectSize;
  }

  private static ObjectSpecification convertToObjectSpec(ScoreObject scoreObject){
    return ObjectSpecification.builder()
        .objectId(scoreObject.getObjectId())
        .objectMd5(scoreObject.getFileMd5sum())
        .objectSize(scoreObject.getFileSize())
        .build();
  }

}
