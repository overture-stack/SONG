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
package bio.overture.song.client.cli;

import bio.overture.song.client.command.Command;
import bio.overture.song.client.command.ErrorCommand;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import lombok.AllArgsConstructor;
import lombok.val;

import java.util.Map;

@AllArgsConstructor
class CommandParser {

  JCommander jc;
  Map<String, Command> commands;

  /**
   * * Parses the command line options, and returns a Command object capable of running those
   * options.
   *
   * <p>Returns an ErrorCommand object containing a usage message if there was an error in the
   * command line arguments.
   *
   * @param args
   * @return A Command object for the given command line arguments.
   */
  public Command parse(String[] args) {
    try {
      jc.parse(args);
    } catch (ParameterException e) {
      return usage(e.getMessage());
    }

    // At this point, we can only get valid commands,
    // or null, if no command was entered.
    val cmd = jc.getParsedCommand();

    if (cmd == null) {
      return usage("");
    }

    return commands.get(cmd);
  }

  ErrorCommand usage(String msg) {
    val s = new StringBuilder();
    jc.usage(s);
    s.append(msg);
    return new ErrorCommand(s.toString());
  }
}
