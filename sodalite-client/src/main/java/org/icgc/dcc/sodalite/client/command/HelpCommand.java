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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import lombok.NonNull;

/**
 * 
 */
public class HelpCommand extends Command {

  String programName;

  List<String> commands;

  public HelpCommand(String programName, Collection<String> commands) {
    this.programName = programName;
    this.commands = new ArrayList<String>();
    this.commands.addAll(commands);
    if (commands.contains("help")) {
      // do nothing
    } else {
      this.commands.add("help");
    }
  }

  @Override
  public void run(String... args) {
    if (args.length == 2) {
      getHelpFor(args[1]);
      return;
    }
    run();
  }

  public void run() {
    output("Usage:\n");
    output("    %s <subcommand>\n\n", programName);
    output("    Valid subcommands are:\n");
    output("    ");
    for (String s : commands) {
      output(s + " ");
    }
    output("\n\n");
    output("    For help on any subcommand, type %s help <subcommand>\n", programName);
  }

  public void getHelpFor(@NonNull String subcommand) {
    if (commands.contains(subcommand)) {
      output("No help available yet for command '%s'", subcommand);
    } else {
      err("'%s' is not a valid subcommand", subcommand);
    }
  }

}
