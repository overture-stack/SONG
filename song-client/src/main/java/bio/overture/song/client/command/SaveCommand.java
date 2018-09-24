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

import bio.overture.song.client.register.Registry;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import bio.overture.song.client.config.Config;

import java.io.IOException;

@RequiredArgsConstructor
@Parameters(separators = "=", commandDescription = "Save an uploaded analysis by it's upload id, and get the permanent analysis id")
public class SaveCommand extends Command {

  @Parameter(names = { "-u", "--upload-id" })
  private String uploadId;

  @Parameter(names = { "-i", "--ignore-id-collisions" },
      description = "Ignores analysisId collisions with ids from the IdService")
  boolean ignoreAnalysisIdCollisions = false;

  @NonNull
  private Registry registry;

  @NonNull
  private Config config;

  @Override
  public void run() throws IOException{
    if (uploadId == null) {
      uploadId = getJson().at("/uploadId").asText("");
    }
    val status = registry.save(config.getStudyId(), uploadId, ignoreAnalysisIdCollisions);
    save(status);
  }

}
