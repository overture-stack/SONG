/*
 * Copyright (c) 2017 The Ontario Institute for Cancer Research. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the GNU Public License v3.0.
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
 * SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER
 * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
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