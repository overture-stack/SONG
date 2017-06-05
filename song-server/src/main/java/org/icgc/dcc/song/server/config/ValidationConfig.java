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
package org.icgc.dcc.song.server.config;

import java.util.HashMap;
import java.util.Map;

import org.icgc.dcc.song.server.model.utils.JsonDocUtils;
import org.icgc.dcc.song.server.model.utils.JsonSchemaUtils;
import org.icgc.dcc.song.server.validation.SchemaValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.JsonSchema;

import lombok.Data;
import lombok.SneakyThrows;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@Data
public class ValidationConfig {

  private static String[] schemaList =
      { "schemas/create-entity-message.json", "schemas/create-study-message.json", "schemas/upload-sequencingread-message.json", "schemas/upload-variantcall-message.json", "schemas/update-entity-message.json", "schemas/update-sequencingread-message.json", "schemas/update-variantcall-message.json"
      };

  @Bean
  public SchemaValidator schemaValidator() {
    return new SchemaValidator();
  }

  @Bean
  @SneakyThrows
  public Map<String, JsonSchema> schemaCache() {
    val cache = new HashMap<String, JsonSchema>();
    // TODO: Arrays.stream(schemaList)
    for (val schema : schemaList) {
      log.debug("Loading schema {}", schema);
      JsonNode node = JsonDocUtils.getJsonNodeFromClasspath(schema);
      cache.put(JsonSchemaUtils.getSchemaId(node), JsonSchemaUtils.getJsonSchema(node));
    }
    for (val s : cache.keySet()) {
      log.info(s);
    }
    return cache;
  }

}
