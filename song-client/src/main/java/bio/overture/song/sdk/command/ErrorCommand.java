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
package bio.overture.song.sdk.command;

import java.io.IOException;

/** */
public class ErrorCommand extends Command {

  /**
   * All this command does is set the error string for display to stderr later.
   *
   * @param args
   */
  public ErrorCommand(String msg) {
    super();
    err(msg);
  }

  @Override
  public void run() throws IOException {
    // no-op
  }
}
