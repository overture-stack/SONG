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
import com.google.common.collect.Lists;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.assertj.core.api.Assertions;
import org.icgc.dcc.song.core.utils.JsonUtils;
import org.icgc.dcc.song.server.model.analysis.Analysis;
import org.icgc.dcc.song.server.model.analysis.SequencingReadAnalysis;
import org.icgc.dcc.song.server.model.analysis.VariantCallAnalysis;
import org.icgc.dcc.song.server.model.entity.File;
import org.icgc.dcc.song.server.repository.InfoRepository;
import org.icgc.dcc.song.server.service.AnalysisService;
import org.icgc.dcc.song.server.service.FileService;
import org.icgc.dcc.song.server.service.UploadService;
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
import java.nio.file.Files;
import java.util.ArrayList;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.icgc.dcc.song.core.utils.JsonUtils.fromJson;
import static org.icgc.dcc.song.core.utils.JsonUtils.toJson;
import static org.icgc.dcc.song.server.repository.search.InfoSearchResponse.createWithInfo;
import static org.icgc.dcc.song.server.repository.search.SearchTerm.createSearchTerm;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class})
@ActiveProfiles("dev")
public class InfoSearchTest {

  @Autowired
  FileService fileService;
  @Autowired
  AnalysisService service;

  @Autowired
  UploadService uploadService;

  @Autowired InfoRepository infoRepository;

  @SneakyThrows
  private String readFile(String name) {
    return new String(Files.readAllBytes(new java.io.File("..", name).toPath()));
  }

  private InfoSearchResponse loadAndCreateResponse(String study, String payloadPath) throws Exception {
    val json = getJsonNodeFromClasspath(payloadPath);
    val uploadStatus = uploadService.upload(study, json, false );
    log.info(format("Got uploadStatus='%s'",uploadStatus));
    val uploadId = fromStatus(uploadStatus,"uploadId");
    val upload = uploadService.read(uploadId);
    assertThat(upload.getState()).isEqualTo("VALIDATED");
    val resp = uploadService.save(study, uploadId);
    val analysisId = fromStatus(resp,"analysisId");
    val info = infoRepository.read(analysisId, "Analysis");
    return createWithInfo(analysisId, JsonUtils.readTree(info) );
  }

  @Test
  public void testMe() throws Exception {
    val study = "ABC123";
    val payloadPath ="documents/search/sequencingread1.json";
    val expectedInfoSearchResponse = loadAndCreateResponse(study, payloadPath);
    val st = createSearchTerm("name", "rob");
    val request = InfoSearchRequest.createInfoSearchRequest(true, Lists.newArrayList(st));
    val actualInfoSearchResponseArray = service.infoSearch(study, request );
    assertThat(actualInfoSearchResponseArray).hasSize(1);
    val firstActualResult = actualInfoSearchResponseArray.get(0);
    assertThat(firstActualResult).isEqualTo(expectedInfoSearchResponse);
    log.info("sdfsdf");
  }

  @SneakyThrows
  public String fromStatus( ResponseEntity<String> uploadStatus, String key) {
    val uploadId = JsonUtils.readTree(uploadStatus.getBody()).at("/"+key).asText("");
    return uploadId;
  }

  @Test
  public void testGreedy(){
    // 1) key1 = miss and key1 = a$
    //    - data1 has key1 = mississauga, key2 = base
    //    - data2 has key1 = missippi, key2 = missa
  }

  @Test
  public void testNonGreedy(){
    // test "^rob$" and "^ro$"
  }

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
  }

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
  }

  @Test
  public void testIncludeInfo(){
    // test that response has correct fields depending on includeInfo value
  }

  @Test
  public void testExcludeInfo(){
    // test that response has correct fields depending on includeInfo value
  }

  @Test
  public void testSyntaxErrors(){
    // create malformed search term if possible
  }



  @Test
  public void testCreateAndUpdate() {
    val study="ABC123";
    val json = readFile("sequencingRead.json");
    val analysis = fromJson(json, Analysis.class);
    val analysisId=service.create(study, analysis);

    val created = service.read(analysisId);
    assertThat(created.getAnalysisId()).isEqualTo(analysisId);
    assertThat(created.getAnalysisState()).isEqualTo(analysis.getAnalysisState());
    assertThat(created.getAnalysisType()).isEqualTo("sequencingRead");
    assertThat(created.getSample().size()).isEqualTo(1);
    val sample = created.getSample().get(0);
    val experiment = ((SequencingReadAnalysis) created).getExperiment();
    assertThat(experiment).isNotNull();
    assertThat(experiment.getAlignmentTool().equals("BigWrench"));
    assertThat(experiment.getInfo()).isEqualTo(JsonUtils.fromSingleQuoted("{'notes':'N/A'}"));

;    // test update
    val change="ModifiedToolName";
    experiment.setAlignmentTool(change);
    service.updateAnalysis(study, created);
    val gotBack = service.read(analysisId);
    val experiment2 =((SequencingReadAnalysis)gotBack).getExperiment();
    assertThat(experiment2.getAlignmentTool() ).isEqualTo(change);

    log.info(format("Created '%s'",toJson(created)));
  }

  @Test
  public void testCreateAndUpdateVariantCall() {
    val study="ABC123";
    val json = readFile("variantCall.json");
    val analysis = fromJson(json, Analysis.class);
    val analysisId=service.create(study, analysis);

    val created = service.read(analysisId);
    assertThat(created.getAnalysisId()).isEqualTo(analysisId);
    assertThat(created.getAnalysisState()).isEqualTo(analysis.getAnalysisState());
    assertThat(created.getAnalysisType()).isEqualTo("variantCall");
    assertThat(created.getSample().size()).isEqualTo(1);
    val sample = created.getSample().get(0);
    val experiment = ((VariantCallAnalysis) created).getExperiment();
    assertThat(experiment).isNotNull();
    assertThat(experiment.getVariantCallingTool()).isEqualTo("silver bullet");
    assertThat(experiment.getInfo()).isEqualTo(
            JsonUtils.fromSingleQuoted("{'notes':'we can put anything we want as extra JSON fields'}"));
    // test update
    val change="GoldenHammer";
    experiment.setVariantCallingTool(change) ;
    service.updateAnalysis(study, created);
    val gotBack = service.read(analysisId);
    val experiment2 =((VariantCallAnalysis)gotBack).getExperiment();
    assertThat(experiment2.getVariantCallingTool()).isEqualTo(change);

    log.info(format("Created '%s'",toJson(created)));
  }


  @Test
  public void testRead() {
    // test sequencing read
    val id1="AN1";
    val json1 = readFile("existingVariantCall.json");
    val analysis1 = service.read(id1);
    assertThat(analysis1.getAnalysisId()).isEqualTo("AN1");
    //assertThat(analysis1.getAnalysisState()).isEqualTo("UNPUBLISHED");
    assertThat(analysis1.getAnalysisType()).isEqualTo("variantCall");
    assertThat(analysis1.getStudy()).isEqualTo("ABC123");
    assertThat(analysis1.getSample().size()).isEqualTo(2);
    assertThat(analysis1.getInfo()).isEqualTo("{}");
    assertThat(analysis1.getFile().size()).isEqualTo(2);
    assertThat(analysis1).isInstanceOf(VariantCallAnalysis.class);
    val experiment1 = ((VariantCallAnalysis) analysis1).getExperiment();
    assertThat(experiment1).isNotNull();
    assertThat(experiment1.getVariantCallingTool()).isEqualTo("SuperNewVariantCallingTool");


    // test variant call
    val id2="AN2";
    val json2 = readFile("existingSequencingRead.json");
    val analysis2 = service.read(id2);
    assertThat(analysis2.getAnalysisId()).isEqualTo("AN2");
    //assertThat(analysis2.getAnalysisState()).isEqualTo("UNPUBLISHED");
    assertThat(analysis2.getAnalysisType()).isEqualTo("sequencingRead");
    assertThat(analysis2.getFile().size()).isEqualTo(2);
    assertThat(analysis2).isInstanceOf(SequencingReadAnalysis.class);
    val experiment2 = ((SequencingReadAnalysis) analysis2).getExperiment();
    assertThat(experiment2).isNotNull();
    assertThat(experiment2.getAlignmentTool()).isEqualTo("BigWrench");

    //checkRead(id2, fromJson(json2, Analysis.class));

    // test not found
    val id3="ANDOESNTEXIST";
    val analysis3 = service.read(id3);
    assertThat(analysis3).isNull();
  }


  public String getJsonNodeFromClasspath(String name) throws Exception {
    InputStream is1 = Thread.currentThread().getContextClassLoader().getResourceAsStream(name);
    ObjectMapper mapper = new ObjectMapper();
    JsonNode node = mapper.readTree(is1);
    return mapper.writeValueAsString(node);
  }

  @Test
  public void testReadFiles() {
    val files = service.readFiles("AN1");
    System.err.printf("Got files '%s'", files);
    val expectedFiles = new ArrayList<File>();
    expectedFiles.add(fileService.read("FI1"));
    expectedFiles.add(fileService.read("FI2"));

    Assertions.assertThat(files).containsAll(expectedFiles);
    assertThat(expectedFiles).containsAll(files);
  }

}
