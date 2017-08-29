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
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import org.icgc.dcc.song.client.config.Config;
import org.icgc.dcc.song.client.json.JsonObject;
import org.icgc.dcc.song.client.model.Manifest;
import org.icgc.dcc.song.client.model.ManifestEntry;
import org.icgc.dcc.song.client.register.Registry;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RequiredArgsConstructor
@Parameters(commandDescription = "Generate a manifest file for the analysis with the specified analysis id")
public class ManifestCommand extends Command {

  private static final String JSON_PATH_TO_FILES = "";

  @Parameter(names = { "-a", "--analysis-id" })
  private String analysisId;

  @Parameter(names = { "--file", "-f" }, description = "Filename to save file in (if not set, displays manifest on standard output")
  private String fileName;

  @NonNull
  private Registry registry;

  @NonNull
  private Config config;

  @Override
  @SneakyThrows
  public void run() {
    if (analysisId == null) {
      analysisId = getJson().at("/analysisId").asText("");
    }

    val status = registry.getAnalysisFiles(config.getStudyId(), analysisId);

    if (status.hasErrors()) {
      save(status);
      return;
    }

    val m = createManifest(analysisId, status.getOutputs());

    if(m.getEntries().size() == 0){
      err("[SONG_CLIENT_ERROR]: the analysisId '%s' returned 0 files", analysisId);
    } else if (fileName == null) {
      output(m.toString());
    } else {
      Files.write(Paths.get(fileName), m.toString().getBytes());
      output("Wrote manifest file '%s' for analysisId '%s'", fileName, analysisId);
    }
  }

  @SneakyThrows
  private Manifest createManifest(String analysisId, String json) {
    val mapper = new ObjectMapper();
    val root = mapper.readTree(json);

    val m = new Manifest(analysisId);

    Iterable<JsonNode> iter = () -> root.at(JSON_PATH_TO_FILES).iterator();
    m.addAll(StreamSupport.stream(iter.spliterator(), false)
        .map(this::jsonNodeToManifestEntry)
        .collect(Collectors.toList()));
    return m;
  }

  private ManifestEntry jsonNodeToManifestEntry(JsonNode node) {
    val j = new JsonObject(node);
    val fileId = j.get("objectId");
    val fileName = j.get("fileName");
    val fileMd5 = j.get("fileMd5sum");

    return new ManifestEntry(fileId, fileName, fileMd5);
  }

}
