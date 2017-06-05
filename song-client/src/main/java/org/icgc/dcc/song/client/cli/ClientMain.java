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

import org.icgc.dcc.sodalite.client.command.ConfigCommand;
import org.icgc.dcc.sodalite.client.command.ManifestCommand;
import org.icgc.dcc.sodalite.client.command.PublishCommand;
import org.icgc.dcc.sodalite.client.command.StatusCommand;
import org.icgc.dcc.sodalite.client.command.UploadCommand;
import org.icgc.dcc.sodalite.client.config.Config;
import org.icgc.dcc.sodalite.client.register.Registry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import lombok.val;

@Component
public class ClientMain implements CommandLineRunner {

  private CommandParser dispatcher;

  @Autowired
  ClientMain(Config config, Registry registry) {
    val programName = config.getProgramName();
    val options = new Options();

    val builder = new CommandParserBuilder(programName, options);
    builder.register("config", new ConfigCommand(config));
    builder.register("upload", new UploadCommand(registry));
    builder.register("status", new StatusCommand(registry, config));
    builder.register("publish", new PublishCommand(registry, config));
    builder.register("manifest", new ManifestCommand(registry, config));
    this.dispatcher = builder.build();
  }

  @Override
  public void run(String... args) {
    val command = dispatcher.parse(args);
    command.run();
    command.report();
  }

}
