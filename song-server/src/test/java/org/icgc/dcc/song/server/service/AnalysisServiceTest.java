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
package org.icgc.dcc.song.server.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.google.common.collect.Maps;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.assertj.core.api.Assertions;
import org.icgc.dcc.song.core.exceptions.ServerError;
import org.icgc.dcc.song.core.exceptions.ServerException;
import org.icgc.dcc.song.core.utils.JsonUtils;
import org.icgc.dcc.song.server.model.Metadata;
import org.icgc.dcc.song.server.model.analysis.Analysis;
import org.icgc.dcc.song.server.model.analysis.SequencingReadAnalysis;
import org.icgc.dcc.song.server.model.analysis.VariantCallAnalysis;
import org.icgc.dcc.song.server.model.entity.File;
import org.icgc.dcc.song.server.utils.TestFiles;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.function.Supplier;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static java.lang.String.format;
import static java.util.Objects.isNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.UNPUBLISHED_FILE_IDS;
import static org.icgc.dcc.song.core.utils.JsonUtils.fromJson;
import static org.icgc.dcc.song.core.utils.JsonUtils.toJson;
import static org.icgc.dcc.song.server.utils.TestFiles.getInfoName;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class})
@ActiveProfiles({"dev", "test"})
public class AnalysisServiceTest {

  private static final String FILEPATH = "src/test/resources/fixtures/";
  private static final String TEST_FILEPATH = "src/test/resources/documents/";
  private static final int DCC_STORAGE_TEST_PORT = 8087;

  @Rule
  public WireMockRule wireMockRule = new WireMockRule(options().port(DCC_STORAGE_TEST_PORT));

  @Autowired
  FileService fileService;
  @Autowired
  AnalysisService service;

  @SneakyThrows
  private String readFile(String name) {
    return new String(Files.readAllBytes(new java.io.File("..", name).toPath()));
  }

  @Test
  public void testCreateAndUpdate() {
    val study="ABC123";
    val json = readFile(FILEPATH + "sequencingRead.json");
    val analysis = fromJson(json, Analysis.class);
    val analysisId=service.create(study, analysis, false);

    val created = service.read(analysisId);
    assertThat(created.getAnalysisId()).isEqualTo(analysisId);
    assertThat(created.getAnalysisState()).isEqualTo(analysis.getAnalysisState());
    assertThat(created.getAnalysisType()).isEqualTo("sequencingRead");
    assertThat(created.getSample().size()).isEqualTo(1);
    val sample = created.getSample().get(0);
    val experiment = ((SequencingReadAnalysis) created).getExperiment();
    assertThat(experiment).isNotNull();
    assertThat(experiment.getAlignmentTool().equals("BigWrench"));
    val expectedMetadata = new Metadata();
    expectedMetadata.setInfo("notes", "N/A");
    assertThat(experiment.getInfo()).isEqualTo(expectedMetadata.getInfo());

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
    val json = readFile(FILEPATH + "variantCall.json");
    val analysis = fromJson(json, Analysis.class);
    val analysisId=service.create(study, analysis, false);

    val created = service.read(analysisId);
    assertThat(created.getAnalysisId()).isEqualTo(analysisId);
    assertThat(created.getAnalysisState()).isEqualTo(analysis.getAnalysisState());
    assertThat(created.getAnalysisType()).isEqualTo("variantCall");
    assertThat(created.getSample().size()).isEqualTo(1);
    val sample = created.getSample().get(0);
    val experiment = ((VariantCallAnalysis) created).getExperiment();
    assertThat(experiment).isNotNull();
    assertThat(experiment.getVariantCallingTool()).isEqualTo("silver bullet");
    assertThat(experiment.getInfoAsString()).isEqualTo(
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

  @Ignore // When mvn runs this test, we get three files, not two. IntelliJ doesn't.
  @Test
  public void testRead() {
    // test sequencing read
    val id1="AN1";
    val analysis1 = service.read(id1);
    assertThat(analysis1.getAnalysisId()).isEqualTo("AN1");
    assertThat(analysis1.getAnalysisType()).isEqualTo("variantCall");
    assertThat(analysis1.getStudy()).isEqualTo("ABC123");
    assertThat(analysis1.getSample().size()).isEqualTo(2);
    assertThat(analysis1.getFile().size()).isEqualTo(2);
    assertThat(analysis1).isInstanceOf(VariantCallAnalysis.class);
    val experiment1 = ((VariantCallAnalysis) analysis1).getExperiment();
    assertThat(experiment1).isNotNull();
    assertThat(experiment1.getVariantCallingTool()).isEqualTo("SuperNewVariantCallingTool");

    assertThat(getInfoName(analysis1)).isEqualTo("analysis1");
    assertThat(getInfoName(experiment1)).isEqualTo("variantCall1");

    // test variant call
    val id2="AN2";
    val analysis2 = service.read(id2);
    assertThat(analysis2.getAnalysisId()).isEqualTo("AN2");
    //assertThat(analysis2.getAnalysisState()).isEqualTo("UNPUBLISHED");
    assertThat(analysis2.getAnalysisType()).isEqualTo("sequencingRead");
    assertThat(analysis2.getFile().size()).isEqualTo(2);
    assertThat(analysis2).isInstanceOf(SequencingReadAnalysis.class);
    val experiment2 = ((SequencingReadAnalysis) analysis2).getExperiment();
    assertThat(experiment2).isNotNull();
    assertThat(experiment2.getAlignmentTool()).isEqualTo("BigWrench");

    assertThat(getInfoName(analysis2)).isEqualTo("analysis2");
    assertThat(getInfoName(experiment2)).isEqualTo("sequencingRead2");

    //checkRead(id2, fromJson(json2, Analysis.class));

    // test not found
    val id3="ANDOESNTEXIST";
    val analysis3 = service.read(id3);
    assertThat(analysis3).isNull();
  }

  private void setUpDccStorageMockService(boolean expectedResult){
    wireMockRule.resetAll();
    wireMockRule.stubFor(get(urlMatching("/upload/.*"))
        .willReturn(aResponse()
            .withBody(Boolean.toString(expectedResult))));
  }

  @Test
  public void testPublish() {
    setUpDccStorageMockService(true);
    val token = "mockToken";
    val id = "AN1";
    service.publish(token, id);

    val analysis = service.read(id);
    assertThat(analysis.getAnalysisState()).isEqualTo("PUBLISHED");
  }

  @Test
  public void testPublishError() {
    setUpDccStorageMockService(false);
    val token = "mockToken";
    val id = "AN1";
    assertSongErrorTemp(() -> service.publish(token, id), UNPUBLISHED_FILE_IDS, null);
  }


  @Test
  public void testSuppress() {
    val id = "AN2";
    service.suppress(id);

    val analysis = service.read(id);
    assertThat(analysis.getAnalysisState()).isEqualTo("SUPPRESSED");
  }

  public String getJsonNodeFromClasspath(String name) throws Exception {
    InputStream is1 = Thread.currentThread().getContextClassLoader().getResourceAsStream(name);
    ObjectMapper mapper = new ObjectMapper();
    JsonNode node = mapper.readTree(is1);
    return mapper.writeValueAsString(node);
  }

  @Ignore // when we run this with IntelliJ, it works. Mvn gets a third file, with a different object id. We don't
          // know why...
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

  @Test
  public void testCustomAnalysisId(){
    val study="ABC123";
    val expectedAnalysisId = "AN-1234";
    val expectedObjectIdMap = Maps.newHashMap();
    expectedObjectIdMap.put("a3bc0998a-3521-43fd-fa10-a834f3874e46.MUSE_1-0rc-vcf.20170711.somatic.snv_mnv.vcf.gz", "0794ae66-80df-5b70-bc22-e49309bfba2a");
    expectedObjectIdMap.put("a3bc0998a-3521-43fd-fa10-a834f3874e46.MUSE_1-0rc-vcf.20170711.somatic.snv_mnv.vcf.gz.idx", "a2449e0a-7020-5f2d-8610-9f58aafd467a" );

    val json = TestFiles.getJsonNodeFromClasspath("documents/sequencingread-custom-analysis-id.json");
    val analysis = fromJson(json, Analysis.class);
    val actualAnalysisId=service.create(study, analysis, false);
    assertThat(actualAnalysisId).isEqualTo(expectedAnalysisId);
    for (val file : analysis.getFile()){
      val filename = file.getFileName();
      assertThat(expectedObjectIdMap).containsKey(filename);
      val expectedObjectId = expectedObjectIdMap.get(filename);
      val actualObjectId = file.getObjectId();
      val actualFileAnalysisId = file.getAnalysisId();
      assertThat(actualObjectId).isEqualTo(expectedObjectId);
      assertThat(actualFileAnalysisId).isEqualTo(actualAnalysisId);
    }
  }

  //TODO: This code is temporarily copied from another branch being worked on in parallel. Refactor later
  private static <T> void assertSongErrorTemp(@NonNull Supplier<T> supplier,
      @NonNull ServerError expectedServerError, String formattedFailMessage, Object...objects){
    val thrown = catchThrowable(supplier::get);

    val assertion = assertThat(thrown);
    if (!isNull(formattedFailMessage)){
      assertion.describedAs(format(formattedFailMessage, objects));
    }
    assertion.isInstanceOf(ServerException.class);

    val songError = ((ServerException)thrown).getSongError();
    assertThat(songError.getErrorId()).isEqualTo(expectedServerError.getErrorId());
    assertThat(songError.getHttpStatusCode()).isEqualTo(expectedServerError.getHttpStatus().value());
  }

}
