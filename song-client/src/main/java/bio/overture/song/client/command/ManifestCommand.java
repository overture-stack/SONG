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

import static java.util.Objects.isNull;

import bio.overture.song.client.config.CustomRestClientConfig;
import bio.overture.song.sdk.ManifestClient;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import java.io.IOException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor
@Parameters(
    commandDescription = "Generate a manifest file for the analysis with the specified analysis id")
public class ManifestCommand extends Command {

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

  @NonNull private CustomRestClientConfig config;
  @NonNull private ManifestClient manifestClient;

  @Override
  public void run() throws IOException {
    if (analysisId == null) {
      analysisId = getJson().at("/analysisId").asText("");
    }

    val m = manifestClient.generateManifest(config.getStudyId(), analysisId, inputDirName);
    if (isNull(fileName)) {
      output(m.toString());
    } else {
      m.writeToFile(fileName);
      output("Wrote manifest file '%s' for analysisId '%s'", fileName, analysisId);
    }
  }
}
