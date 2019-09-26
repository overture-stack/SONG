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

package bio.overture.song.sdk;

import bio.overture.song.client.cli.ClientMain;
import bio.overture.song.client.config.CustomRestClientConfig;
import bio.overture.song.core.model.Analysis;
import bio.overture.song.core.model.AnalysisType;
import bio.overture.song.core.model.AnalysisTypeId;
import bio.overture.song.core.model.Donor;
import bio.overture.song.core.model.FileDTO;
import bio.overture.song.core.model.PageDTO;
import bio.overture.song.core.model.Sample;
import bio.overture.song.core.model.Specimen;
import bio.overture.song.core.model.SubmitResponse;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import static com.google.common.base.Preconditions.checkState;
import static java.util.stream.Collectors.toUnmodifiableList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static bio.overture.song.core.model.enums.AnalysisStates.UNPUBLISHED;
import static bio.overture.song.core.utils.JsonUtils.mapper;
import static bio.overture.song.core.utils.JsonUtils.objectToTree;
import static bio.overture.song.core.utils.JsonUtils.readTree;
import static bio.overture.song.core.utils.RandomGenerator.createRandomGenerator;

@Slf4j
@RunWith(MockitoJUnitRunner.class)
public class HappyPathClientMainTest extends AbstractClientMainTest {

  private static final String DUMMY_STUDY_ID = "ABC123";
  private static final String DUMMY_ANALYSIS_ID = UUID.randomUUID().toString();
  private static final JsonNode DUMMY_PAYLOAD = mapper().createObjectNode().put("study", "ABC123");
  private static final JsonNode DUMMY_SCHEMA = mapper().createObjectNode().put("type", "object");
  private static final AnalysisType ANALYSIS_TYPE1 =
      AnalysisType.builder().name("name1").schema(DUMMY_SCHEMA).version(1).build();
  private static final AnalysisTypeId ANALYSIS_TYPE_ID1 =
      AnalysisTypeId.builder()
          .name(ANALYSIS_TYPE1.getName())
          .version(ANALYSIS_TYPE1.getVersion())
          .build();
  private static final JsonNode ANALYSIS_TYPE1_JSON = objectToTree(ANALYSIS_TYPE1);

  private static final AnalysisType ANALYSIS_TYPE2 =
      AnalysisType.builder().name("name2").schema(DUMMY_SCHEMA).version(2).build();

  private static final PageDTO<AnalysisType> ANALYSIS_TYPE_PAGE =
      PageDTO.<AnalysisType>builder()
          .count(2)
          .offset(2)
          .limit(4)
          .resultSet(List.of(ANALYSIS_TYPE1, ANALYSIS_TYPE2))
          .build();

  @Rule public TemporaryFolder tmp = new TemporaryFolder();

  @Mock private SongApi songApi;
  @Mock private CustomRestClientConfig customRestClientConfig;
  @Mock private ManifestClient manifestClient;

  @Override
  protected ClientMain getClientMain() {
    // Needs to be a new instance, to avoid appending status
    return new ClientMain(customRestClientConfig, songApi, manifestClient);
  }

  @Before
  public void beforeTest() {
    Mockito.reset(songApi);
  }

  /** Ensure the capture mechanism is working properly */
  @Test
  public void testCaptureConsole() {
    val randomGenerator = createRandomGenerator("capture");
    val expectedStdout = randomGenerator.generateRandomAsciiString(30);
    val expectedStderr = randomGenerator.generateRandomAsciiString(30);
    assertNotEquals(expectedStdout, expectedStderr);

    System.out.print(expectedStdout);
    val c = captureConsole();
    assertEquals(expectedStdout, c.getOut());
    assertEquals("", c.getErr());

    val c2 = captureConsole();
    assertEquals("", c2.getOut());
    assertEquals("", c2.getErr());

    System.err.print(expectedStderr);
    val c3 = captureConsole();
    assertEquals("", c3.getOut());
    assertEquals(expectedStderr, c3.getErr());

    val c4 = captureConsole();
    assertEquals("", c4.getOut());
    assertEquals("", c4.getErr());

    System.out.print(expectedStdout);
    System.err.print(expectedStderr);
    val c5 = captureConsole();
    assertEquals(expectedStdout, c5.getOut());
    assertEquals(expectedStderr, c5.getErr());
  }

  @Test
  public void testPing() {
    when(songApi.isAlive()).thenReturn(true);
    val e1 = executeMain("ping");
    assertTrue(getExitCode() == 0);
    assertTrue(e1.getOut().contains("true"));

    when(songApi.isAlive()).thenReturn(false);
    val e2 = executeMain("ping");
    assertTrue(getExitCode() == 0);
    assertTrue(e2.getOut().contains("false"));
  }

  @Test
  @SneakyThrows
  public void testListAnalysisTypes() {
    when(songApi.listAnalysisTypes(Mockito.any())).thenReturn(ANALYSIS_TYPE_PAGE);
    val e1 = executeMain("list-analysis-types");
    assertTrue(getExitCode() == 0);
    val actualOutputJson = readTree(e1.getOut());
    val expectedOutputJson = objectToTree(ANALYSIS_TYPE_PAGE.getResultSet());
    assertEquals(expectedOutputJson, actualOutputJson);
  }

  @SneakyThrows
  private void assertOutputJson(JsonNode expectedOutputJson, String... args) {
    val e1 = executeMain(args);
    assertTrue(getExitCode() == 0);
    val actualOutputJson = readTree(e1.getOut());
    assertEquals(expectedOutputJson, actualOutputJson);
  }

  @Test
  @SneakyThrows
  public void testGetAnalysisType() {
    when(songApi.getAnalysisType(ANALYSIS_TYPE1.getName(), null, false)).thenReturn(ANALYSIS_TYPE1);
    when(songApi.getAnalysisType(ANALYSIS_TYPE1.getName(), ANALYSIS_TYPE1.getVersion(), false))
        .thenReturn(ANALYSIS_TYPE1);
    when(songApi.getAnalysisType(ANALYSIS_TYPE1.getName(), ANALYSIS_TYPE1.getVersion(), true))
        .thenReturn(ANALYSIS_TYPE1);

    assertOutputJson(ANALYSIS_TYPE1_JSON, "get-analysis-type", "-n", ANALYSIS_TYPE1.getName());
    assertOutputJson(ANALYSIS_TYPE1_JSON, "get-analysis-type", "--name", ANALYSIS_TYPE1.getName());
    assertOutputJson(
        ANALYSIS_TYPE1_JSON,
        "get-analysis-type",
        "-n",
        ANALYSIS_TYPE1.getName(),
        "-v",
        ANALYSIS_TYPE1.getVersion().toString());
    assertOutputJson(
        ANALYSIS_TYPE1_JSON,
        "get-analysis-type",
        "-n",
        ANALYSIS_TYPE1.getName(),
        "--version",
        ANALYSIS_TYPE1.getVersion().toString());
    assertOutputJson(
        ANALYSIS_TYPE1_JSON,
        "get-analysis-type",
        "-n",
        ANALYSIS_TYPE1.getName(),
        "-v",
        ANALYSIS_TYPE1.getVersion().toString(),
        "-u");
    assertOutputJson(
        ANALYSIS_TYPE1_JSON,
        "get-analysis-type",
        "-n",
        ANALYSIS_TYPE1.getName(),
        "-v",
        ANALYSIS_TYPE1.getVersion().toString(),
        "--unrendered-only");
  }

  @Test
  @SneakyThrows
  public void testRegisterAnalysisType() {
    val file = tmp.newFile("example-register-request.json");
    Files.write(file.toPath(), DUMMY_SCHEMA.toString().getBytes());

    when(songApi.registerAnalysisType(DUMMY_SCHEMA.toString())).thenReturn(ANALYSIS_TYPE1);
    assertOutputJson(ANALYSIS_TYPE1_JSON, "register-analysis-type", "-f", file.getAbsolutePath());
    assertOutputJson(
        ANALYSIS_TYPE1_JSON, "register-analysis-type", "--file", file.getAbsolutePath());
  }

  @Test
  @SneakyThrows
  public void testSubmit() {
    val file = tmp.newFile();
    Files.write(file.toPath(), DUMMY_PAYLOAD.toString().getBytes());

    val expectedSubmitResponse =
        SubmitResponse.builder().analysisId(DUMMY_ANALYSIS_ID).status("ok").build();
    when(customRestClientConfig.getStudyId()).thenReturn(DUMMY_STUDY_ID);
    when(songApi.submit(DUMMY_STUDY_ID, DUMMY_PAYLOAD.toString()))
        .thenReturn(expectedSubmitResponse);
    assertOutputJson(objectToTree(expectedSubmitResponse), "submit", "-f", file.getAbsolutePath());
    assertOutputJson(
        objectToTree(expectedSubmitResponse), "submit", "--file", file.getAbsolutePath());
  }

  @Test
  public void testAnalysisSearch() {
    val expectedAnalysis =
        Analysis.builder()
            .analysisId(DUMMY_ANALYSIS_ID)
            .analysisState(UNPUBLISHED)
            .analysisType(ANALYSIS_TYPE_ID1)
            .study(DUMMY_STUDY_ID)
            .build();
    when(customRestClientConfig.getStudyId()).thenReturn(DUMMY_STUDY_ID);
    when(songApi.getAnalysis(DUMMY_STUDY_ID, DUMMY_ANALYSIS_ID)).thenReturn(expectedAnalysis);
    assertOutputJson(objectToTree(expectedAnalysis), "search", "-a", DUMMY_ANALYSIS_ID);
    assertOutputJson(objectToTree(expectedAnalysis), "search", "--analysis-id", DUMMY_ANALYSIS_ID);
  }

  @Test
  public void testIdSearch() {
    val expectedFile = FileDTO.builder().objectId("FI1").build();
    val expectedSample = Sample.builder().sampleId("SA1").build();
    val expectedSpecimen = Specimen.builder().specimenId("SP1").build();
    val expectedDonor = Donor.builder().donorId("DO1").build();
    val expectedAnalyses =
        List.of(
            Analysis.builder()
                .analysisId(DUMMY_ANALYSIS_ID)
                .analysisState(UNPUBLISHED)
                .analysisType(ANALYSIS_TYPE_ID1)
                .study(DUMMY_STUDY_ID)
                .build());

    when(customRestClientConfig.getStudyId()).thenReturn(DUMMY_STUDY_ID);
    when(songApi.idSearch(DUMMY_STUDY_ID, expectedSample.getSampleId(), null, null, null))
        .thenReturn(expectedAnalyses);
    when(songApi.idSearch(DUMMY_STUDY_ID, null, expectedSpecimen.getSpecimenId(), null, null))
        .thenReturn(expectedAnalyses);
    when(songApi.idSearch(DUMMY_STUDY_ID, null, null, expectedDonor.getDonorId(), null))
        .thenReturn(expectedAnalyses);
    when(songApi.idSearch(DUMMY_STUDY_ID, null, null, null, expectedFile.getObjectId()))
        .thenReturn(expectedAnalyses);
    assertOutputJson(objectToTree(expectedAnalyses), "search", "-d", expectedDonor.getDonorId());
    assertOutputJson(
        objectToTree(expectedAnalyses), "search", "--donor-id", expectedDonor.getDonorId());
    assertOutputJson(
        objectToTree(expectedAnalyses), "search", "-sp", expectedSpecimen.getSpecimenId());
    assertOutputJson(
        objectToTree(expectedAnalyses),
        "search",
        "--specimen-id",
        expectedSpecimen.getSpecimenId());
    assertOutputJson(objectToTree(expectedAnalyses), "search", "-sa", expectedSample.getSampleId());
    assertOutputJson(
        objectToTree(expectedAnalyses), "search", "--sample-id", expectedSample.getSampleId());
    assertOutputJson(objectToTree(expectedAnalyses), "search", "-f", expectedFile.getObjectId());
    assertOutputJson(
        objectToTree(expectedAnalyses), "search", "--file-id", expectedFile.getObjectId());
  }

  @SneakyThrows
  private File touchFile(Path dir, String filename) {
    val path = dir.resolve(filename);
    val f = path.toFile();
    checkState(f.createNewFile());
    return f;
  }

  @Test
  @SneakyThrows
  public void testManifest() {
    val mc = new ManifestClient(songApi);
    val randomGenerator = createRandomGenerator("manifest");
    // Create tmp inputDir and tmp files, as well as expected FileDTOs
    val inputDir = tmp.newFolder().toPath();
    val expectedFiles =
        IntStream.range(1, 4)
            .boxed()
            .map(i -> "file" + i + ".vcf.gz")
            .map(fn -> touchFile(inputDir, fn))
            .map(
                f ->
                    FileDTO.builder()
                        .fileName(f.getName())
                        .objectId(randomGenerator.generateRandomUUIDAsString())
                        .fileMd5sum(randomGenerator.generateRandomMD5())
                        .build())
            .map(f -> (bio.overture.song.core.model.File) f)
            .collect(toUnmodifiableList());

    // Mock api to return expected FileDTOs
    when(songApi.getAnalysisFiles(DUMMY_STUDY_ID, DUMMY_ANALYSIS_ID)).thenReturn(expectedFiles);

    // Generate manifest and assert entries match expected data
    val m =
        mc.generateManifest(
            DUMMY_STUDY_ID, DUMMY_ANALYSIS_ID, inputDir.toAbsolutePath().toString());
    assertEquals(DUMMY_ANALYSIS_ID, m.getAnalysisId());
    assertEquals(m.getEntries().size(), expectedFiles.size());
    val actualEntries = List.copyOf(m.getEntries());
    for (int i = 0; i < m.getEntries().size(); i++) {
      val actualEntry = actualEntries.get(i);
      val expectedFile = expectedFiles.get(i);
      assertEquals(expectedFile.getFileName(), actualEntry.getFileName().replaceAll(".*\\/", ""));
      assertEquals(expectedFile.getObjectId(), actualEntry.getFileId());
      assertEquals(expectedFile.getFileMd5sum(), actualEntry.getMd5sum());
    }

    // Build expected output string of manifest
    val expectedStringBuilder = new StringBuilder().append(DUMMY_ANALYSIS_ID + "\t\t\n");
    m.getEntries().stream()
        .map(e -> e.getFileId() + "\t" + e.getFileName() + "\t" + e.getMd5sum() + "\n")
        .forEach(expectedStringBuilder::append);
    val expectedString = expectedStringBuilder.toString();
    assertEquals(expectedString, m.toString());

    // Assert the manifest is written to the file properly
    val outputFile = tmp.newFile();
    m.writeToFile(outputFile.getAbsolutePath());
    val actualFileContents = Files.readString(outputFile.toPath());
    assertEquals(expectedString, actualFileContents);
  }

  @Test
  @Ignore
  public void testR() {
    when(songApi.listAnalysisTypes(Mockito.any())).thenCallRealMethod();
    val customRestClientConfig =
        CustomRestClientConfig.builder()
            .accessToken("1bf201c1-c458-41e9-af3a-723bd45796cb")
            //            .accessToken("922da174-c3b0-4114-9046-9150c0357120")
            //        .serverUrl("https://song.dev.argo.cancercollaboratory.org")
            .serverUrl("http://localhost:8080")
            .studyId("ABC123-CA")
            .build();
    val toolbox = Toolbox.createToolbox(customRestClientConfig);
    val cm =
        new ClientMain(customRestClientConfig, toolbox.getSongApi(), toolbox.getManifestClient());
    cm.run("list-analysis-types");
    log.info("sdfsdf");
  }
}
