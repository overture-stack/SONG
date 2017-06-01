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

package org.icgc.dcc.sodalite.server.model.entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.icgc.dcc.sodalite.server.model.enums.DonorGender;
import org.icgc.dcc.sodalite.server.utils.JsonUtils;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.val;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Donor {

  @NonNull
  private String donorId;
  @NonNull
  private String donorSubmitterId;
  @NonNull
  private String studyId;
  @NonNull
  private DonorGender donorGender;

  @JsonAnySetter
  private Map<String, Object> donorInfo = new HashMap<>();

  private Collection<Specimen> specimens = new ArrayList<>();

  public String getDonorGender() {
    return donorGender.value();
  }

  @SneakyThrows
  public String getDonorInfo() {
    return JsonUtils.mapper().writeValueAsString(donorInfo);
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  public void addDonorInfo(String json) {
    val info = JsonUtils.mapper().convertValue(json, donorInfo.getClass());
    donorInfo.putAll(info);
  }

  public void addSpecimen(Specimen specimen) {
    specimens.add(specimen);
  }

  @JsonProperty("specimens")
  public Collection<Specimen> getSpecimens() {
    return specimens;
  }

  public void setSpecimens(Collection<Specimen> specimens) {
    this.specimens.clear();
    this.specimens.addAll(specimens);
  }
}
