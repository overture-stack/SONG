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
package org.icgc.dcc.song.server.repository.search;

import com.google.common.collect.Maps;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.icgc.dcc.common.core.json.JsonNodeBuilders;
import org.icgc.dcc.song.core.utils.JsonUtils;
import org.icgc.dcc.song.server.model.entity.Info;
import org.icgc.dcc.song.server.repository.InfoRepository;
import org.icgc.dcc.song.server.service.AnalysisService;
import org.icgc.dcc.song.server.service.UploadService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.util.LinkedMultiValueMap;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.icgc.dcc.common.core.util.Joiners.PATH;
import static org.icgc.dcc.song.server.model.entity.InfoPK.createInfoPK;
import static org.icgc.dcc.song.server.repository.search.InfoSearchRequest.createInfoSearchRequest;
import static org.icgc.dcc.song.server.repository.search.InfoSearchResponse.createWithInfo;
import static org.icgc.dcc.song.server.repository.search.InfoSearchResponse.createWithoutInfo;
import static org.icgc.dcc.song.server.repository.search.SearchTerm.parseSearchTerms;
import static org.icgc.dcc.song.server.utils.TestConstants.DEFAULT_ANALYSIS_ID;
import static org.icgc.dcc.song.server.utils.TestFiles.SEARCH_TEST_DIR;
import static org.icgc.dcc.song.server.utils.TestFiles.getJsonStringFromClasspath;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class})
@ActiveProfiles({"dev", "test"})
public class InfoSearchTest {

  private static final String STUDY = "ABC123";

  private static final Set<String> TEST_PAYLOAD_FILENAMES = newHashSet(
      "testData_0.json",
      "testData_1.json",
      "testData_2.json",
      "testData_3.json",
      "testData_4.json",
      "testData_5.json",
      "testData_6.json",
      "testData_7.json",
      "testData_8.json"
  );

  @Autowired private AnalysisService service;
  @Autowired private UploadService uploadService;
  @Autowired private InfoRepository infoRepository;

  private Map<String, InfoSearchResponse> analysisRespMap;

  @Before
  public void init(){
    if (analysisRespMap == null){//load it only once
      this.analysisRespMap = Maps.<String, InfoSearchResponse>newHashMap();
      for (val payloadPath : TEST_PAYLOAD_FILENAMES){
        val expectedResponse = extractResponse(STUDY, payloadPath);
        analysisRespMap.put(expectedResponse.getAnalysisId(), expectedResponse);
      }
    }
  }

  @Test
  public void testInfoSearchResponse(){
    val info = JsonNodeBuilders.object()
        .with("key1", "value1")
        .with("key2", "value2")
        .end();

    val withInfoResponse = createWithInfo(DEFAULT_ANALYSIS_ID, info);
    assertThat(withInfoResponse.getInfo()).isEqualTo(info);
    assertThat(withInfoResponse.hasInfo()).isTrue();

    val withoutInfoResponse = createWithoutInfo(DEFAULT_ANALYSIS_ID);
    assertThat(withoutInfoResponse.getInfo()).isNull();
    assertThat(withoutInfoResponse.hasInfo()).isFalse();
  }

  @Test
  public void testBasicTermSearch() {
    val term1 = "dataCategorization.dataType=SSM";
    val term2 = "dataCategorization.experimentalStrategy=WGS";
    runBasicTermSearchTest(() ->  search(true, term1, term2), true);
    runBasicTermSearchTest(() ->  search(false, term1, term2), false);
    runBasicTermSearchTest(() ->  search2(false, term1, term2), false);
    runBasicTermSearchTest(() ->  search2(true, term1, term2), true);
    assert(true);
  }

  @SneakyThrows
  private void runBasicTermSearchTest(Supplier<List<InfoSearchResponse>> responseSupplier, boolean shouldHaveInfo){
    val actualResponseList1 = responseSupplier.get();
    assertThat(actualResponseList1).hasSize(1);
    val infoSearchResponse1 = actualResponseList1.get(0);
    assertThat(this.analysisRespMap).containsKey(infoSearchResponse1.getAnalysisId());
    if (shouldHaveInfo){
      assertThat(infoSearchResponse1).isEqualTo(this.analysisRespMap.get(infoSearchResponse1.getAnalysisId()));
    }
    assertThat(infoSearchResponse1.hasInfo()).isEqualTo(shouldHaveInfo);
  }

  private InfoSearchResponse loadAndCreateResponse1(String study, String payloadString) throws Exception {
    val uploadStatus = uploadService.upload(study, payloadString, false );
    log.info(format("Got uploadStatus='%s'",uploadStatus));
    val uploadId = fromStatus(uploadStatus,"uploadId");
    val upload = uploadService.read(uploadId);
    assertThat(upload.getState()).isEqualTo("VALIDATED");
    val resp = uploadService.save(study, uploadId, false);
    val analysisId = fromStatus(resp,"analysisId");

    val infoPK = createInfoPK(analysisId, "Analysis");
    val infoResult = infoRepository.findById(infoPK);
    val info = infoResult.map(Info::getInfo)
        .map(JsonUtils::toJsonNode)
        .map(JsonUtils::toJson)
        .orElse(null);
    return createWithInfo(analysisId, JsonUtils.readTree(info) );
  }

  @SneakyThrows
  private List<InfoSearchResponse> search(boolean includeInfo, String ... searchTermStrings){
    val searchTerms = parseSearchTerms(searchTermStrings);
    val req = createInfoSearchRequest(includeInfo, searchTerms);
    return service.infoSearch(STUDY, req);
  }

  @SneakyThrows
  private List<InfoSearchResponse> search2(boolean includeInfo, String ... searchTermStrings){
    val searchTerms = parseSearchTerms(searchTermStrings);
    val map = new LinkedMultiValueMap<String, String>();
    searchTerms.forEach(x-> map.put(x.getKey(), newArrayList(x.getValue())));
    return service.infoSearch(STUDY, includeInfo, map);
  }

  @SneakyThrows
  private InfoSearchResponse extractResponse(String study, String payloadPath){
    val testDataPath = PATH.join(SEARCH_TEST_DIR, payloadPath);
    val testDataString = getJsonStringFromClasspath(testDataPath);
    return loadAndCreateResponse1(study, testDataString);
  }


  @SneakyThrows
  private String fromStatus( ResponseEntity<String> uploadStatus, String key) {
    val uploadId = JsonUtils.readTree(uploadStatus.getBody()).at("/"+key).asText("");
    return uploadId;
  }

}
