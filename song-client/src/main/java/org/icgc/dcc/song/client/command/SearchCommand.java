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
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.icgc.dcc.song.client.config.Config;
import org.icgc.dcc.song.client.register.Registry;

@RequiredArgsConstructor
@Parameters(separators = "=", commandDescription = "Search for analysis objects for the current studyId" )
public class SearchCommand extends Command {

  @Parameter(names = { "-f", "--file-id" }, required = false)
  private String fileId;

  @Parameter(names = { "-sa", "--sample-id" }, required = false)
  private String sampleId;

  @Parameter(names = { "-sp", "--specimen-id" }, required = false)
  private String specimenId;

  @Parameter(names = { "-d", "--donor-id" }, required = false)
  private String donorId;

  @NonNull
  Registry registry;

  @NonNull
  Config config;

  @Override
  public void run() {
    val status = registry.search(config.getStudyId(), sampleId, specimenId, donorId, fileId );
    save(status);
  }

}
