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
 */
package org.icgc.dcc.sodalite.client.command;

import java.util.HashMap;
import java.util.Map;

import org.icgc.dcc.sodalite.client.config.SodaliteConfig;

import lombok.Data;
import lombok.val;

/**
 * A parent class that commands can inherit from. Will probably soon be replaced by JCommander
 */
@Data
public class Command {

  static Map<String, Command> commands = new HashMap<String, Command>();
  public static String[] noArgs = new String[0];
  static {
    commands.put("config", new ConfigCommand());
    commands.put("help", new HelpCommand());
    commands.put("manifest", new ManifestCommand());
    commands.put("register", new RegisterCommand());
    commands.put("status", new StatusCommand());
  }
  private Status status;
  private String[] args;

  protected Command() {
    this.status = new Status();
  }

  /***
   * Return the command corresponding to args
   * 
   * @param args
   * @return If args is length 0, or the first argument is not a valid subcommand, returns an ErrorCommand with an error
   * message.
   * 
   * Otherwise, returns a Command object capable of running the given subcommand.
   */
  public static Command parse(String[] args) {

    if (args.length == 0) {
      return new HelpCommand();
    }
    val cmd = args[0];
    // log.info("Looking up command " + cmd);
    val c = commands.getOrDefault(cmd, new ErrorCommand("Unknown subcommand: " + cmd));
    c.setArgs(args);
    return c;
  }

  /***
   * Convenience method for children to save error message
   * 
   * @param format See String.format
   * @param args
   * 
   * Formats a string and adds it to the output for the command
   */
  Status err(String format, Object... args) {
    status.err(format, args);
    return status;
  }

  /***
   * Convenience method for child classes to save output message
   * 
   * @param format See String.format
   * @param args
   * 
   * Formats a string and adds it to the error message for the command
   */
  void output(String format, Object... args) {
    status.output(format, args);
  }

  /***
   * Define an empty run method for children to implement.
   * @param config
   */
  public void run(SodaliteConfig config) {
  }
}
