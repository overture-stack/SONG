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
package org.icgc.dcc.song.client.cli;

import java.util.HashMap;
import java.util.Map;

import org.icgc.dcc.song.client.command.Command;

import com.beust.jcommander.JCommander;

import lombok.val;

/**
 * A class to build CommandParsers
 */
public class CommandParserBuilder {

  private Map<String, Command> commands = new HashMap<String, Command>();
  private JCommander.Builder builder;
  private String programName;

  /***
   * Create a new builder class for a CommandParser
   * @param programName The name to use to identify the main program in the help text.
   * @param options A JCommander annotated class identifying the options for the main program.
   */
  CommandParserBuilder(String programName, Object options) {
    this.programName = programName;
    this.builder = JCommander.newBuilder().addObject(options);
  }

  /***
   * Register a command to recognized by our command parser
   * 
   * @param commandName The command name, as it should appear on the command line
   * @param command A Command class with JCommander annotations to identify all it's valid command line options.
   */
  public void register(String commandName, Command command) {
    commands.put(commandName, command);
    builder.addCommand(commandName, command);
  }

  /***
   * Build our CommandParser for the Commands we have registered.
   * @return A CommandParser object that can parse the registered objects
   */
  public CommandParser build() {
    val jCommander = builder.build();
    jCommander.setProgramName(programName);
    return new CommandParser(jCommander, new HashMap<>(commands));
  }

}