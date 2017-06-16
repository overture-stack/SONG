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
package org.icgc.dcc.song.server.model;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import com.fasterxml.jackson.annotation.JsonSetter;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import org.icgc.dcc.song.server.utils.JsonUtils;

import com.fasterxml.jackson.annotation.JsonAnySetter;

import lombok.val;
@AllArgsConstructor
@EqualsAndHashCode
public class Metadata {

  private final Map<String, Object> info = new TreeMap<>();

  @JsonAnySetter
  public void setInfo(String key, Object value) {
    info.put(key, value);
  }

  @JsonSetter
  public void setInfo(String info) {
      addInfo(info);
  }

  @JsonSetter
  public String getInfo() {
    return JsonUtils.toJson(info);
  }

  @SuppressWarnings("unchecked")
  public void addInfo(String json) {
    if (json == null || json.equals("")) {
      return;
    }
    Map<String, Object> m;
    try {
      m = JsonUtils.toMap(json);
    } catch (IllegalArgumentException | IOException e) {
      val j = JsonUtils.ObjectNode().put("info", json);
      m = JsonUtils.convertValue(j, Map.class);
    }
   info.putAll(m);
  }


}
