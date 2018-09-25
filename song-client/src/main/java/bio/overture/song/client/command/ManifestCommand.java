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

import bio.overture.song.client.config.Config;
import bio.overture.song.client.json.JsonObject;
import bio.overture.song.client.model.Manifest;
import bio.overture.song.client.model.ManifestEntry;
import bio.overture.song.client.register.Registry;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RequiredArgsConstructor
@Parameters(commandDescription = "Generate a manifest file for the analysis with the specified analysis id")
public class ManifestCommand extends Command {

  private static final String JSON_PATH_TO_FILES = "";

  @Parameter(names = { "-a", "--analysis-id" })
  private String analysisId;

  @Parameter(names = { "--file", "-f" }, description = "Filename to save file in (if not set, displays manifest on standard output")
  private String fileName;

  @NonNull
  private Registry registry;

  @NonNull
  private Config config;

  @Override
  public void run() throws IOException {
    if (analysisId == null) {
      analysisId = getJson().at("/analysisId").asText("");
    }

    val status = registry.getAnalysisFiles(config.getStudyId(), analysisId);

    if (status.hasErrors()) {
      save(status);
      return;
    }

    val m = createManifest(analysisId, status.getOutputs());

    if(m.getEntries().size() == 0){
      err("[SONG_CLIENT_ERROR]: the analysisId '%s' returned 0 files", analysisId);
    } else if (fileName == null) {
      output(m.toString());
    } else {
      Files.write(Paths.get(fileName), m.toString().getBytes());
      output("Wrote manifest file '%s' for analysisId '%s'", fileName, analysisId);
    }
  }

  private Manifest createManifest(String analysisId, String json) throws IOException {
    val mapper = new ObjectMapper();
    val root = mapper.readTree(json);

    val m = new Manifest(analysisId);

    Iterable<JsonNode> iter = () -> root.at(JSON_PATH_TO_FILES).iterator();
    m.addAll(StreamSupport.stream(iter.spliterator(), false)
        .map(this::jsonNodeToManifestEntry)
        .collect(Collectors.toList()));
    return m;
  }

  private ManifestEntry jsonNodeToManifestEntry(JsonNode node) {
    val j = new JsonObject(node);
    val fileId = j.get("objectId");
    val fileName = j.get("fileName");
    val fileMd5 = j.get("fileMd5sum");

    return new ManifestEntry(fileId, fileName, fileMd5);
  }

}
