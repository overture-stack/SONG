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

import bio.overture.song.sdk.config.RestClientConfig;
import bio.overture.song.sdk.register.Registry;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.io.IOException;

@RequiredArgsConstructor
@Parameters(separators = "=", commandDescription = "Publish an analysis id")
public class PublishCommand extends Command {

  @Parameter(
      names = {"-a", "--analysis-id"},
      required = false)
  private String analysisId;

  @Parameter(
      names = {"-i", "--ignore-undefined-md5"},
      required = false)
  private boolean ignoreUndefinedMd5 = false;

  @NonNull private Registry registry;

  @NonNull private RestClientConfig config;

  @Override
  public void run() throws IOException {
    if (analysisId == null) {
      analysisId = getJson().at("/analysisId").asText("");
    }

    val status = registry.publish(config.getStudyId(), analysisId, ignoreUndefinedMd5);
    save(status);
  }
}
