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

import org.icgc.dcc.song.client.config.Config;
import org.icgc.dcc.song.client.register.Registry;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor
@Parameters(separators = "=", commandDescription = "Get the status of an upload from it's upload id.")
public class StatusCommand extends Command {

  @Parameter(names = { "-u", "--upload-id" }, required = false)
  private String uploadId;

  @Parameter(names = { "-p", "--ping" }, required = false, description = "Pings the server to see if its connected")
  private boolean ping;

  @NonNull
  private Registry registry;

  @NonNull
  private Config config;

  @Override
  public void run() {
    if (ping){
      val status = registry.isAlive();
      save(status);
    }  else {
      if (uploadId == null) {
        uploadId = getJson().at("/uploadId").asText("");
      }
      val status = registry.getUploadStatus(config.getStudyId(), uploadId);
      save(status);
    }
  }

}
