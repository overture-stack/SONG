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

package bio.overture.song.server.service;

import bio.overture.song.core.utils.JsonUtils;
import bio.overture.song.core.utils.RandomGenerator;
import bio.overture.song.server.model.StorageObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.springframework.http.HttpStatus.OK;
import static bio.overture.song.core.exceptions.ServerErrors.INVALID_STORAGE_DOWNLOAD_RESPONSE;
import static bio.overture.song.core.exceptions.ServerErrors.STORAGE_OBJECT_NOT_FOUND;
import static bio.overture.song.core.testing.SongErrorAssertions.assertSongError;
import static bio.overture.song.core.utils.JsonUtils.toJson;
import static bio.overture.song.core.utils.RandomGenerator.createRandomGenerator;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
public class StorageServiceTest {

  private static final String DEFAULT_ACCESS_TOKEN = "anyToken";

  @Autowired
  private RetryTemplate retryTemplate;

  @Autowired
  private ValidationService validationService;

  private StorageService storageService;
  private final RandomGenerator randomGenerator = createRandomGenerator(StorageServiceTest.class.getSimpleName());

  @Rule
  public WireMockRule wireMockRule = new WireMockRule(options().dynamicPort());

  @Before
  public void beforeTest(){
    val testStorageUrl = format("http://localhost:%s", wireMockRule.port());
    this.storageService = StorageService.builder()
            .restTemplate(new RestTemplate())
            .retryTemplate(retryTemplate)
            .storageUrl(testStorageUrl)
            .validationService(validationService)
            .scoreAuthorizationHeader(DEFAULT_ACCESS_TOKEN)
            .build();
  }

  @Test
  public void testObjectExistence(){
    val expectedStorageObject =  StorageObject.builder()
        .objectId(randomGenerator.generateRandomUUIDAsString())
        .fileMd5sum(randomGenerator.generateRandomMD5())
        .fileSize((long)randomGenerator.generateRandomIntRange(1,100000))
        .build();
    val storageResponse = toNode(convertToObjectSpec(expectedStorageObject));

    // Test nonexisting
    val nonExistingConfig = StorageBehaviourConfig.builder()
        .objectExists(false)
        .expectedStorageResponse(storageResponse)
        .build();
    setupStorageMockService(expectedStorageObject.getObjectId(), nonExistingConfig);
    val result = storageService.isObjectExist(expectedStorageObject.getObjectId());
    assertFalse(result);

    // Test existing
    val existingConfig = StorageBehaviourConfig.builder()
        .objectExists(true)
        .expectedStorageResponse(storageResponse)
        .build();
    setupStorageMockService(expectedStorageObject.getObjectId(), existingConfig);
    val result2 = storageService.isObjectExist(expectedStorageObject.getObjectId());
    assertThat(result2).isTrue();
  }

  @Test
  public void testNonExistingDownloadStorageObject_noMd5(){
    runDownloadStorageObjectTest(false, false);
  }

  @Test
  public void testNonExistingDownloadStorageObject_withMd5(){
    runDownloadStorageObjectTest(false, true);
  }

  @Test
  public void testExistingDownloadStorageObject_noMd5(){
    runDownloadStorageObjectTest(true, false);
  }

  @Test
  public void testExistingDownloadStorageObject_withMd5(){
    runDownloadStorageObjectTest(true, true);
  }

  @Test
  public void testInvalidStorageDownloadResponse_invalidMd5Length(){
    runInvalidStorageDownloadResponse(randomGenerator.generateRandomMD5()+3, 10000);
  }

  @Test
  public void testInvalidStorageDownloadResponse_invalidMd5Characters(){
    runInvalidStorageDownloadResponse(randomGenerator.generateRandomMD5().replaceFirst(".", "q"),
        10000);
  }

  @Test
  public void testInvalidStorageDownloadResponse_invalidSize0() {
    runInvalidStorageDownloadResponse(randomGenerator.generateRandomMD5(), 0L);
  }

  @Test
  public void testInvalidStorageDownloadResponse_invalidSizeMinusOne() {
    runInvalidStorageDownloadResponse(randomGenerator.generateRandomMD5(), -1L);
  }

  private void runInvalidStorageDownloadResponse(String expectedMd5, long expectedSize){
    val expectedResponse =  StorageObject.builder()
        .objectId(randomGenerator.generateRandomUUIDAsString())
        .fileMd5sum(expectedMd5)
        .fileSize(expectedSize)
        .build();
    val storageResponse = toNode(convertToObjectSpec(expectedResponse));
    val existingConfig = StorageBehaviourConfig.builder()
        .objectExists(true)
        .expectedStorageResponse(storageResponse)
        .build();
    setupStorageMockService(expectedResponse.getObjectId(),existingConfig);
    assertSongError(
        () -> storageService.downloadObject(expectedResponse.getObjectId()),
        INVALID_STORAGE_DOWNLOAD_RESPONSE);
  }

  private void runDownloadStorageObjectTest(boolean exists, boolean md5Defined){
    val expectedResponse =  StorageObject.builder()
        .objectId(randomGenerator.generateRandomUUIDAsString())
        .fileMd5sum(md5Defined ? randomGenerator.generateRandomMD5() : null)
        .fileSize((long)randomGenerator.generateRandomIntRange(1,100000))
        .build();
    val storageResponse = toNode(convertToObjectSpec(expectedResponse));
    val existingConfig = StorageBehaviourConfig.builder()
        .objectExists(exists)
        .expectedStorageResponse(storageResponse)
        .build();
    setupStorageMockService(expectedResponse.getObjectId(),existingConfig);
    if (exists){
      val result = storageService.downloadObject(expectedResponse.getObjectId());
      assertEquals(result,expectedResponse);
    } else {
      assertSongError(
          () -> storageService.downloadObject(expectedResponse.getObjectId()),
          STORAGE_OBJECT_NOT_FOUND);
    }
  }

  private void setupStorageMockService(String objectId, StorageBehaviourConfig config){
    wireMockRule.resetAll();
    wireMockRule.stubFor(get(urlMatching(format("/upload/%s", objectId)))
        .willReturn(aResponse()
            .withStatus(OK.value())
            .withBody(Boolean.toString(config.isObjectExists()))));

    wireMockRule.stubFor(get(urlMatching(format("/download/%s\\?offset=0&length=-1&exclude-urls=true", objectId)))
        .willReturn(aResponse()
            .withStatus(OK.value())
            .withBody(toJson(config.getExpectedStorageResponse()))));
  }

  private static ObjectSpecification convertToObjectSpec(StorageObject storageObject){
    return ObjectSpecification.builder()
        .objectId(storageObject.getObjectId())
        .objectMd5(storageObject.getFileMd5sum())
        .objectSize(storageObject.getFileSize())
        .build();
  }

  private static JsonNode toNode(Object o){
    return JsonUtils.mapper().valueToTree(o);
  }

  @Value
  @Builder
  public static class StorageBehaviourConfig {
    private final boolean objectExists;
    @NonNull private final JsonNode expectedStorageResponse;
  }

  @Value
  @Builder
  public static class ObjectSpecification{
    @NonNull private final String objectId;
    private final String objectMd5;
    @NonNull private final Long objectSize;
  }

}
