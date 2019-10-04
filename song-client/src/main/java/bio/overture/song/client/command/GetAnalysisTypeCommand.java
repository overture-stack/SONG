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

import bio.overture.song.sdk.SongApi;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import java.io.IOException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor
@Parameters(separators = "=", commandDescription = "Retrieve schema information")
public class GetAnalysisTypeCommand extends Command {

  private static final String N_SWITCH = "-n";
  private static final String NAME_SWITCH = "--name";
  private static final String VERSION_SWITCH = "--version";
  private static final String V_SWITCH = "-v";
  private static final String UNRENDERED_ONLY_SWITCH = "--unrendered-only";
  private static final String U_SWITCH = "-u";

  @Parameter(
      names = {N_SWITCH, NAME_SWITCH},
      required = true)
  private String name;

  @Parameter(
      names = {V_SWITCH, VERSION_SWITCH},
      required = false)
  private Integer version;

  @Parameter(
      names = {U_SWITCH, UNRENDERED_ONLY_SWITCH},
      required = false)
  private boolean unrenderedOnly;

  @NonNull private SongApi songApi;

  @Override
  public void run() throws IOException {
    val response = songApi.getAnalysisType(name, version, unrenderedOnly);
    prettyOutput(response);
  }
}
