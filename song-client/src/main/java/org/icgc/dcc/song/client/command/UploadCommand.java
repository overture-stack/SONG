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
package org.icgc.dcc.song.client.command;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.google.common.base.Charsets;
import com.google.common.io.Files;
import lombok.val;
import org.icgc.dcc.song.client.register.Registry;

import java.io.File;
import java.io.IOException;

@Parameters(separators = "=", commandDescription = "Upload an analysis file, and get an upload id")
public class UploadCommand extends Command {

  @Parameter(names = { "-f", "--file" })
  String fileName;

  @Parameter(names = { "-a", "--async" },description = "Enables asynchronous validation")
  boolean isAsyncValidation = false;

  Registry registry;

  public UploadCommand(Registry registry) {
    this.registry = registry;
  }

  @Override
  public void run() {

    String json;
    try {
      json = readUploadContent();
    } catch (IOException e) {
      err("Error: Input/Output Error '%s'", e.getMessage());
      return;
    }
    val status = registry.upload(json, isAsyncValidation);
    save(status);
  }

  String readUploadContent() throws IOException {
    if (fileName == null) {
      val json=getJson();
      return json.toString();
    }

    val file = new File(fileName);
    return Files.toString(file, Charsets.UTF_8);
  }

}
