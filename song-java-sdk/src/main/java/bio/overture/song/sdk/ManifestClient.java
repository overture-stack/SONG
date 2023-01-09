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

import static bio.overture.song.core.utils.FileIO.checkDirectoryExists;
import static bio.overture.song.sdk.errors.ManifestClientException.checkManifest;
import static java.nio.file.Files.exists;
import static java.nio.file.Paths.get;
import static java.util.stream.Collectors.toList;

import bio.overture.song.core.model.File;
import bio.overture.song.sdk.model.Manifest;
import bio.overture.song.sdk.model.ManifestEntry;
import com.google.common.base.Joiner;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor
public class ManifestClient {

  @NonNull private final SongApi songApi;

  public Manifest generateManifest(
      @NonNull String studyId, @NonNull String analysisId, @NonNull String inputDirname)
      throws IOException {

    val inputDirPath = get(inputDirname);
    checkDirectoryExists(inputDirPath);
    val files = songApi.getAnalysisFiles(studyId, analysisId);
    val m = createManifest(inputDirPath, analysisId, files);
    val missingFiles =
        m.getEntries().stream()
            .map(ManifestEntry::getFileName)
            .map(Paths::get)
            .filter(x -> !exists(x))
            .collect(toList());

    checkManifest(!m.getEntries().isEmpty(), "The analysisId '%s' returned 0 files", analysisId);
    checkManifest(
        missingFiles.isEmpty(),
        "The following files do not exist: \n'%s'",
        Joiner.on("',\n'").join(missingFiles));
    return m;
  }

  private static Manifest createManifest(
      Path inputDir, String analysisId, @NonNull List<? extends File> files) throws IOException {
    val manifest = new Manifest(analysisId);
    files.stream().map(f -> createManifestEntry(inputDir, f)).forEach(manifest::add);
    return manifest;
  }

  private static ManifestEntry createManifestEntry(Path inputDir, File f) {
    val filepath = inputDir.resolve(f.getFileName()).toAbsolutePath().normalize().toString();
    return new ManifestEntry(f.getObjectId(), filepath, f.getFileMd5sum());
  }
}
