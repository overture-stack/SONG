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

import bio.overture.song.client.cli.Status;
import bio.overture.song.client.register.Registry;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.io.IOException;

@RequiredArgsConstructor
@Parameters(
    separators = "=",
    commandDescription = "Get the status of an upload from it's upload id.")
public class StatusCommand extends Command {

  @Parameter(
      names = {"-p", "--ping"},
      required = false,
      description = "Pings the server to see if its connected")
  private boolean ping;

  @NonNull private Registry registry;

  @Override
  public void run() throws IOException {
    val status = new Status();
    status.output(Boolean.toString(registry.isAlive()));
    save(status);
  }
}
