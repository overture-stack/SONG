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
package org.icgc.dcc.sodalite.client.cli;

import java.util.Map;

import org.icgc.dcc.sodalite.client.command.Command;
import org.icgc.dcc.sodalite.client.command.ErrorCommand;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;

import lombok.AllArgsConstructor;
import lombok.val;

@AllArgsConstructor
class CommandParser {

  JCommander jc;
  Map<String, Command> commands;

  /***
   * Parses the command line options, and returns a Command object capable of running those options.
   * 
   * Returns an ErrorCommand object containing a usage message if there was an error in the command line arguments.
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