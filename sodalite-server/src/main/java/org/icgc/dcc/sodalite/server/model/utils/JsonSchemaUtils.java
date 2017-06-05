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
package org.icgc.dcc.sodalite.server.model.utils;

import java.io.InputStream;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;

import lombok.SneakyThrows;
import lombok.val;

public class JsonSchemaUtils extends JsonDocUtils {

  @SneakyThrows
  public static String getSchemaId(JsonNode schemaRoot) {
    // JSON Schema id field intended to contain a URI
    val rootNode = schemaRoot.path("id");
    if (rootNode.isMissingNode()) {
      throw new IllegalArgumentException("Invalid JSON Schema found: schema missing mandatory id field");
    } else {
      return extractFromSchemaId(rootNode.asText());
    }
  }

  @SneakyThrows
  public static String extractFromSchemaId(String id) {
    int separatorPosition = id.lastIndexOf("/");
    if (separatorPosition < 0) {
      throw new IllegalArgumentException(String.format("Invalid JSON Schema id found: %s", id));
    } else {
      return id.substring(separatorPosition + 1);
    }
  }

  @SneakyThrows
  public static JsonSchema getJsonSchema(JsonNode node) {
    JsonSchemaFactory factory = new JsonSchemaFactory();
    JsonSchema schema = factory.getSchema(node);
    return schema;
  }

  @SneakyThrows
  public static JsonSchema getJsonSchemaFromClasspath(String fileName) {
    JsonSchemaFactory factory = new JsonSchemaFactory();
    InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName);
    JsonSchema schema = factory.getSchema(is);
    return schema;
  }
}
