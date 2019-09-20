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

import static bio.overture.song.client.util.FileIO.readFileContent;
import static bio.overture.song.client.util.FileIO.statusFileExists;

import bio.overture.song.client.config.ClientConfig;
import bio.overture.song.client.register.Registry;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import java.io.IOException;
import java.nio.file.Paths;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor
@Parameters(separators = "=", commandDescription = "Update the dynamic data of an analysis")
public class UpdateAnalysisCommand extends Command {
  private static final String ANALYSIS_ID_SWITCH = "--analysis-id";
  private static final String A_SWITCH = "-a";
  private static final String FILE_SWITCH = "--file";
  private static final String F_SWITCH = "-f";

  @Parameter(
      names = {F_SWITCH, FILE_SWITCH},
      required = true)
  private String fileName;

  @Parameter(
      names = {A_SWITCH, ANALYSIS_ID_SWITCH},
      required = true)
  private String analysisId;

  @NonNull private ClientConfig config;

  @NonNull private Registry registry;

  @Override
  public void run() throws IOException {
    // File checking
    val filePath = Paths.get(fileName);
    val fileStatus = statusFileExists(filePath);
    if (fileStatus.hasErrors()) {
      save(fileStatus);
      return;
    }

    val json = readFileContent(filePath);
    val status = registry.updateAnalysis(config.getStudyId(), analysisId, json);
    save(status);
  }
}
