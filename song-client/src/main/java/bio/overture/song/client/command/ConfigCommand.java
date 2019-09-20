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

import bio.overture.song.client.config.ClientConfig;
import com.beust.jcommander.Parameters;
import java.io.IOException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor
@Parameters(commandDescription = "Show the current configuration settings")
public class ConfigCommand extends Command {

  @NonNull private ClientConfig config;

  @Override
  public void run() throws IOException {
    output("Current configuration:\n");

    @NonNull val url = config.getServerUrl();
    output("URL: %s\n", url);

    @NonNull val id = config.getStudyId();
    output("Study ID: %s\n", id);

    @NonNull val debugEnabled = config.isDebug();
    output("Debug Enabled: %s\n", debugEnabled);
  }
}
