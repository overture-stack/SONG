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

package org.icgc.dcc.sodalite.server.model.analysis;

import java.util.HashMap;
import java.util.Map;

import org.icgc.dcc.sodalite.server.model.enums.AnalysisType;
import org.icgc.dcc.sodalite.server.utils.JsonUtils;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;
import lombok.SneakyThrows;
import lombok.val;

@JsonInclude(JsonInclude.Include.NON_ABSENT)

@Data
public class Analysis {

  String id;
  String study;
  AnalysisType type;
  Map<String, Object> info;

  @JsonAnySetter
  private Map<String, Object> donorInfo = new HashMap<>();

  @SneakyThrows
  public String getDonorInfo() {
    return JsonUtils.toJson(donorInfo);
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  public void addDonorInfo(String json) {
    if (json == null) {
      return;
    }
    val info = JsonUtils.fromJson(json, donorInfo.getClass());
    donorInfo.putAll(info);
  }

}
