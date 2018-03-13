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

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.icgc.dcc.song.core.utils.JsonUtils;
import org.icgc.dcc.song.core.utils.RandomGenerator;
import org.icgc.dcc.song.server.model.Metadata;
import org.icgc.dcc.song.server.model.analysis.Analysis;
import org.icgc.dcc.song.server.model.analysis.SequencingReadAnalysis;
import org.icgc.dcc.song.server.model.analysis.VariantCallAnalysis;
import org.icgc.dcc.song.server.model.entity.File;
import org.icgc.dcc.song.server.model.entity.Sample;
import org.icgc.dcc.song.server.model.entity.Study;
import org.icgc.dcc.song.server.repository.AnalysisRepository;
import org.icgc.dcc.song.server.repository.SampleRepository;
import org.icgc.dcc.song.server.utils.AnalysisGenerator;
import org.icgc.dcc.song.server.utils.PayloadGenerator;
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

import java.util.ArrayList;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.ANALYSIS_ID_NOT_FOUND;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.ANALYSIS_MISSING_FILES;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.ANALYSIS_MISSING_SAMPLES;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.DUPLICATE_ANALYSIS_ATTEMPT;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.SEQUENCING_READ_NOT_FOUND;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.STUDY_ID_DOES_NOT_EXIST;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.UNPUBLISHED_FILE_IDS;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.VARIANT_CALL_NOT_FOUND;
import static org.icgc.dcc.song.core.testing.SongErrorAssertions.assertSongError;
import static org.icgc.dcc.song.core.utils.JsonUtils.toJson;
import static org.icgc.dcc.song.core.utils.RandomGenerator.createRandomGenerator;
import static org.icgc.dcc.song.server.model.enums.AnalysisStates.UNPUBLISHED;
import static org.icgc.dcc.song.server.repository.search.IdSearchRequest.createIdSearchRequest;
import static org.icgc.dcc.song.server.service.ExistenceService.createExistenceService;
import static org.icgc.dcc.song.server.utils.AnalysisGenerator.createAnalysisGenerator;
import static org.icgc.dcc.song.server.utils.PayloadGenerator.createPayloadGenerator;
import static org.icgc.dcc.song.server.utils.TestConstants.DEFAULT_STUDY_ID;
import static org.icgc.dcc.song.server.utils.TestFiles.getInfoName;
import static org.springframework.http.HttpStatus.OK;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class})
@ActiveProfiles({"dev", "test"})
public class AnalysisServiceTest {

  private static final String FILEPATH = "src/test/resources/fixtures/";
  private static final String TEST_FILEPATH = "src/test/resources/documents/";

  @Rule
  public WireMockRule wireMockRule = new WireMockRule(options().dynamicPort());

  @Autowired
  FileService fileService;
  @Autowired
  AnalysisService service;
  @Autowired
  IdService idService;
  @Autowired
  private StudyService studyService;
  @Autowired
  private SampleRepository sampleRepository;
  @Autowired
  private AnalysisRepository analysisRepository;

  private final RandomGenerator randomGenerator = createRandomGenerator(AnalysisServiceTest.class.getSimpleName());

  private PayloadGenerator payloadGenerator;
  private AnalysisGenerator analysisGenerator;

  @Autowired
  private RetryTemplate retryTemplate;

  /**
   * This is dirty, but since the existenceService is so easy to construct
   * and the storage url port is randomly assigned, it's worth it.
   */
  @Before
  public void init(){
    this.payloadGenerator = createPayloadGenerator(randomGenerator);
    this.analysisGenerator = createAnalysisGenerator(DEFAULT_STUDY_ID, service, payloadGenerator);
    val testStorageUrl = format("http://localhost:%s", wireMockRule.port());
    val testExistenceService = createExistenceService(retryTemplate,testStorageUrl);
    ReflectionTestUtils.setField(service, "existence", testExistenceService);
    log.info("ExistenceService configured to endpoint: {}",testStorageUrl );
  }

  @Test
  public void testCreateAndUpdate() {
    val created = analysisGenerator.createDefaultRandomSequencingReadAnalysis();
    val analysisId = created.getAnalysisId();
    assertThat(created.getAnalysisId()).isEqualTo(analysisId);
    assertThat(created.getAnalysisState()).isEqualTo("UNPUBLISHED");
    assertThat(created.getAnalysisType()).isEqualTo("sequencingRead");
    assertThat(created.getSample().size()).isEqualTo(1);
    val sample = created.getSample().get(0);
    val experiment = ((SequencingReadAnalysis) created).getExperiment();
    assertThat(experiment).isNotNull();
    assertThat(experiment.getAlignmentTool().equals("BigWrench"));
    val expectedMetadata = new Metadata();
    expectedMetadata.setInfo("marginOfError", "0.01%");
    assertThat(experiment.getInfo()).isEqualTo(expectedMetadata.getInfo());

;    // test update
    val change="ModifiedToolName";
    experiment.setAlignmentTool(change);
    service.updateAnalysis(DEFAULT_STUDY_ID, created);
    val gotBack = service.read(analysisId);
    val experiment2 =((SequencingReadAnalysis)gotBack).getExperiment();
    assertThat(experiment2.getAlignmentTool() ).isEqualTo(change);

    log.info(format("Created '%s'",toJson(created)));
  }

  @Test
  public void testIsAnalysisExist(){
    val analysis = analysisGenerator.createDefaultRandomVariantCallAnalysis();
    val randomAnalysisId = randomGenerator.generateRandomUUID().toString();
    analysis.setAnalysisId(randomAnalysisId);
    assertThat(service.doesAnalysisIdExist(randomAnalysisId)).isFalse();
    val actualAnalysisId = service.create(DEFAULT_STUDY_ID, analysis, false);
    assertThat(actualAnalysisId).isEqualTo(randomAnalysisId);
    assertThat(service.doesAnalysisIdExist(randomAnalysisId)).isTrue();
  }

  @Test
  public void testCreateAndUpdateVariantCall() {
    val created = analysisGenerator.createRandomAnalysis(VariantCallAnalysis.class,
        "documents/variantcall-valid-1.json");
    val analysisId = created.getAnalysisId();
    assertThat(created.getAnalysisId()).isEqualTo(analysisId);
    assertThat(created.getAnalysisState()).isEqualTo(UNPUBLISHED.toString());
    assertThat(created.getAnalysisType()).isEqualTo("variantCall");
    assertThat(created.getSample().size()).isEqualTo(1);
    val sample = created.getSample().get(0);
    val experiment = ((VariantCallAnalysis) created).getExperiment();
    assertThat(experiment).isNotNull();
    assertThat(experiment.getVariantCallingTool()).isEqualTo("silver bullet");
    assertThat(experiment.getInfoAsString()).isEqualTo(
            JsonUtils.fromSingleQuoted("{\"extraInfo\":\"this is extra info\"}"));
    // test update
    val change="GoldenHammer";
    experiment.setVariantCallingTool(change) ;
    service.updateAnalysis(DEFAULT_STUDY_ID, created);
    val gotBack = service.read(analysisId);
    val experiment2 =((VariantCallAnalysis)gotBack).getExperiment();
    assertThat(experiment2.getVariantCallingTool()).isEqualTo(change);

    log.info(format("Created '%s'",toJson(created)));
  }

  @Test
  public void testReadAnalysisDNE() {
    val nonExistentAnalysisId = randomGenerator.generateRandomUUID().toString();
    assertThat(service.doesAnalysisIdExist(nonExistentAnalysisId)).isFalse();
    assertSongError(() -> service.read(nonExistentAnalysisId), ANALYSIS_ID_NOT_FOUND);
  }

  @Test
  public void testReadVariantCallDNE() {
    assertThat(studyService.isStudyExist(DEFAULT_STUDY_ID)).isTrue();

    val analysis = analysisGenerator.createDefaultRandomVariantCallAnalysis();
    val analysisId = analysis.getAnalysisId();

    analysisRepository.deleteVariantCall(analysisId);
    assertThat(analysisRepository.readVariantCall(analysisId)).isNull();
    assertSongError(() -> service.read(analysisId), VARIANT_CALL_NOT_FOUND);
  }

  @Test
  public void testReadSequencingReadDNE() {
    assertThat(studyService.isStudyExist(DEFAULT_STUDY_ID)).isTrue();

    val analysis = analysisGenerator.createDefaultRandomSequencingReadAnalysis();
    val analysisId = analysis.getAnalysisId();

    analysisRepository.deleteSequencingRead(analysisId);
    assertThat(analysisRepository.readSequencingRead(analysisId)).isNull();
    assertSongError(() -> service.read(analysisId), SEQUENCING_READ_NOT_FOUND);
  }

  @Ignore
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
    assertSongError(() -> service.publish(token, id), UNPUBLISHED_FILE_IDS, null);
  }


  @Test
  public void testSuppress() {
    val an = analysisGenerator.createDefaultRandomAnalysis(SequencingReadAnalysis.class);
    assertThat(an.getAnalysisState()).isEqualTo("UNPUBLISHED");
    val id = an.getAnalysisId();
    service.suppress(id);

    val analysis = service.read(id);
    assertThat(analysis.getAnalysisState()).isEqualTo("SUPPRESSED");
  }

  @Test
  public void testReadFiles() {
    val files = service.readFiles("AN1");
    System.err.printf("Got files '%s'", files);
    val expectedFiles = new ArrayList<File>();

    expectedFiles.add(fileService.read("FI1"));
    expectedFiles.add(fileService.read("FI2"));

    assertThat(files).containsAll(expectedFiles);
    assertThat(expectedFiles).containsAll(files);
  }

  @Test
  public void testDuplicateAnalysisAttemptError() {
    val an1 = service.read("AN1");
    assertSongError(() -> service.create(an1.getStudy(), an1, true),
        DUPLICATE_ANALYSIS_ATTEMPT);
  }

  @Test
  public void testCustomAnalysisId(){
    val study= DEFAULT_STUDY_ID;
    val expectedAnalysisId = "AN-1234";
    val expectedObjectIdMap = Maps.newHashMap();
    expectedObjectIdMap.put("a3bc0998a-3521-43fd-fa10-a834f3874e46.MUSE_1-0rc-vcf.20170711.somatic.snv_mnv.vcf.gz", "0794ae66-80df-5b70-bc22-e49309bfba2a");
    expectedObjectIdMap.put("a3bc0998a-3521-43fd-fa10-a834f3874e46.MUSE_1-0rc-vcf.20170711.somatic.snv_mnv.vcf.gz.idx", "a2449e0a-7020-5f2d-8610-9f58aafd467a" );

    val analysisRaw = payloadGenerator.generateRandomPayload(SequencingReadAnalysis.class,"documents/sequencingread-custom-analysis-id"
        + ".json");
    analysisRaw.setAnalysisId(expectedAnalysisId);
    val actualAnalysisId = service.create(study, analysisRaw, false);
    assertThat(actualAnalysisId).isEqualTo(expectedAnalysisId);
    val analysis = service.read(actualAnalysisId);
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

  @Test
  public void testCreateAnalysisStudyDNE(){
    val nonExistentStudyId = randomGenerator.generateRandomUUID().toString();
    assertThat(studyService.isStudyExist(nonExistentStudyId)).isFalse();

    val payload = payloadGenerator.generateDefaultRandomPayload(VariantCallAnalysis.class);
    payload.setAnalysisId(null);

    assertThat(payload.getAnalysisId()).isNull();
    assertSongError(() -> service.create(nonExistentStudyId, payload, false), STUDY_ID_DOES_NOT_EXIST);
  }

  @Test
  public void testGetAnalysisEmptyStudy(){
    val nonExistentStudyId = randomGenerator.generateRandomAsciiString(10);
    assertThat(studyService.isStudyExist(nonExistentStudyId)).isFalse();
    studyService.saveStudy(Study.create(nonExistentStudyId,"","",""));
    assertThat(service.getAnalysis(nonExistentStudyId)).isEmpty();
  }

  @Test
  public void testIdSearchEmptyStudy(){
    val nonExistentStudyId = randomGenerator.generateRandomAsciiString(10);
    assertThat(studyService.isStudyExist(nonExistentStudyId)).isFalse();
    studyService.saveStudy(Study.create(nonExistentStudyId,"","",""));
    val idSearchRequest = createIdSearchRequest(null, null, null, null);
    assertThat(service.idSearch(nonExistentStudyId, idSearchRequest)).isEmpty();
  }

  @Test
  public void testGetAnalysisDNEStudy() {
    val nonExistentStudyId = randomGenerator.generateRandomAsciiString(12);
    assertSongError(() -> service.getAnalysis(nonExistentStudyId), STUDY_ID_DOES_NOT_EXIST);
  }

  @Test
  public void testIdSearchDNEStudy(){
    val nonExistentStudyId = randomGenerator.generateRandomAsciiString(12);
    val idSearchRequest = createIdSearchRequest(null, null, null, null);
    assertSongError(() -> service.idSearch(nonExistentStudyId, idSearchRequest), STUDY_ID_DOES_NOT_EXIST);
  }

  @Test
  public void testAnalysisMissingFilesException(){
    val studyId = DEFAULT_STUDY_ID;
    assertThat(studyService.isStudyExist(studyId)).isTrue();

    val analysis1 = analysisGenerator.createDefaultRandomSequencingReadAnalysis();
    val analysisId1 = analysis1.getAnalysisId();

    analysisRepository.deleteFiles(analysisId1);
    assertThat(analysisRepository.readFiles(analysisId1)).isEmpty();
    assertSongError(() -> service.readFiles(analysisId1), ANALYSIS_MISSING_FILES);

    val analysis2 = analysisGenerator.createDefaultRandomVariantCallAnalysis();
    val analysisId2 = analysis2.getAnalysisId();
    analysisRepository.deleteFiles(analysisId2);
    assertThat(analysisRepository.readFiles(analysisId2)).isEmpty();
    assertSongError(() -> service.readFiles(analysisId2), ANALYSIS_MISSING_FILES);
  }

  @Test
  public void testSequencingReadAnalysisMissingSamplesException() {
    runAnalysisMissingSamplesTest(SequencingReadAnalysis.class);
  }

  @Test
  public void testVariantCallAnalysisMissingSamplesException() {
    runAnalysisMissingSamplesTest(VariantCallAnalysis.class);
  }

  @Test
  public void testAnalysisIdDneException(){
    val nonExistentAnalysisId = randomGenerator.generateRandomUUID().toString();
    assertSongError(() -> service.read(nonExistentAnalysisId), ANALYSIS_ID_NOT_FOUND);
    assertSongError(() -> service.readFiles(nonExistentAnalysisId), ANALYSIS_ID_NOT_FOUND);
    assertSongError(() -> service.readSamples(nonExistentAnalysisId), ANALYSIS_ID_NOT_FOUND);
  }

  private void runAnalysisMissingSamplesTest(Class<? extends Analysis> analysisClass) {
    assertThat(studyService.isStudyExist(DEFAULT_STUDY_ID)).isTrue();

    // Create random analysis,
    val analysis = analysisGenerator.createDefaultRandomAnalysis(analysisClass);
    val analysisId = analysis.getAnalysisId();

    analysisRepository.deleteCompositeEntities(analysisId);
    analysis.getSample().stream()
        .map(Sample::getSampleId)
        .forEach(sampleRepository::delete);
    assertSongError(() -> service.readSamples(analysisId), ANALYSIS_MISSING_SAMPLES);
  }

}
