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

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.icgc.dcc.sodalite.client.config.SodaliteConfig;
import org.icgc.dcc.sodalite.client.json.JsonObject;
import org.icgc.dcc.sodalite.client.model.Manifest;
import org.icgc.dcc.sodalite.client.model.ManifestEntry;
import org.icgc.dcc.sodalite.client.register.Registry;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.SneakyThrows;
import lombok.val;

public class ManifestCommand extends Command {

  private static final String JSON_PATH_TO_FILES = "/payload/study/donor/specimen/sample/files";

  @SneakyThrows
  @Override
  public void run(SodaliteConfig config) {
    if (getArgs().length < 3) {
      err("Usage: sodalite-client manifest <uploadId> <filename>");
      return;
    }

    val registry = new Registry(config);
    val uploadId = getArgs()[1];
    val fileName = getArgs()[2];
    val result = registry.getRegistrationState(config.getStudyId(), uploadId);
    val m = createManifest(uploadId, result);

    Files.write(Paths.get(fileName), m.toString().getBytes());

    output("Wrote manifest file '%s' for uploadId '%s'", fileName, uploadId);
  }

  @SneakyThrows
  Manifest createManifest(String uploadId, String json) {
    val mapper = new ObjectMapper();
    val root = mapper.readTree(json);

    val m = new Manifest(uploadId);

    Iterable<JsonNode> iter = () -> root.at(JSON_PATH_TO_FILES).iterator();
    m.addAll(StreamSupport.stream(iter.spliterator(), false)
        .map(this::jsonNodeToManifestEntry)
        .collect(Collectors.toList()));
    return m;
  }

  ManifestEntry jsonNodeToManifestEntry(JsonNode node) {
    val j = new JsonObject(node);
    val fileId = j.get("objectId");
    val fileName = j.get("fileName");
    val fileMd5 = j.get("fileMd5");

    return new ManifestEntry(fileId, fileName, fileMd5);
  }

}
