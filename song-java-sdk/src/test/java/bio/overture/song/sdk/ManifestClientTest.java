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

package bio.overture.song.sdk;

import static bio.overture.song.core.utils.RandomGenerator.createRandomGenerator;
import static com.google.common.base.Preconditions.checkState;
import static java.util.stream.Collectors.toUnmodifiableList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import bio.overture.song.core.model.FileDTO;
import bio.overture.song.core.utils.RandomGenerator;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@Slf4j
@RunWith(MockitoJUnitRunner.class)
public class ManifestClientTest {
  private static final String DUMMY_STUDY_ID = "ABC123";
  private static final String DUMMY_ANALYSIS_ID = UUID.randomUUID().toString();

  @Rule public TemporaryFolder tmp = new TemporaryFolder();
  @Mock private SongApi songApi;
  private RandomGenerator randomGenerator;

  @Before
  public void beforeTest() {
    randomGenerator = createRandomGenerator(getClass().getSimpleName());
  }

  @Test
  @SneakyThrows
  public void testManifest() {
    val mc = new ManifestClient(songApi);
    // Create tmp inputDir and tmp files, as well as expected FileDTOs
    val inputDir = tmp.newFolder().toPath();
    val expectedFiles =
        IntStream.range(1, 4)
            .boxed()
            .map(i -> "file" + i + ".vcf.gz")
            .map(fn -> touchFile(inputDir, fn))
            .map(
                f ->
                    new FileDTO()
                        .setFileName(f.getName())
                        .setObjectId(randomGenerator.generateRandomUUIDAsString())
                        .setFileMd5sum(randomGenerator.generateRandomMD5()))
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

  @SneakyThrows
  private File touchFile(Path dir, String filename) {
    val path = dir.resolve(filename);
    val f = path.toFile();
    checkState(f.createNewFile());
    return f;
  }
}
