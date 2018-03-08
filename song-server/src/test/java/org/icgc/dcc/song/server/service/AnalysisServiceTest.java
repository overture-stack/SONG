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
import com.fasterxml.uuid.impl.TimeBasedGenerator;
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
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.function.Supplier;

import static com.fasterxml.uuid.Generators.timeBasedGenerator;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static java.lang.String.format;
import static java.util.Objects.isNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.Assertions.fail;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.UNPUBLISHED_FILE_IDS;
import static org.icgc.dcc.song.core.utils.JsonUtils.fromJson;
import static org.icgc.dcc.song.core.utils.JsonUtils.toJson;
import static org.icgc.dcc.song.server.service.ExistenceService.createExistenceService;
import static org.icgc.dcc.song.server.utils.TestFiles.assertInfoKVPair;
import static org.icgc.dcc.song.server.utils.TestFiles.getInfoName;
import static org.springframework.http.HttpStatus.OK;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class})
@ActiveProfiles({"dev", "test"})
public class AnalysisServiceTest {

  private static final String DEFAULT_STUDY_ID = "ABC123";

  private static final String FILEPATH = "src/test/resources/fixtures/";
  private static final String TEST_FILEPATH = "src/test/resources/documents/";

  @Rule
  public WireMockRule wireMockRule = new WireMockRule(options().dynamicPort());

  @Autowired
  FileService fileService;
  @Autowired
  AnalysisService service;

  @Autowired
  private RetryTemplate retryTemplate;

  @SneakyThrows
  private String readFile(String name) {
    return new String(Files.readAllBytes(new java.io.File("..", name).toPath()));
  }

  /**
   * This is dirty, but since the existenceService is so easy to construct
   * and the storage url port is randomly assigned, it's worth it.
   */
  @Before
  public void init(){
    val testStorageUrl = format("http://localhost:%s", wireMockRule.port());
    val testExistenceService = createExistenceService(retryTemplate,testStorageUrl);
    ReflectionTestUtils.setField(service, "existence", testExistenceService);
    log.info("ExistenceService configured to endpoint: {}",testStorageUrl );
  }

  @Test
  public void testCreateAndUpdate() {
    val study=DEFAULT_STUDY_ID;
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
    val study=DEFAULT_STUDY_ID;
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

  @Test
  public void testReadSequencingRead(){
    val json = TestFiles.getJsonStringFromClasspath("documents/sequencingread-read-test.json");
    val analysisRaw = fromJson(json, SequencingReadAnalysis.class);
    val analysisId = service.create(DEFAULT_STUDY_ID, analysisRaw, false);
    val a = service.read(analysisId);

    //Asserting Analysis
    assertThat(a.getAnalysisState()).isEqualTo("UNPUBLISHED");
    assertThat(a.getAnalysisType()).isEqualTo("sequencingRead");
    assertThat(a.getStudy()).isEqualTo(DEFAULT_STUDY_ID);
    assertInfoKVPair(a, "description1","description1 for this sequencingRead analysis an01" );
    assertInfoKVPair(a, "description2","description2 for this sequencingRead analysis an01" );

    //Asserting Sample
    assertThat(a.getSample()).hasSize(2);
    val sample0 = a.getSample().get(0);
    assertThat(sample0.getSampleSubmitterId()).isEqualTo("internal_sample_98024759826836_fr01");
    assertThat(sample0.getSampleType()).isEqualTo("Total RNA");
    assertInfoKVPair(sample0, "extraSampleInfo","some more data for a sequencingRead sample_fr01");

    val donor00 = sample0.getDonor();
    assertThat(donor00.getStudyId()).isEqualTo(DEFAULT_STUDY_ID);
    assertThat(donor00.getDonorGender()).isEqualTo("male");
    assertThat(donor00.getDonorSubmitterId()).isEqualTo("internal_donor_123456789-00_fr01");
    assertInfoKVPair(donor00, "extraDonorInfo", "some more data for a sequencingRead donor_fr01");

    val specimen00 = sample0.getSpecimen();
    assertThat(specimen00.getDonorId()).isEqualTo(donor00.getDonorId());
    assertThat(specimen00.getSpecimenClass()).isEqualTo("Tumour");
    assertThat(specimen00.getSpecimenType()).isEqualTo("Primary tumour - other");
    assertThat(sample0.getSpecimenId()).isEqualTo(specimen00.getSpecimenId());
    assertInfoKVPair(specimen00, "extraSpecimenInfo_0", "first for a sequencingRead specimen_fr01");
    assertInfoKVPair(specimen00, "extraSpecimenInfo_1", "second data for a sequencingRead specimen_fr01");

    val sample1 = a.getSample().get(1);
    assertThat(sample1.getSampleSubmitterId()).isEqualTo("internal_sample_98024759826836_fr02");
    assertThat(sample1.getSampleType()).isEqualTo("Total RNA");
    assertInfoKVPair(sample1, "extraSampleInfo","some more data for a sequencingRead sample_fr02");

    val donor01 = sample1.getDonor();
    assertThat(donor01.getStudyId()).isEqualTo(DEFAULT_STUDY_ID);
    assertThat(donor01.getDonorGender()).isEqualTo("female");
    assertThat(donor01.getDonorSubmitterId()).isEqualTo("internal_donor_123456789-00_fr02");
    assertInfoKVPair(donor01, "extraDonorInfo_0", "first data for a sequencingRead donor_fr02");
    assertInfoKVPair(donor01, "extraDonorInfo_1","second data for a sequencingRead donor_fr02");

    val specimen01 = sample1.getSpecimen();
    assertThat(specimen01.getDonorId()).isEqualTo(donor01.getDonorId());
    assertThat(specimen01.getSpecimenClass()).isEqualTo("Tumour");
    assertThat(specimen01.getSpecimenType()).isEqualTo("Primary tumour - other");
    assertThat(sample1.getSpecimenId()).isEqualTo(specimen01.getSpecimenId());
    assertInfoKVPair(specimen01, "extraSpecimenInfo", "some more data for a sequencingRead specimen_fr02");

    assertThat(a.getFile()).hasSize(3);
    val file0 = a.getFile().get(0);
    val file1 = a.getFile().get(1);
    val file2 = a.getFile().get(2);
    assertThat(file0.getAnalysisId()).isEqualTo(analysisId);
    assertThat(file1.getAnalysisId()).isEqualTo(analysisId);
    assertThat(file2.getAnalysisId()).isEqualTo(analysisId);
    assertThat(file0.getStudyId()).isEqualTo(DEFAULT_STUDY_ID);
    assertThat(file1.getStudyId()).isEqualTo(DEFAULT_STUDY_ID);
    assertThat(file2.getStudyId()).isEqualTo(DEFAULT_STUDY_ID);

    val fileName0 = "a3bc0998a-3521-43fd-fa10-a834f3874e46-fn1.MUSE_1-0rc-vcf.20170711.bam";
    val fileName1 = "a3bc0998a-3521-43fd-fa10-a834f3874e46-fn2.MUSE_1-0rc-vcf.20170711.bam";
    val fileName2 = "a3bc0998a-3521-43fd-fa10-a834f3874e46-fn3.MUSE_1-0rc-vcf.20170711.bam.bai";

    for (val file : a.getFile()){
      if (file.getFileName().equals(fileName0)){
        assertThat(file.getFileName()).isEqualTo(fileName0);
        assertThat(file.getFileSize()).isEqualTo(1212121);
        assertThat(file.getFileMd5sum()).isEqualTo("e2324667df8085eddfe95742047e153f");
        assertThat(file.getFileAccess()).isEqualTo("controlled");
        assertThat(file.getFileType()).isEqualTo("BAM");
        assertInfoKVPair(file, "extraFileInfo_0", "first data for sequencingRead file_fn1");
        assertInfoKVPair(file, "extraFileInfo_1", "second data for sequencingRead file_fn1");
      } else if (file.getFileName().equals(fileName1)){
        assertThat(file.getFileName()).isEqualTo(fileName1);
        assertThat(file.getFileSize()).isEqualTo(34343);
        assertThat(file.getFileMd5sum()).isEqualTo("8b5379a29aac642d6fe1808826bd9e49");
        assertThat(file.getFileAccess()).isEqualTo("open");
        assertThat(file.getFileType()).isEqualTo("BAM");
        assertInfoKVPair(file, "extraFileInfo", "some more data for sequencingRead file_fn2");

      } else if (file.getFileName().equals(fileName2)){
        assertThat(file.getFileName()).isEqualTo(fileName2);
        assertThat(file.getFileSize()).isEqualTo(4840);
        assertThat(file.getFileMd5sum()).isEqualTo("61da923f32863a9c5fa3d2a0e19bdee3");
        assertThat(file.getFileAccess()).isEqualTo("open");
        assertThat(file.getFileType()).isEqualTo("BAI");
        assertInfoKVPair(file, "extraFileInfo", "some more data for sequencingRead file_fn3");
      } else {
        fail(format("the fileName %s is not recognized", file.getFileName()));
      }
    }
  }

  @Ignore // When mvn runs this test, we get three files, not two. IntelliJ doesn't.
  @Test
  public void testRead() {
    // test sequencing read
    val id1="AN1";
    val analysis1 = service.read(id1);
    assertThat(analysis1.getAnalysisId()).isEqualTo("AN1");
    assertThat(analysis1.getAnalysisType()).isEqualTo("variantCall");
    assertThat(analysis1.getStudy()).isEqualTo(DEFAULT_STUDY_ID);
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
            .withStatus(OK.value())
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
    val study=DEFAULT_STUDY_ID;
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
