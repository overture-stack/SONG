/*
 * Copyright (c) 2019. Ontario Institute for Cancer Research
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

import static bio.overture.song.core.exceptions.ServerErrors.*;
import static bio.overture.song.core.model.enums.AnalysisStates.*;
import static bio.overture.song.core.testing.SongErrorAssertions.assertCollectionsMatchExactly;
import static bio.overture.song.core.testing.SongErrorAssertions.assertSongError;
import static bio.overture.song.core.utils.JsonUtils.fromJson;
import static bio.overture.song.core.utils.RandomGenerator.createRandomGenerator;
import static bio.overture.song.server.repository.search.IdSearchRequest.createIdSearchRequest;
import static bio.overture.song.server.utils.TestFiles.assertInfoKVPair;
import static bio.overture.song.server.utils.TestFiles.getJsonStringFromClasspath;
import static bio.overture.song.server.utils.generator.AnalysisGenerator.createAnalysisGenerator;
import static bio.overture.song.server.utils.generator.LegacyAnalysisTypeName.SEQUENCING_READ;
import static bio.overture.song.server.utils.generator.LegacyAnalysisTypeName.VARIANT_CALL;
import static bio.overture.song.server.utils.generator.PayloadGenerator.createPayloadGenerator;
import static bio.overture.song.server.utils.generator.StudyGenerator.createStudyGenerator;
import static bio.overture.song.server.utils.securestudy.impl.SecureAnalysisTester.createSecureAnalysisTester;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.Arrays.stream;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.*;
import static java.util.stream.IntStream.range;
import static org.junit.Assert.*;

import bio.overture.song.core.exceptions.ServerException;
import bio.overture.song.core.model.enums.AnalysisStates;
import bio.overture.song.core.testing.SongErrorAssertions;
import bio.overture.song.core.utils.RandomGenerator;
import bio.overture.song.server.model.analysis.Analysis;
import bio.overture.song.server.model.analysis.AnalysisData;
import bio.overture.song.server.model.analysis.AnalysisStateChange;
import bio.overture.song.server.model.dto.Payload;
import bio.overture.song.server.model.entity.FileEntity;
import bio.overture.song.server.repository.AnalysisRepository;
import bio.overture.song.server.repository.FileRepository;
import bio.overture.song.server.service.analysis.AnalysisServiceImpl;
import bio.overture.song.server.service.id.IdService;
import bio.overture.song.server.utils.TestAnalysis;
import bio.overture.song.server.utils.generator.AnalysisGenerator;
import bio.overture.song.server.utils.generator.LegacyAnalysisTypeName;
import bio.overture.song.server.utils.generator.PayloadGenerator;
import bio.overture.song.server.utils.generator.StudyGenerator;
import bio.overture.song.server.utils.securestudy.impl.SecureAnalysisTester;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;
import javax.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
public class AnalysisServiceTest {

  private static final String ID_SERVICE = "idService";
  private static final String DEFAULT_STUDY_ID = "ABC123";
  private static final String DEFAULT_ANALYSIS_ID = "AN1";
  private static final Set<String> PUBLISHED_ONLY = ImmutableSet.of(PUBLISHED.toString());
  private static final Set<String> ALL_STATES =
      stream(AnalysisStates.values()).map(AnalysisStates::toString).collect(toImmutableSet());

  @Autowired FileService fileService;
  @Autowired AnalysisServiceImpl service;
  @Autowired IdService idService;
  @Autowired private StudyService studyService;
  @Autowired private AnalysisRepository analysisRepository;
  @Autowired private FileRepository fileRepository;
  @Autowired private ExportService exportService;

  private final RandomGenerator randomGenerator =
      createRandomGenerator(AnalysisServiceTest.class.getSimpleName());

  private PayloadGenerator payloadGenerator;
  private AnalysisGenerator analysisGenerator;
  private StudyGenerator studyGenerator;
  private SecureAnalysisTester secureAnalysisTester;

  @Before
  public void beforeTest() {
    assertTrue(studyService.isStudyExist(DEFAULT_STUDY_ID));
    assertTrue(service.isAnalysisExist(DEFAULT_ANALYSIS_ID));
  }

  /**
   * This is dirty, but since the existenceService is so easy to construct and the storage url port
   * is randomly assigned, it's worth it.
   */
  @Before
  public void init() {
    this.payloadGenerator = createPayloadGenerator(randomGenerator);
    this.analysisGenerator = createAnalysisGenerator(DEFAULT_STUDY_ID, service, randomGenerator);
    this.studyGenerator = createStudyGenerator(studyService, randomGenerator);
    this.secureAnalysisTester = createSecureAnalysisTester(randomGenerator, studyService, service);
  }

  @Test
  @Transactional
  public void testReadState() {
    val a = service.securedDeepRead(DEFAULT_STUDY_ID, DEFAULT_ANALYSIS_ID);
    val expectedState = resolveAnalysisState(a.getAnalysisState());
    val actualState = service.readState(a.getAnalysisId());
    assertEquals(actualState, expectedState);
  }

  @Test
  @Transactional
  public void testCreateAndUpdate() {
    val created = analysisGenerator.createDefaultRandomSequencingReadAnalysis();
    val analysisId = created.getAnalysisId();
    assertEquals(created.getAnalysisId(), analysisId);
    assertEquals(created.getAnalysisState(), "UNPUBLISHED");
    assertEquals(created.getAnalysisSchema().getName(), "sequencingRead");
    val data = created.getAnalysisData().getData();

    assertTrue(data.has("experiment"));
    assertEquals(
        data.get("experiment").get("alignmentTool").textValue(), "MUSE variant call pipeline");
    assertTrue(data.path("experiment").hasNonNull("info"));
    assertEquals(data.path("experiment").path("info").get("marginOfError").textValue(), "0.01%");
  }

  @Test
  @Transactional
  public void testCreateAndUpdateVariantCall() {
    val created = analysisGenerator.createRandomAnalysis("documents/variantcall-valid-1.json");
    val analysisId = created.getAnalysisId();
    assertEquals(created.getAnalysisId(), analysisId);
    assertEquals(created.getAnalysisState(), UNPUBLISHED.toString());
    assertEquals(created.getAnalysisSchema().getName(), "variantCall");
    assertTrue(created.getAnalysisData().getData().has("experiment"));
    assertEquals(extractVariantCallingTool(created.getAnalysisData()), "silver bullet");
    assertTrue(created.getAnalysisData().getData().path("experiment").has("extraInfo"));
    assertEquals(
        created.getAnalysisData().getData().path("experiment").path("extraInfo").textValue(),
        "this is extra info");
  }

  private static JsonNode extractExperiment(AnalysisData a) {
    return a.getData().path("experiment");
  }

  private static String extractVariantCallingTool(AnalysisData a) {
    return extractExperiment(a).path("variantCallingTool").textValue();
  }

  private static String extractAlignmentTool(AnalysisData a) {
    return extractExperiment(a).path("alignmentTool").textValue();
  }

  @Test
  @Transactional
  public void testReadAnalysisDNE() {
    val nonExistentAnalysisId = analysisGenerator.generateNonExistingAnalysisId();
    assertSongError(
        () -> service.securedDeepRead(DEFAULT_STUDY_ID, nonExistentAnalysisId),
        ANALYSIS_ID_NOT_FOUND);
    assertSongError(() -> service.unsecuredDeepRead(nonExistentAnalysisId), ANALYSIS_ID_NOT_FOUND);
  }

  @Test
  @Transactional
  public void testReadVariantCall() {
    val json = getJsonStringFromClasspath("documents/variantcall-read-test.json");
    val payload = fromJson(json, Payload.class);
    val analysis = service.create(DEFAULT_STUDY_ID, payload);
    val analysisId = analysis.getAnalysisId();
    val a = service.securedDeepRead(DEFAULT_STUDY_ID, analysisId);
    val aUnsecured = service.unsecuredDeepRead(analysisId);
    assertEquals(a, aUnsecured);

    // Asserting Analysis
    assertEquals(
        TestAnalysis.extractString(a, "info", "description1"),
        "description1 for this variantCall analysis an01");
    assertEquals(
        TestAnalysis.extractString(a, "info", "description2"),
        "description2 for this variantCall analysis an01");
    assertEquals(
        TestAnalysis.extractString(a, "experiment", "variantCallingTool"), "silver bullet ex01");
    assertEquals(
        TestAnalysis.extractString(a, "experiment", "extraExperimentInfo"),
        "some more data for a variantCall experiment ex01");

    assertEquals(a.getFiles().size(), 3);
    val file0 = a.getFiles().get(0);
    val file1 = a.getFiles().get(1);
    val file2 = a.getFiles().get(2);
    assertEquals(file0.getAnalysisId(), analysisId);
    assertEquals(file1.getAnalysisId(), analysisId);
    assertEquals(file2.getAnalysisId(), analysisId);
    assertEquals(file0.getStudyId(), DEFAULT_STUDY_ID);
    assertEquals(file1.getStudyId(), DEFAULT_STUDY_ID);
    assertEquals(file2.getStudyId(), DEFAULT_STUDY_ID);

    val fileName0 =
        "a3bc0998a-3521-43fd-fa10-a834f3874e46-fn1.MUSE_1-0rc-vcf.20170711.somatic.snv_mnv.vcf.gz";
    val fileName1 =
        "a3bc0998a-3521-43fd-fa10-a834f3874e46-fn2.MUSE_1-0rc-vcf.20170711.somatic.snv_mnv.vcf.gz";
    val fileName2 =
        "a3bc0998a-3521-43fd-fa10-a834f3874e46-fn3.MUSE_1-0rc-vcf.20170711.somatic.snv_mnv.vcf.gz.idx";

    for (val file : a.getFiles()) {
      if (file.getFileName().equals(fileName0)) {
        assertEquals(file.getFileName(), fileName0);
        assertEquals(file.getFileSize().longValue(), 376953L);
        assertEquals(file.getFileMd5sum(), "652b2e2b7133229a89650de27ad7fc41");
        assertEquals(file.getFileAccess(), "controlled");
        assertEquals(file.getFileType(), "VCF");
        assertInfoKVPair(file, "extraFileInfo_0", "first data for variantCall file_fn1");
        assertInfoKVPair(file, "extraFileInfo_1", "second data for variantCall file_fn1");
      } else if (file.getFileName().equals(fileName1)) {
        assertEquals(file.getFileName(), fileName1);
        assertEquals(file.getFileSize().longValue(), 983820L);
        assertEquals(file.getFileMd5sum(), "b8b743a499e461922accad58fdbf25d2");
        assertEquals(file.getFileAccess(), "open");
        assertEquals(file.getFileType(), "VCF");
        assertInfoKVPair(file, "extraFileInfo", "some more data for variantCall file_fn2");

      } else if (file.getFileName().equals(fileName2)) {
        assertEquals(file.getFileName(), fileName2);
        assertEquals(file.getFileSize().longValue(), 4840L);
        assertEquals(file.getFileMd5sum(), "2b80298c2f312df7db482105053f889b");
        assertEquals(file.getFileAccess(), "open");
        assertEquals(file.getFileType(), "IDX");
        assertInfoKVPair(file, "extraFileInfo", "some more data for variantCall file_fn3");
      } else {
        Assert.fail(String.format("the fileName %s is not recognized", file.getFileName()));
      }
    }
  }

  @Test
  @Transactional
  public void testReadSequencingRead() {
    val json = getJsonStringFromClasspath("documents/sequencingread-read-test.json");
    val payload = fromJson(json, Payload.class);
    val analysis = service.create(DEFAULT_STUDY_ID, payload);
    val analysisId = analysis.getAnalysisId();
    val a = service.securedDeepRead(DEFAULT_STUDY_ID, analysisId);
    val aUnsecured = service.unsecuredDeepRead(analysisId);
    assertEquals(a, aUnsecured);

    // Asserting Analysis
    assertEquals(a.getAnalysisState(), "UNPUBLISHED");
    assertEquals(a.getAnalysisSchema().getName(), "sequencingRead");
    assertEquals(a.getStudyId(), DEFAULT_STUDY_ID);
    assertEquals(
        TestAnalysis.extractString(a, "info", "description1"),
        "description1 for this sequencingRead analysis an01");
    assertEquals(
        TestAnalysis.extractString(a, "info", "description2"),
        "description2 for this sequencingRead analysis an01");

    assertEquals(TestAnalysis.extractString(a, "experiment", "libraryStrategy"), "WXS");
    assertFalse(TestAnalysis.extractBoolean(a, "experiment", "pairedEnd"));
    assertEquals(TestAnalysis.extractLong(a, "experiment", "insertSize"), 92736);
    assertTrue(TestAnalysis.extractBoolean(a, "experiment", "aligned"));
    assertEquals(
        TestAnalysis.extractString(a, "experiment", "alignmentTool"),
        "myCool Sequence ReadingTool");
    assertEquals(TestAnalysis.extractString(a, "experiment", "referenceGenome"), "someSeq Genome");
    assertEquals(
        TestAnalysis.extractString(a, "experiment", "extraExperimentInfo"),
        "some more data for a sequencingRead experiment ex02");

    assertEquals(a.getFiles().size(), 3);
    val file0 = a.getFiles().get(0);
    val file1 = a.getFiles().get(1);
    val file2 = a.getFiles().get(2);
    assertEquals(file0.getAnalysisId(), analysisId);
    assertEquals(file1.getAnalysisId(), analysisId);
    assertEquals(file2.getAnalysisId(), analysisId);
    assertEquals(file0.getStudyId(), DEFAULT_STUDY_ID);
    assertEquals(file1.getStudyId(), DEFAULT_STUDY_ID);
    assertEquals(file2.getStudyId(), DEFAULT_STUDY_ID);

    val fileName0 = "a3bc0998a-3521-43fd-fa10-a834f3874e46-fn1.MUSE_1-0rc-vcf.20170711.bam";
    val fileName1 = "a3bc0998a-3521-43fd-fa10-a834f3874e46-fn2.MUSE_1-0rc-vcf.20170711.bam";
    val fileName2 = "a3bc0998a-3521-43fd-fa10-a834f3874e46-fn3.MUSE_1-0rc-vcf.20170711.bam.bai";
    val fileMap = Maps.<String, FileEntity>newHashMap();

    for (val file : a.getFiles()) {
      fileMap.put(file.getFileName(), file);
      if (file.getFileName().equals(fileName0)) {
        assertEquals(file.getFileName(), fileName0);
        assertEquals(file.getFileSize().longValue(), 1212121L);
        assertEquals(file.getFileMd5sum(), "e2324667df8085eddfe95742047e153f");
        assertEquals(file.getFileAccess(), "controlled");
        assertEquals(file.getFileType(), "BAM");
        assertInfoKVPair(file, "extraFileInfo_0", "first data for sequencingRead file_fn1");
        assertInfoKVPair(file, "extraFileInfo_1", "second data for sequencingRead file_fn1");
      } else if (file.getFileName().equals(fileName1)) {
        assertEquals(file.getFileName(), fileName1);
        assertEquals(file.getFileSize().longValue(), 34343L);
        assertEquals(file.getFileMd5sum(), "8b5379a29aac642d6fe1808826bd9e49");
        assertEquals(file.getFileAccess(), "open");
        assertEquals(file.getFileType(), "BAM");
        assertInfoKVPair(file, "extraFileInfo", "some more data for sequencingRead file_fn2");

      } else if (file.getFileName().equals(fileName2)) {
        assertEquals(file.getFileName(), fileName2);
        assertEquals(file.getFileSize().longValue(), 4840L);
        assertEquals(file.getFileMd5sum(), "61da923f32863a9c5fa3d2a0e19bdee3");
        assertEquals(file.getFileAccess(), "open");
        assertEquals(file.getFileType(), "BAI");
        assertInfoKVPair(file, "extraFileInfo", "some more data for sequencingRead file_fn3");
      } else {
        Assert.fail(String.format("the fileName %s is not recognized", file.getFileName()));
      }
    }

    // Test the readFiles method
    for (val file : service.unsecuredReadFiles(analysisId)) {
      assertTrue(fileMap.containsKey(file.getFileName()));
      assertEquals(file, fileMap.get(file.getFileName()));
    }

    assertEquals(
        TestAnalysis.extractNode(service.unsecuredDeepRead(analysisId), "experiment"),
        TestAnalysis.extractNode(a, "experiment"));
  }

  @Test
  @Transactional
  public void testSuppress() {
    val an = analysisGenerator.createDefaultRandomAnalysis(SEQUENCING_READ);
    assertEquals(an.getAnalysisState(), "UNPUBLISHED");
    val id = an.getAnalysisId();
    val studyId = an.getStudyId();

    service.suppress(studyId, id);
    val analysis = service.securedDeepRead(studyId, id);
    assertEquals(analysis.getAnalysisState(), "SUPPRESSED");
  }

  @Test
  @Transactional
  public void testReadFiles() {
    val files = service.unsecuredReadFiles(DEFAULT_ANALYSIS_ID);
    System.err.printf("Got files '%s'", files);
    val expectedFiles = new ArrayList<FileEntity>();

    expectedFiles.add(fileService.securedRead(DEFAULT_STUDY_ID, "FI1"));
    expectedFiles.add(fileService.securedRead(DEFAULT_STUDY_ID, "FI2"));

    assertTrue(files.containsAll(expectedFiles));
    assertTrue(expectedFiles.containsAll(files));
    val files2 = service.securedReadFiles(DEFAULT_STUDY_ID, DEFAULT_ANALYSIS_ID);
    assertTrue(files2.containsAll(files));
    assertTrue(files.containsAll(files2));
  }

  @Test
  @Transactional
  public void testReadFilesError() {
    val nonExistingAnalysisId = analysisGenerator.generateNonExistingAnalysisId();
    assertSongError(() -> service.unsecuredReadFiles(nonExistingAnalysisId), ANALYSIS_ID_NOT_FOUND);
  }

  @Test
  @Transactional
  public void testNoDuplicateAnalysisAttemptError() {
    val an1 = analysisGenerator.createDefaultRandomSequencingReadAnalysis();
    val equivalentPayload = exportService.convertToPayloadDTO(an1);
    val differentAnalysis = service.create(an1.getStudyId(), equivalentPayload);
    assertNotEquals(an1, differentAnalysis.getAnalysisId());
  }

  @Test
  @Transactional
  public void testCreateAnalysisStudyDNE() {
    val nonExistentStudyId = randomGenerator.generateRandomUUID().toString();
    assertFalse(studyService.isStudyExist(nonExistentStudyId));

    val payload = payloadGenerator.generateDefaultRandomPayload(VARIANT_CALL);

    assertSongError(() -> service.create(nonExistentStudyId, payload), STUDY_ID_DOES_NOT_EXIST);
  }

  @Test
  @Transactional
  public void testGetAnalysisAndIdSearch() {
    val studyId = studyGenerator.createRandomStudy();

    val analysisGenerator = createAnalysisGenerator(studyId, service, randomGenerator);
    val numAnalysis = 10;
    val sraMap = Maps.<String, Analysis>newHashMap();
    val vcaMap = Maps.<String, Analysis>newHashMap();
    val expectedAnalyses = Sets.<Analysis>newHashSet();
    for (int i = 1; i <= numAnalysis; i++) {
      if (i % 2 == 0) {
        val sra = analysisGenerator.createDefaultRandomSequencingReadAnalysis();
        assertFalse(sraMap.containsKey(sra.getAnalysisId()));
        sraMap.put(sra.getAnalysisId(), sra);
        expectedAnalyses.add(sra);
      } else {
        val vca = analysisGenerator.createDefaultRandomVariantCallAnalysis();
        assertFalse(sraMap.containsKey(vca.getAnalysisId()));
        vcaMap.put(vca.getAnalysisId(), vca);
        expectedAnalyses.add(vca);
      }
    }
    assertEquals(expectedAnalyses.size(), numAnalysis);
    assertEquals(sraMap.keySet().size() + vcaMap.keySet().size(), numAnalysis);
    val expectedVCAs = newHashSet(vcaMap.values());
    val expectedSRAs = newHashSet(sraMap.values());
    assertEquals(expectedSRAs.size(), sraMap.keySet().size());
    assertEquals(expectedVCAs.size(), vcaMap.keySet().size());

    //  test legacy getAnalysis
    val actualAnalyses = service.getAnalysis(studyId, ALL_STATES);
    val actualSRAs =
        actualAnalyses.stream()
            .filter(
                x -> x.getAnalysisSchema().getName().equals(SEQUENCING_READ.getAnalysisTypeName()))
            .collect(toSet());
    val actualVCAs =
        actualAnalyses.stream()
            .filter(x -> x.getAnalysisSchema().getName().equals(VARIANT_CALL.getAnalysisTypeName()))
            .collect(toSet());
    assertEquals(actualSRAs.size(), sraMap.keySet().size());
    assertEquals(actualVCAs.size(), vcaMap.keySet().size());

    assertTrue(actualSRAs.containsAll(expectedSRAs));
    assertTrue(actualVCAs.containsAll(expectedVCAs));

    // testing paginated getAnalysis
    val actualAnalysesPaginated = service.getAnalysis(studyId, ALL_STATES, numAnalysis, 0);
    val actualSRAsPaginated =
        actualAnalysesPaginated.getAnalyses().stream()
            .filter(
                x -> x.getAnalysisSchema().getName().equals(SEQUENCING_READ.getAnalysisTypeName()))
            .collect(toSet());
    val actualVCAsPaginated =
        actualAnalysesPaginated.getAnalyses().stream()
            .filter(x -> x.getAnalysisSchema().getName().equals(VARIANT_CALL.getAnalysisTypeName()))
            .collect(toSet());

    assertEquals(actualSRAsPaginated.size(), sraMap.keySet().size());
    assertEquals(actualVCAsPaginated.size(), vcaMap.keySet().size());

    for (val actual : actualSRAsPaginated) {
      val expected =
          expectedSRAs.stream()
              .filter(a -> a.getAnalysisId().equals(actual.getAnalysisId()))
              .findFirst()
              .orElseThrow();

      Assertions.assertThat(actual)
          .usingRecursiveComparison()
          // the new getAnalysis() method does not load 'analysisData.analysis' because this field
          // is not needed for the getAnalysis endpoint response.
          // expected results have 'analysisData.analysis' because it's a Hibernate default
          // behaviour
          // same reason for 'analysisSchema.analyses'.
          .ignoringFields("analysisData.analysis")
          .ignoringFields("analysisSchema.analyses")
          .ignoringFields("analysisStateHistory.analysis")
          .ignoringCollectionOrderInFields(
              "files", "analysisData.data", "samples", "analysisStateHistory")
          .isEqualTo(expected);
    }

    for (val actual : actualVCAsPaginated) {
      val expected =
          expectedVCAs.stream()
              .filter(a -> a.getAnalysisId().equals(actual.getAnalysisId()))
              .findFirst()
              .orElseThrow();

      Assertions.assertThat(actual)
          .usingRecursiveComparison()
          // the new getAnalysis() method does not load 'analysisData.analysis' because this field
          // is not needed for the getAnalysis endpoint response.
          // expected results have 'analysisData.analysis' because it's a Hibernate default
          // behaviour
          // same reason for 'analysisSchema.analyses'.
          .ignoringFields("analysisData.analysis")
          .ignoringFields("analysisSchema.analyses")
          .ignoringCollectionOrderInFields(
              "files", "analysisData.data", "samples", "analysisStateHistory")
          .isEqualTo(expected);
    }

    // Do a study-wide idSearch and verify the response effectively has the same
    // number of results as the getAnalysis method
    val searchedAnalyses = service.idSearch(studyId, createIdSearchRequest(null));
    assertEquals(searchedAnalyses.size(), expectedAnalyses.size());
    assertTrue(searchedAnalyses.containsAll(expectedAnalyses));
    assertTrue(expectedAnalyses.containsAll(searchedAnalyses));
  }

  @Test
  @Transactional
  public void testOnlyGetPublishedAnalyses() {
    val studyId = studyGenerator.createRandomStudy();
    val analysisGenerator = createAnalysisGenerator(studyId, service, randomGenerator);
    val numAnalysis = 10;
    val expectedMap =
        range(0, numAnalysis)
            .boxed()
            .map(
                x -> {
                  Analysis a = analysisGenerator.createDefaultRandomSequencingReadAnalysis();
                  AnalysisStates randomState =
                      x == 0 ? PUBLISHED : randomGenerator.randomEnum(AnalysisStates.class);
                  a.setAnalysisState(randomState.toString());
                  service.securedUpdateState(studyId, a.getAnalysisId(), randomState);
                  return a;
                })
            .collect(groupingBy(Analysis::getAnalysisState));

    val actualMap =
        // test legacy getAnalysis
        service.getAnalysis(studyId, PUBLISHED_ONLY).stream()
            .collect(groupingBy(Analysis::getAnalysisState));

    assertEquals(actualMap.keySet().size(), 1);
    assertTrue(expectedMap.containsKey(PUBLISHED.toString()));
    assertTrue(actualMap.containsKey(PUBLISHED.toString()));
    assertEquals(
        actualMap.get(PUBLISHED.toString()).size(), expectedMap.get(PUBLISHED.toString()).size());
    val actualResult = actualMap.get(PUBLISHED.toString());
    val expectedResult = expectedMap.get(PUBLISHED.toString());
    assertTrue(actualResult.containsAll(expectedResult));
    assertTrue(expectedResult.containsAll(actualResult));

    val actualMapPaginated =
        // test paginated getAnalysis
        service.getAnalysis(studyId, PUBLISHED_ONLY, numAnalysis, 0).getAnalyses().stream()
            .collect(groupingBy(Analysis::getAnalysisState));
    assertEquals(actualMapPaginated.keySet().size(), 1);
    assertTrue(actualMapPaginated.containsKey(PUBLISHED.toString()));
    assertEquals(
        actualMapPaginated.get(PUBLISHED.toString()).size(),
        expectedMap.get(PUBLISHED.toString()).size());
    val actualResultPaginated = actualMapPaginated.get(PUBLISHED.toString());
    val expectedResultPaginated = expectedMap.get(PUBLISHED.toString());

    for (val actual : actualResultPaginated) {
      val expected =
          expectedResultPaginated.stream()
              .filter(a -> a.getAnalysisId().equals(actual.getAnalysisId()))
              .findFirst()
              .orElseThrow();
      Assertions.assertThat(actual)
          .usingRecursiveComparison()
          .ignoringFields("analysisData.analysis")
          .ignoringFields("analysisSchema.analyses")
          .ignoringFields("analysisStateHistory.analysis")
          .ignoringCollectionOrderInFields(
              "files", "analysisData.data", "samples", "analysisStateHistory")
          .isEqualTo(expected);
    }
  }

  @Test
  @Transactional
  public void testGetAnalysisEmptyStudy() {
    val studyId = studyGenerator.createRandomStudy();
    assertTrue(service.getAnalysis(studyId, PUBLISHED_ONLY).isEmpty());
    assertTrue(service.getAnalysis(studyId, PUBLISHED_ONLY, 100, 0).getAnalyses().isEmpty());
  }

  @Test
  @Transactional
  public void testIdSearchEmptyStudy() {
    val studyId = studyGenerator.createRandomStudy();
    val idSearchRequest = createIdSearchRequest(null);
    assertTrue(service.idSearch(studyId, idSearchRequest).isEmpty());
  }

  @Test
  @Transactional
  public void testGetAnalysisDNEStudy() {
    val nonExistentStudyId = studyGenerator.generateNonExistingStudyId();
    assertSongError(
        () -> service.getAnalysis(nonExistentStudyId, PUBLISHED_ONLY), STUDY_ID_DOES_NOT_EXIST);
    assertSongError(
        () -> service.getAnalysis(nonExistentStudyId, PUBLISHED_ONLY, 100, 0),
        STUDY_ID_DOES_NOT_EXIST);
  }

  @Test
  @Transactional
  public void testIdSearchDNEStudy() {
    val nonExistentStudyId = studyGenerator.generateNonExistingStudyId();
    val idSearchRequest = createIdSearchRequest(null);
    assertSongError(
        () -> service.idSearch(nonExistentStudyId, idSearchRequest), STUDY_ID_DOES_NOT_EXIST);
  }

  @Test
  @Transactional
  public void testAnalysisMissingFilesException() {
    val analysis1 = analysisGenerator.createDefaultRandomSequencingReadAnalysis();
    val analysisId1 = analysis1.getAnalysisId();

    fileRepository.deleteAllByAnalysisId(analysisId1);
    assertTrue(fileRepository.findAllByAnalysisId(analysisId1).isEmpty());
    assertSongError(() -> service.unsecuredReadFiles(analysisId1), ANALYSIS_MISSING_FILES);
    assertSongError(
        () -> service.securedReadFiles(analysis1.getStudyId(), analysisId1),
        ANALYSIS_MISSING_FILES);

    Analysis analysis2 = null;
    var notValid = true;
    int retries = 0;
    while (notValid) {
      try {
        analysis2 = analysisGenerator.createDefaultRandomVariantCallAnalysis();
        notValid = false;
      } catch (ServerException err) {
        System.err.println(err);
        if (retries > 100) {
          analysis2 = null;
          notValid = false;
        }
      }
    }

    val analysisId2 = analysis2.getAnalysisId();
    fileRepository.deleteAllByAnalysisId(analysisId2);
    assertTrue(fileRepository.findAllByAnalysisId(analysisId2).isEmpty());
    assertSongError(() -> service.unsecuredReadFiles(analysisId2), ANALYSIS_MISSING_FILES);

    val finalAnalysis = analysis2;
    assertSongError(
        () -> service.securedReadFiles(finalAnalysis.getStudyId(), analysisId2),
        ANALYSIS_MISSING_FILES);
  }

  @Test
  @Transactional
  public void testAnalysisIdDneException() {
    val nonExistentAnalysisId = analysisGenerator.generateNonExistingAnalysisId();
    SongErrorAssertions.assertSongErrorRunnable(
        () -> service.checkAnalysisAndStudyRelated(DEFAULT_STUDY_ID, nonExistentAnalysisId),
        ANALYSIS_ID_NOT_FOUND);
    SongErrorAssertions.assertSongErrorRunnable(
        () -> service.checkAnalysisExists(nonExistentAnalysisId), ANALYSIS_ID_NOT_FOUND);
    assertSongError(
        () -> service.securedDeepRead(DEFAULT_STUDY_ID, nonExistentAnalysisId),
        ANALYSIS_ID_NOT_FOUND);
    assertSongError(() -> service.unsecuredDeepRead(nonExistentAnalysisId), ANALYSIS_ID_NOT_FOUND);
    assertSongError(() -> service.unsecuredReadFiles(nonExistentAnalysisId), ANALYSIS_ID_NOT_FOUND);
    assertSongError(
        () -> service.securedReadFiles(DEFAULT_STUDY_ID, nonExistentAnalysisId),
        ANALYSIS_ID_NOT_FOUND);
    assertSongError(() -> service.readState(nonExistentAnalysisId), ANALYSIS_ID_NOT_FOUND);
  }

  @Test
  @Transactional
  public void testCheckAnalysisAndStudyRelated() {
    val existingAnalysisId = DEFAULT_ANALYSIS_ID;
    val existingStudyId = DEFAULT_STUDY_ID;
    assertTrue(service.isAnalysisExist(existingAnalysisId));
    assertTrue(studyService.isStudyExist(existingStudyId));
    service.checkAnalysisAndStudyRelated(existingStudyId, existingAnalysisId);
    assert (true);
  }

  @Test
  @Transactional
  public void testCheckAnalysisUnrelatedToStudy() {
    secureAnalysisTester.runSecureTest((s, a) -> service.checkAnalysisAndStudyRelated(s, a));
    secureAnalysisTester.runSecureTest(
        (s, a) -> service.securedDeepRead(s, a), LegacyAnalysisTypeName.VARIANT_CALL);
    secureAnalysisTester.runSecureTest((s, a) -> service.securedDeepRead(s, a), SEQUENCING_READ);
    secureAnalysisTester.runSecureTest((s, a) -> service.suppress(s, a));
    secureAnalysisTester.runSecureTest((s, a) -> service.securedReadFiles(s, a));
    secureAnalysisTester.runSecureTest((s, a) -> service.publish(s, a, false));
    secureAnalysisTester.runSecureTest((s, a) -> service.publish(s, a, true));
  }

  @Test
  @Transactional
  public void testAnalysisExistence() {
    val existingAnalysisId = DEFAULT_ANALYSIS_ID;
    val nonExistentAnalysisId = randomGenerator.generateRandomUUID().toString();
    assertFalse(service.isAnalysisExist(nonExistentAnalysisId));
    assertTrue(service.isAnalysisExist(existingAnalysisId));
    assertTrue(analysisRepository.existsById(existingAnalysisId));
    assertFalse(analysisRepository.existsById(nonExistentAnalysisId));
  }

  @Test
  @Transactional
  public void testGetAnalysisForStudyFilteredByStates() {
    val studyId = studyGenerator.createRandomStudy();
    val generator = createAnalysisGenerator(studyId, service, randomGenerator);

    val numCopies = 2;
    val expectedAnalyses =
        range(0, numCopies)
            .boxed()
            .flatMap(x -> stream(AnalysisStates.values()))
            .map(x -> createSRAnalysisWithState(generator, x))
            .collect(toImmutableSet());

    // 0 - Empty
    assertGetAnalysesForStudy(expectedAnalyses, studyId);

    // 1 - PUBLISHED
    assertGetAnalysesForStudy(expectedAnalyses, studyId, PUBLISHED);

    // 2 - UNPUBLISHED
    assertGetAnalysesForStudy(expectedAnalyses, studyId, UNPUBLISHED);

    // 3 - SUPPRESSED
    assertGetAnalysesForStudy(expectedAnalyses, studyId, SUPPRESSED);

    // 4 - PUBLISHED,UNPUBLISHED
    assertGetAnalysesForStudy(expectedAnalyses, studyId, PUBLISHED, UNPUBLISHED);

    // 5 - PUBLISHED,SUPPRESSED
    assertGetAnalysesForStudy(expectedAnalyses, studyId, PUBLISHED, SUPPRESSED);

    // 6 - UNPUBLISHED,SUPPRESSED
    assertGetAnalysesForStudy(expectedAnalyses, studyId, UNPUBLISHED, SUPPRESSED);

    // 7 - PUBLISHED,UNPUBLISHED,SUPPRESSED
    assertGetAnalysesForStudy(expectedAnalyses, studyId, UNPUBLISHED, SUPPRESSED, PUBLISHED);

    // test legacy getAnalysis
    assertSongError(
        () -> service.getAnalysis(studyId, newHashSet(PUBLISHED.toString(), "SomethingElse")),
        MALFORMED_PARAMETER);

    assertSongError(
        () -> service.getAnalysis(studyId, newHashSet("SomethingElse")), MALFORMED_PARAMETER);

    // test paginated getAnalysis
    assertSongError(
        () ->
            service.getAnalysis(
                studyId, newHashSet(PUBLISHED.toString(), "SomethingElse"), numCopies, 0),
        MALFORMED_PARAMETER);

    assertSongError(
        () -> service.getAnalysis(studyId, newHashSet("SomethingElse"), numCopies, 0),
        MALFORMED_PARAMETER);
  }

  private static LegacyAnalysisTypeName selectAnalysisType(int select) {
    return select % 2 == 0 ? LegacyAnalysisTypeName.VARIANT_CALL : SEQUENCING_READ;
  }

  @Test
  @Transactional
  public void testGetAnalysisForStudyView() {
    val numAnalysesPerStudy = 100;
    val numStudies = 3;
    val studyIds =
        range(0, numStudies)
            .boxed()
            .map(x -> studyGenerator.createRandomStudy())
            .collect(toImmutableSet());
    val study2AnalysesMap =
        studyIds.stream()
            .map(x -> createAnalysisGenerator(x, service, randomGenerator))
            .map(
                x ->
                    range(0, numAnalysesPerStudy)
                        .boxed()
                        .map(a -> (Analysis) x.createDefaultRandomAnalysis(selectAnalysisType(a))))
            .flatMap(x -> x)
            .collect(groupingBy(Analysis::getStudyId));

    val studyId = study2AnalysesMap.keySet().stream().findFirst().get();
    val expectedAnalyses = study2AnalysesMap.get(studyId);

    val expectedAnalysisMap =
        expectedAnalyses.stream().collect(toMap(Analysis::getAnalysisId, identity()));

    val expectedAnalysisIds = expectedAnalysisMap.keySet();

    val analysisStates = ImmutableSet.of(UNPUBLISHED.toString());

    // test legacy getAnalysis
    val actualAnalyses1 = service.getAnalysis(studyId, analysisStates);
    val actualAnalysisIds1 =
        actualAnalyses1.stream().map(Analysis::getAnalysisId).collect(toImmutableSet());

    assertCollectionsMatchExactly(actualAnalysisIds1, expectedAnalysisIds);
    actualAnalyses1.forEach(x -> diff(x, expectedAnalysisMap.get(x.getAnalysisId())));

    // test paginated getAnalysis
    val actualAnalyses1_paginated =
        service.getAnalysis(studyId, analysisStates, numAnalysesPerStudy, 0).getAnalyses();

    val actualAnalysisIds1_paginated =
        actualAnalyses1_paginated.stream().map(Analysis::getAnalysisId).collect(toImmutableSet());

    assertCollectionsMatchExactly(actualAnalysisIds1_paginated, expectedAnalysisIds);
    actualAnalyses1_paginated.forEach(x -> diff(x, expectedAnalysisMap.get(x.getAnalysisId())));
  }

  @Test
  public void testUnpublishState() {
    Stream.of(LegacyAnalysisTypeName.VARIANT_CALL, SEQUENCING_READ)
        .forEach(this::runUnpublishStateTest);
  }

  // A set of tests to check the behaviour of createdAt,
  //   updatedAt, publishedAt and firstPublishedAt properties:

  @Test
  public void testNewAnalysisGetsDateProperties() {
    val created = analysisGenerator.createDefaultRandomSequencingReadAnalysis();
    assertNotNull(created.getCreatedAt());
    assertNotNull(created.getUpdatedAt());
    assertNull(created.getPublishedAt());
    assertNull(created.getFirstPublishedAt());
  }

  @Test
  public void testUnpublishAnalysisChangesUpdatedAt() {
    val createdAnalysis = analysisGenerator.createDefaultRandomSequencingReadAnalysis();

    val studyId = createdAnalysis.getStudyId();
    val analysisId = createdAnalysis.getAnalysisId();

    service.securedUpdateState(studyId, analysisId, AnalysisStates.PUBLISHED);
    service.unpublish(studyId, analysisId);

    val reloadedAnalysis = service.unsecuredDeepRead(analysisId);

    assertTrue(createdAnalysis.getUpdatedAt().isBefore(reloadedAnalysis.getUpdatedAt()));
  }

  @Test
  public void testSuppressAnalysisChangesUpdatedAt() {
    val createdAnalysis = analysisGenerator.createDefaultRandomSequencingReadAnalysis();

    val studyId = createdAnalysis.getStudyId();
    val analysisId = createdAnalysis.getAnalysisId();

    service.suppress(studyId, analysisId);

    val reloadedAnalysis = service.unsecuredDeepRead(analysisId);

    assertTrue(createdAnalysis.getUpdatedAt().isBefore(reloadedAnalysis.getUpdatedAt()));
  }

  @Test
  public void testPublishedAnalysisGetsUpdatedDateProperties() {
    val createdAnalysis = analysisGenerator.createDefaultRandomSequencingReadAnalysis();

    val studyId = createdAnalysis.getStudyId();
    val analysisId = createdAnalysis.getAnalysisId();

    service.securedUpdateState(studyId, analysisId, PUBLISHED);

    val reloadedAnalysis = service.unsecuredDeepRead(analysisId);
    assertTrue(createdAnalysis.getUpdatedAt().isBefore(reloadedAnalysis.getUpdatedAt()));
    assertNotNull(reloadedAnalysis.getPublishedAt());
    assertNotNull(reloadedAnalysis.getFirstPublishedAt());
    assertEquals(reloadedAnalysis.getPublishedAt(), reloadedAnalysis.getFirstPublishedAt());
  }

  @Test
  public void testRepeatPublishesDoesNotUpdateFirstPublishedAt() {
    val createdAnalysis = analysisGenerator.createDefaultRandomSequencingReadAnalysis();

    val studyId = createdAnalysis.getStudyId();
    val analysisId = createdAnalysis.getAnalysisId();

    service.securedUpdateState(studyId, analysisId, PUBLISHED);
    service.unpublish(studyId, analysisId);
    service.securedUpdateState(studyId, analysisId, PUBLISHED);

    val reloadedAnalysis = service.unsecuredDeepRead(analysisId);
    assertTrue(createdAnalysis.getUpdatedAt().isBefore(reloadedAnalysis.getUpdatedAt()));
    assertNotNull(reloadedAnalysis.getPublishedAt());
    assertNotNull(reloadedAnalysis.getFirstPublishedAt());
    assertNotEquals(reloadedAnalysis.getPublishedAt(), reloadedAnalysis.getFirstPublishedAt());
  }

  @Test
  public void testPublishRecordsStateChangeHistory() {
    val createdAnalysis = analysisGenerator.createDefaultRandomSequencingReadAnalysis();

    val studyId = createdAnalysis.getStudyId();
    val analysisId = createdAnalysis.getAnalysisId();

    service.securedUpdateState(studyId, analysisId, PUBLISHED);

    val reloadedAnalysis = service.unsecuredDeepRead(analysisId);
    assertNotNull(reloadedAnalysis.getAnalysisStateHistory());
    assertEquals(reloadedAnalysis.getAnalysisStateHistory().size(), 1);

    val stateChangeRecord = reloadedAnalysis.getAnalysisStateHistory().iterator().next();
    assertEquals(stateChangeRecord.getInitialState(), UNPUBLISHED.name());
    assertEquals(stateChangeRecord.getUpdatedState(), PUBLISHED.name());
    assertTrue(createdAnalysis.getUpdatedAt().isBefore(stateChangeRecord.getUpdatedAt()));
  }

  @Test
  public void testUnpublishRecordsStateChangeHistory() {
    val createdAnalysis = analysisGenerator.createDefaultRandomSequencingReadAnalysis();

    val studyId = createdAnalysis.getStudyId();
    val analysisId = createdAnalysis.getAnalysisId();

    service.securedUpdateState(studyId, analysisId, PUBLISHED);
    service.unpublish(studyId, analysisId);

    val reloadedAnalysis = service.unsecuredDeepRead(analysisId);
    assertNotNull(reloadedAnalysis.getAnalysisStateHistory());
    assertEquals(reloadedAnalysis.getAnalysisStateHistory().size(), 2);

    val historyIterator = reloadedAnalysis.getAnalysisStateHistory().iterator();
    val publishStateRecordOption =
        reloadedAnalysis.getAnalysisStateHistory().stream()
            .filter(i -> i.getUpdatedState().equals(PUBLISHED.name()))
            .findFirst();
    val unpublishStateRecordOption =
        reloadedAnalysis.getAnalysisStateHistory().stream()
            .filter(i -> i.getUpdatedState().equals(UNPUBLISHED.name()))
            .findFirst();
    assertTrue(!publishStateRecordOption.isEmpty());
    assertTrue(!unpublishStateRecordOption.isEmpty());

    val publishStateRecord = publishStateRecordOption.get();
    val unpublishStateRecord = unpublishStateRecordOption.get();

    assertEquals(publishStateRecord.getInitialState(), UNPUBLISHED.name());
    assertEquals(unpublishStateRecord.getInitialState(), PUBLISHED.name());
    assertTrue(publishStateRecord.getUpdatedAt().isBefore(unpublishStateRecord.getUpdatedAt()));
  }

  @Test
  public void testSuppressRecordsStateChangeHistory() {
    val createdAnalysis = analysisGenerator.createDefaultRandomSequencingReadAnalysis();

    val studyId = createdAnalysis.getStudyId();
    val analysisId = createdAnalysis.getAnalysisId();

    service.suppress(studyId, analysisId);

    val reloadedAnalysis = service.unsecuredDeepRead(analysisId);
    assertNotNull(reloadedAnalysis.getAnalysisStateHistory());
    assertEquals(reloadedAnalysis.getAnalysisStateHistory().size(), 1);

    val stateChangeRecord = reloadedAnalysis.getAnalysisStateHistory().iterator().next();
    assertEquals(stateChangeRecord.getInitialState(), UNPUBLISHED.name());
    assertEquals(stateChangeRecord.getUpdatedState(), SUPPRESSED.name());
    assertTrue(createdAnalysis.getUpdatedAt().isBefore(stateChangeRecord.getUpdatedAt()));
  }

  @Test
  public void testMultipleStateChangeHasSortedHistory() {
    val createdAnalysis = analysisGenerator.createDefaultRandomSequencingReadAnalysis();

    val studyId = createdAnalysis.getStudyId();
    val analysisId = createdAnalysis.getAnalysisId();

    service.securedUpdateState(studyId, analysisId, PUBLISHED);
    service.securedUpdateState(studyId, analysisId, UNPUBLISHED);
    service.securedUpdateState(studyId, analysisId, PUBLISHED);
    service.securedUpdateState(studyId, analysisId, SUPPRESSED);

    val reloadedAnalysis = service.unsecuredDeepRead(analysisId);
    val stateHistory = new AnalysisStateChange[4];
    reloadedAnalysis.getAnalysisStateHistory().toArray(stateHistory);

    assertTrue(stateHistory[0].getUpdatedAt().isBefore(stateHistory[1].getUpdatedAt()));
    assertTrue(stateHistory[1].getUpdatedAt().isBefore(stateHistory[2].getUpdatedAt()));
    assertTrue(stateHistory[2].getUpdatedAt().isBefore(stateHistory[3].getUpdatedAt()));
  }

  private void runUnpublishStateTest(LegacyAnalysisTypeName legacyAnalysisTypeName) {
    val a = analysisGenerator.createDefaultRandomAnalysis(legacyAnalysisTypeName);
    val analysisId = a.getAnalysisId();
    val studyId = a.getStudyId();

    // 1: UNPUBLISHED -> UNPUBLISHED
    service.securedUpdateState(studyId, analysisId, UNPUBLISHED);
    val a11 = service.unsecuredDeepRead(analysisId);
    val actualState11 = resolveAnalysisState(a11.getAnalysisState());
    assertEquals(actualState11, UNPUBLISHED);
    service.unpublish(studyId, analysisId);
    val a12 = service.unsecuredDeepRead(analysisId);
    val actualState12 = resolveAnalysisState(a12.getAnalysisState());
    assertEquals(actualState12, UNPUBLISHED);

    // 2: PUBLISHED -> UNPUBLISHED
    service.securedUpdateState(studyId, analysisId, PUBLISHED);
    val a21 = service.unsecuredDeepRead(analysisId);
    val actualState21 = resolveAnalysisState(a21.getAnalysisState());
    assertEquals(actualState21, PUBLISHED);
    service.unpublish(studyId, analysisId);
    val a22 = service.unsecuredDeepRead(analysisId);
    val actualState22 = resolveAnalysisState(a22.getAnalysisState());
    assertEquals(actualState22, UNPUBLISHED);

    // 3: SUPPRESSED -> UNPUBLISHED
    service.securedUpdateState(studyId, analysisId, SUPPRESSED);
    val a31 = service.unsecuredDeepRead(analysisId);
    val actualState31 = resolveAnalysisState(a31.getAnalysisState());
    assertEquals(actualState31, SUPPRESSED);
    assertSongError(() -> service.unpublish(studyId, analysisId), SUPPRESSED_STATE_TRANSITION);
  }

  private void assertGetAnalysesForStudy(
      Set<Analysis> expectedAnalyses, String studyId, AnalysisStates... states) {
    Set<String> stateStrings =
        stream(states).map(AnalysisStates::toString).collect(toImmutableSet());
    if (states.length == 0) {
      stateStrings = PUBLISHED_ONLY;
    }
    val finalStates = stateStrings;

    val results =
        service.getAnalysis(studyId, states.length == 0 ? newHashSet() : newHashSet(finalStates));
    val actual = results.stream().map(Analysis::getAnalysisId).collect(toImmutableSet());
    val expected =
        expectedAnalyses.stream()
            .filter(x -> finalStates.contains(x.getAnalysisState()))
            .map(Analysis::getAnalysisId)
            .collect(toImmutableSet());
    assertCollectionsMatchExactly(actual, expected);
  }

  private Analysis createSRAnalysisWithState(AnalysisGenerator generator, AnalysisStates state) {
    val a = generator.createDefaultRandomSequencingReadAnalysis();
    service.securedUpdateState(a.getStudyId(), a.getAnalysisId(), state);
    return service.unsecuredDeepRead(a.getAnalysisId());
  }

  private static <T, R> void assertFunctionEqual(T l, T r, Function<T, R> trFunction) {
    assertEquals(trFunction.apply(l), trFunction.apply(r));
  }

  private static void diff(Analysis l, Analysis r) {
    assertFunctionEqual(l, r, Analysis::getAnalysisId);
    assertFunctionEqual(l, r, Analysis::getAnalysisState);
    assertFunctionEqual(l, r, x -> x.getAnalysisSchema().getName());
    assertFunctionEqual(l, r, Analysis::getStudyId);
    assertFunctionEqual(l, r, x -> x.getAnalysisData().getData());

    val leftFiles = newHashSet(l.getFiles());
    val rightFiles = newHashSet(r.getFiles());
    assertCollectionsMatchExactly(leftFiles, rightFiles);

    assertEquals(l.getAnalysisData(), r.getAnalysisData());
  }
}
