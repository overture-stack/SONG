/*
 * Copyright (c) 2017 The Ontario Institute for Cancer Research. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the GNU Public License v3.0.
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
 * SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER
 * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */
package org.icgc.dcc.song.server.repository.search;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.icgc.dcc.song.core.utils.JsonUtils;
import org.icgc.dcc.song.server.repository.InfoRepository;
import org.icgc.dcc.song.server.service.AnalysisService;
import org.icgc.dcc.song.server.service.UploadService;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;
import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.icgc.dcc.common.core.util.Joiners.PATH;
import static org.icgc.dcc.song.server.repository.search.InfoSearchRequest.createInfoSearchRequest;
import static org.icgc.dcc.song.server.repository.search.InfoSearchResponse.createWithInfo;
import static org.icgc.dcc.song.server.repository.search.SearchTerm.parseSearchTerms;
import static org.junit.Assert.fail;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class})
@ActiveProfiles("dev")
public class InfoSearchTest {

  private static final String STUDY = "ABC123";
  private static final String PAYLOAD_DIR = "documents/search";

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
  @SneakyThrows
  public void testTemplateTest(){
    val actualResponseList = search(true,
        "dataCategorization.dataType=SSM",
        "dataCategorization.experimentalStrategy=WGS");
    assertThat(actualResponseList).hasSize(1);
    val infoSearchResponse = actualResponseList.get(0);
    assertThat(this.analysisRespMap).containsKey(infoSearchResponse.getAnalysisId());
    assertThat(infoSearchResponse).isEqualTo(this.analysisRespMap.get(infoSearchResponse.getAnalysisId()));
  }

  @Ignore
  @Test
  public void testGreedy(){
    // 1) key1 = miss and key1 = a$
    //    - data1 has key1 = mississauga, key2 = base
    //    - data2 has key1 = missippi, key2 = missa
    fail("not implemented yet");
  }

  @Ignore
  @Test
  public void testNonGreedy(){
    // test "^rob$" and "^ro$"
    fail("not implemented yet");
  }

  @Ignore
  @Test
  public void testNested(){
    // test searching for documents with nested values
    //      data:
    //      { "name" : "rob",
    //        "address" : { "coordinate" : { "latitude" : 10.0 ,  "longitude" : 12.0 } }
    //      }
    //      { "name" : "alex",
    //        "address" : { "coordinate" : { "latitude" : 17.0 ,  "longitude" : 32.0 } }
    //      }
    // 1)  key1 is address->coordinate->latitude
    // 2)  key1 is name
    fail("not implemented yet");
  }

  @Ignore
  @Test
  public void testANDing(){
    // test mutlple search terms.
    // 1) key1 = miss and key1 = a$
    //    - data1 has key1 = mississauga, key2 = base
    //    - data2 has key1 = missippi, key2 = missa
    // 2) key1 = miss and key2 = male
    //    - data1 has key1 = mississauga, key2 = male
    //    - data2 has key1 = missippi, key2 = male
    //    - data3 has key1 = missippi, key2 = unknown
    //    - data3 has key1 = toronto, key2 = male
    //    - data3 has key1 = toronto, key2 = unknown
    fail("not implemented yet");
  }

  @Ignore
  @Test
  public void testIncludeInfo(){
    // test that response has correct fields depending on includeInfo value
    fail("not implemented yet");
  }

  @Ignore
  @Test
  public void testExcludeInfo(){
    // test that response has correct fields depending on includeInfo value
    fail("not implemented yet");
  }

  @Ignore
  @Test
  public void testSyntaxErrors(){
    // create malformed search term if possible
    fail("not implemented yet");
  }

  @Ignore
  @Test
  public void testArrayNonGreedy(){
    // test that a field that contains an array, can be searched for a non-greedy pattern, and if one or more items
    // it, the analysis id is returned. for example. info.info->>'study' ~ '^PCA$'
    fail("not implemented yet");
  }

  @Ignore
  @Test
  public void testArrayGreedy(){
    // test that a field that contains an array, can be searched for a GREEDY pattern...
    fail("not implemented yet");
  }

  private InfoSearchResponse loadAndCreateResponse1(String study, String payloadString) throws Exception {
    val uploadStatus = uploadService.upload(study, payloadString, false );
    log.info(format("Got uploadStatus='%s'",uploadStatus));
    val uploadId = fromStatus(uploadStatus,"uploadId");
    val upload = uploadService.read(uploadId);
    assertThat(upload.getState()).isEqualTo("VALIDATED");
    val resp = uploadService.save(study, uploadId);
    val analysisId = fromStatus(resp,"analysisId");
    val info = infoRepository.read(analysisId, "Analysis");
    return createWithInfo(analysisId, JsonUtils.readTree(info) );
  }

  @SneakyThrows
  private List<InfoSearchResponse> search(boolean includeInfo, String ... searchTermStrings){
    val searchTerms = parseSearchTerms(searchTermStrings);
    val req = createInfoSearchRequest(includeInfo, searchTerms);
    return service.infoSearch(STUDY, req);
  }

  @SneakyThrows
  private InfoSearchResponse extractResponse(String study, String payloadPath){
    val testDataPath = PATH.join(PAYLOAD_DIR, payloadPath);
    val testDataString = getJsonNodeFromClasspath(testDataPath);
    return loadAndCreateResponse1(study, testDataString);
  }


  @SneakyThrows
  private String fromStatus( ResponseEntity<String> uploadStatus, String key) {
    val uploadId = JsonUtils.readTree(uploadStatus.getBody()).at("/"+key).asText("");
    return uploadId;
  }

  private static String getJsonNodeFromClasspath(String name) throws Exception {
    InputStream is1 = Thread.currentThread().getContextClassLoader().getResourceAsStream(name);
    ObjectMapper mapper = new ObjectMapper();
    JsonNode node = mapper.readTree(is1);
    return mapper.writeValueAsString(node);
  }

}
