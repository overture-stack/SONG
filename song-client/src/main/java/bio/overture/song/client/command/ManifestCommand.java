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
package bio.overture.song.client.command;

import static java.nio.file.Files.exists;
import static java.nio.file.Files.isDirectory;
import static java.nio.file.Paths.get;
import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;

import bio.overture.song.client.config.Config;
import bio.overture.song.client.json.JsonObject;
import bio.overture.song.client.model.Manifest;
import bio.overture.song.client.model.ManifestEntry;
import bio.overture.song.client.register.Registry;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Joiner;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor
@Parameters(
    commandDescription = "Generate a manifest file for the analysis with the specified analysis id")
public class ManifestCommand extends Command {

  private static final String JSON_PATH_TO_FILES = "";

  @Parameter(names = {"-a", "--analysis-id"})
  private String analysisId;

  @Parameter(
      names = {"--file", "-f"},
      description = "Filename to save file in (if not set, displays manifest on standard output")
  private String fileName;

  @Parameter(
      names = {"-d", "--input-dir"},
      description = "Directory containing the files",
      required = true)
  private String inputDirName;

  @NonNull private Registry registry;

  @NonNull private Config config;

  private Path inputDirPath;

  @Override
  public void run() throws IOException {
    if (analysisId == null) {
      analysisId = getJson().at("/analysisId").asText("");
    }

    inputDirPath = get(inputDirName);

    if (!exists(inputDirPath)) {
      err("[SONG_CLIENT_ERROR]: The input path '%s' does not exist", inputDirName);
      return;
    }

    if (!isDirectory(inputDirPath)) {
      err("[SONG_CLIENT_ERROR]: The input path '%s' is not a directory", inputDirName);
      return;
    }

    val status = registry.getAnalysisFiles(config.getStudyId(), analysisId);

    if (status.hasErrors()) {
      save(status);
      return;
    }

    val m = createManifest(analysisId, status.getOutputs());
    val missingFiles =
        m.getEntries()
            .stream()
            .map(ManifestEntry::getFileName)
            .map(Paths::get)
            .filter(x -> !Files.exists(x))
            .collect(toList());

    if (m.getEntries().size() == 0) {
      err("[SONG_CLIENT_ERROR]: the analysisId '%s' returned 0 files", analysisId);
    } else if (missingFiles.size() > 0) {
      err(
          "[SONG_CLIENT_ERROR]: The following files do not exist: \n'%s'",
          Joiner.on("',\n'").join(missingFiles));
    } else if (fileName == null) {
      output(m.toString());
    } else {
      Files.write(get(fileName), m.toString().getBytes());
      output("Wrote manifest file '%s' for analysisId '%s'", fileName, analysisId);
    }
  }

  private Manifest createManifest(String analysisId, String json) throws IOException {
    val mapper = new ObjectMapper();
    val root = mapper.readTree(json);

    val m = new Manifest(analysisId);

    Iterable<JsonNode> iter = () -> root.at(JSON_PATH_TO_FILES).iterator();
    m.addAll(
        stream(iter.spliterator(), false).map(this::jsonNodeToManifestEntry).collect(toList()));
    return m;
  }

  private ManifestEntry jsonNodeToManifestEntry(JsonNode node) {
    val j = new JsonObject(node);
    val fileId = j.get("objectId");
    val fileName = j.get("fileName");
    val fileMd5 = j.get("fileMd5sum");

    val path = inputDirPath.resolve(fileName);
    return new ManifestEntry(fileId, path.toAbsolutePath().normalize().toString(), fileMd5);
  }
}
