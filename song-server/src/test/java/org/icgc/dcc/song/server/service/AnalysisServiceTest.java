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
import com.google.common.collect.Maps;
import lombok.SneakyThrows;
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
import org.icgc.dcc.song.server.utils.TestFiles;
import org.junit.Ignore;
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

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.ANALYSIS_ID_NOT_FOUND;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.ANALYSIS_MISSING_FILES;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.ANALYSIS_MISSING_SAMPLES;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.DUPLICATE_ANALYSIS_ATTEMPT;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.SEQUENCING_READ_NOT_FOUND;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.STUDY_ID_DOES_NOT_EXIST;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.VARIANT_CALL_NOT_FOUND;
import static org.icgc.dcc.song.core.testing.SongErrorAssertions.assertSongError;
import static org.icgc.dcc.song.core.utils.JsonUtils.fromJson;
import static org.icgc.dcc.song.core.utils.JsonUtils.toJson;
import static org.icgc.dcc.song.core.utils.RandomGenerator.createRandomGenerator;
import static org.icgc.dcc.song.server.repository.search.IdSearchRequest.createIdSearchRequest;
import static org.icgc.dcc.song.server.utils.TestFiles.getInfoName;
import static org.icgc.dcc.song.server.utils.TestFiles.getJsonStringFromClasspath;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class})
@ActiveProfiles({"dev", "test"})
public class AnalysisServiceTest {

  private static final String FILEPATH = "src/test/resources/fixtures/";
  private static final String DEFAULT_STUDY_ID = "ABC123";


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

  @SneakyThrows
  private String readFile(String name) {
    return new String(Files.readAllBytes(new java.io.File("..", name).toPath()));
  }

  @Test
  public void testCreateAndUpdate() {
    val json = getJsonStringFromClasspath("documents/sequencingread-valid-1.json");
    val analysis = fromJson(json, Analysis.class);
    val analysisId=service.create(DEFAULT_STUDY_ID, analysis, false);

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
    service.updateAnalysis(DEFAULT_STUDY_ID, created);
    val gotBack = service.read(analysisId);
    val experiment2 =((SequencingReadAnalysis)gotBack).getExperiment();
    assertThat(experiment2.getAlignmentTool() ).isEqualTo(change);

    log.info(format("Created '%s'",toJson(created)));
  }

  @Test
  public void testIsAnalysisExist(){
    val json = getJsonStringFromClasspath("documents/variantcall-valid-1.json");
    val analysis = fromJson(json, Analysis.class);
    val randomAnalysisId = randomGenerator.generateRandomUUID().toString();
    analysis.setAnalysisId(randomAnalysisId);
    assertThat(service.doesAnalysisIdExist(randomAnalysisId)).isFalse();
    val actualAnalysisId = service.create(DEFAULT_STUDY_ID, analysis, false);
    assertThat(actualAnalysisId).isEqualTo(randomAnalysisId);
    assertThat(service.doesAnalysisIdExist(randomAnalysisId)).isTrue();
  }

  @Test
  public void testCreateAndUpdateVariantCall() {
    val json = getJsonStringFromClasspath("documents/variantcall-valid-2.json");
    val analysis = fromJson(json, Analysis.class);
    assertThat(service.doesAnalysisIdExist(analysis.getAnalysisId())).isFalse();
    val analysisId=service.create(DEFAULT_STUDY_ID, analysis, false);
    assertThat(service.doesAnalysisIdExist(analysis.getAnalysisId())).isTrue();

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
    val json = getJsonStringFromClasspath("documents/variantcall-valid.json");
    val analysis = fromJson(json, Analysis.class);
    assertThat(studyService.isStudyExist(DEFAULT_STUDY_ID)).isTrue();
    val analysisId = service.create(DEFAULT_STUDY_ID, analysis, false);
    analysisRepository.deleteVariantCall(analysisId);
    assertThat(analysisRepository.readVariantCall(analysisId)).isNull();
    assertSongError(() -> service.read(analysisId), VARIANT_CALL_NOT_FOUND);
  }

  @Test
  public void testReadSequencingReadDNE() {
    val json = getJsonStringFromClasspath("documents/sequencingread-valid.json");
    val analysis = fromJson(json, Analysis.class);
    assertThat(studyService.isStudyExist(DEFAULT_STUDY_ID)).isTrue();
    val analysisId = service.create(DEFAULT_STUDY_ID, analysis, false);
    analysisRepository.deleteSequencingRead(analysisId);
    assertThat(analysisRepository.readSequencingRead(analysisId)).isNull();
    assertSongError(() -> service.read(analysisId), SEQUENCING_READ_NOT_FOUND);
  }

  @Test
  public void testRead() {
    // test sequencing read
    val json = getJsonStringFromClasspath("documents/variantcall-valid-3.json");
    val analysisRaw = fromJson(json, Analysis.class);
    val randomAnId1 = randomGenerator.generateRandomUUID().toString();
    analysisRaw.setAnalysisId(randomAnId1);
    val id1 = service.create(DEFAULT_STUDY_ID, analysisRaw, false);
    val analysis1 = service.read(id1);
    assertThat(analysis1.getAnalysisId()).isEqualTo(randomAnId1);
    assertThat(analysis1.getAnalysisType()).isEqualTo("variantCall");
    assertThat(analysis1.getAnalysisState()).isEqualTo("UNPUBLISHED");
    assertThat(analysis1.getStudy()).isEqualTo("ABC123");
    assertThat(analysis1.getSample().size()).isEqualTo(2);
    assertThat(analysis1.getFile().size()).isEqualTo(2);
    assertThat(analysis1).isInstanceOf(VariantCallAnalysis.class);

    val experiment1 = ((VariantCallAnalysis) analysis1).getExperiment();
    assertThat(experiment1).isNotNull();
    assertThat(experiment1.getVariantCallingTool()).isEqualTo("SuperNewVariantCallingTool");
    assertThat(experiment1.getMatchedNormalSampleSubmitterId()).isEqualTo("myMatchedNormalSampleSubmitterId");
    assertThat(experiment1.getAnalysisId()).isEqualTo(id1);

    assertThat(getInfoName(analysis1)).isEqualTo("analysis1");
    assertThat(getInfoName(experiment1)).isEqualTo("variantCall1");

    // test variant call
    val json2 = getJsonStringFromClasspath("documents/sequencingread-valid-2.json");
    val analysisRaw2 = fromJson(json, Analysis.class);
    val randomAnId2 = randomGenerator.generateRandomUUID().toString();
    analysisRaw2.setAnalysisId(randomAnId2);
    val id2 = service.create(DEFAULT_STUDY_ID, analysisRaw2, false);
    val analysis2 = service.read(id2);
    assertThat(analysis2.getAnalysisId()).isEqualTo(randomAnId2);
    assertThat(analysis2.getAnalysisState()).isEqualTo("UNPUBLISHED");
    assertThat(analysis2.getAnalysisType()).isEqualTo("sequencingRead");
    assertThat(analysis2.getFile().size()).isEqualTo(2);
    assertThat(analysis2.getSample().size()).isEqualTo(1);
    assertThat(analysis2).isInstanceOf(SequencingReadAnalysis.class);

    val experiment2 = ((SequencingReadAnalysis) analysis2).getExperiment();
    assertThat(experiment2).isNotNull();
    assertThat(experiment2.getAligned()).isEqualTo(true);
    assertThat(experiment2.getAnalysisId()).isEqualTo(id2);
    assertThat(experiment2.getInsertSize()).isEqualTo(12345);
    assertThat(experiment2.getLibraryStrategy()).isEqualTo("Other");
    assertThat(experiment2.getPairedEnd()).isEqualTo(true);
    assertThat(experiment2.getReferenceGenome()).isEqualTo("hg19");
    assertThat(experiment2.getAlignmentTool()).isEqualTo("BigWrench");

    assertThat(getInfoName(analysis2)).isEqualTo("analysis2");
    assertThat(getInfoName(experiment2)).isEqualTo("sequencingRead2");
  }

  update all references to AN1 and AN2 to read from payload files

  @Ignore
  @Test
  public void testPublish() {
    // TODO: Figure out how to test this
    val token = "mockToken";
    val id = "AN1";
    service.publish(token, id);

    val analysis = service.read(id);
    assertThat(analysis.getAnalysisState()).isEqualTo("PUBLISHED");
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

  @Test
  public void testCreateAnalysisStudyDNE(){
    val nonExistentStudyId = randomGenerator.generateRandomAsciiString(12);
    assertThat(studyService.isStudyExist(nonExistentStudyId)).isFalse();
    val json = readFile(FILEPATH + "variantCall.json");
    val analysis = fromJson(json, Analysis.class);
    assertThat(service.doesAnalysisIdExist(analysis.getAnalysisId())).isFalse();
    assertSongError(() -> service.create(nonExistentStudyId, analysis, false), STUDY_ID_DOES_NOT_EXIST);
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
    val studyId = "ABC123";
    assertThat(studyService.isStudyExist(studyId)).isTrue();

    val json1 = getJsonStringFromClasspath("documents/sequencingread-valid.json");
    val analysis1 = fromJson(json1, Analysis.class);
    val analysisId1 = service.create(studyId, analysis1, false);
    analysisRepository.deleteFiles(analysisId1);
    assertThat(analysisRepository.readFiles(analysisId1)).isEmpty();
    assertSongError(() -> service.readFiles(analysisId1), ANALYSIS_MISSING_FILES);

    val json2 = getJsonStringFromClasspath("documents/variantcall-valid.json");
    val analysis2 = fromJson(json2, Analysis.class);
    val analysisId2 = service.create(studyId, analysis2, false);
    analysisRepository.deleteFiles(analysisId2);
    assertThat(analysisRepository.readFiles(analysisId2)).isEmpty();
    assertSongError(() -> service.readFiles(analysisId2), ANALYSIS_MISSING_FILES);
  }

  @Test
  public void testAnalysisMissingSamplesException(){
    val studyId = "ABC123";
    assertThat(studyService.isStudyExist(studyId)).isTrue();

    val json1 = getJsonStringFromClasspath("documents/sequencingread-valid.json");
    val analysis1 = fromJson(json1, Analysis.class);
    val analysisId1 = service.create(studyId, analysis1, false);
    val analysisObj1 = service.read(analysisId1);
    analysisRepository.deleteCompositeEntities(analysisId1);
    analysisObj1.getSample().stream()
        .map(Sample::getSampleId)
        .forEach(sampleRepository::delete);
    assertSongError(() -> service.readSamples(analysisId1), ANALYSIS_MISSING_SAMPLES);

    val json2 = getJsonStringFromClasspath("documents/variantcall-valid.json");
    val analysis2 = fromJson(json2, Analysis.class);
    analysis2.getSample().forEach(x -> {
      x.setSampleSubmitterId(randomGenerator.generateRandomUUID().toString());
      x.getSpecimen().setSpecimenSubmitterId(randomGenerator.generateRandomUUID().toString());
      x.getDonor().setDonorSubmitterId(randomGenerator.generateRandomUUID().toString());
    });
    val analysisId2 = service.create(studyId, analysis2, false);
    val analysisObj2 = service.read(analysisId2);
    analysisRepository.deleteCompositeEntities(analysisId2);
    analysisObj2.getSample().stream()
        .map(Sample::getSampleId)
        .forEach(sampleRepository::delete);
    assertSongError(() -> service.readSamples(analysisId2), ANALYSIS_MISSING_SAMPLES);
  }

  @Test
  public void testAnalysisIdDneException(){
    val nonExistentAnalysisId = randomGenerator.generateRandomUUID().toString();
    assertSongError(() -> service.read(nonExistentAnalysisId), ANALYSIS_ID_NOT_FOUND);
    assertSongError(() -> service.readFiles(nonExistentAnalysisId), ANALYSIS_ID_NOT_FOUND);
    assertSongError(() -> service.readSamples(nonExistentAnalysisId), ANALYSIS_ID_NOT_FOUND);
  }

}
