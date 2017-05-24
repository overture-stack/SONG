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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.*;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "specimenId", "specimenSubmitterId", "specimenClass", "specimenType", "samples"
})
public class Specimen implements Entity {

  @JsonProperty("specimenId")
  private String specimenId;

  @JsonProperty("specimenSubmitterId")
  private String specimenSubmitterId;

  @JsonProperty("specimenClass")
  private SpecimenClass specimenClass;

  @JsonProperty("specimenType")
  private SpecimenType specimenType;

  @JsonProperty("samples")
  private Collection<Sample> samples;

  @JsonIgnore
  private Map<String, Object> additionalProperties = new HashMap<String, Object>();

  @JsonProperty("specimenId")
  public String getSpecimenId() {
    return specimenId;
  }

  @JsonProperty("specimenId")
  public void setSpecimenId(String specimenId) {
    this.specimenId = specimenId;
  }

  public Specimen withSpecimenId(String specimenId) {
    this.specimenId = specimenId;
    return this;
  }

  @JsonProperty("specimenSubmitterId")
  public String getSpecimenSubmitterId() {
    return specimenSubmitterId;
  }

  @JsonProperty("specimenSubmitterId")
  public void setSpecimenSubmitterId(String specimenSubmitterId) {
    this.specimenSubmitterId = specimenSubmitterId;
  }

  public Specimen withSpecimenSubmitterId(String specimenSubmitterId) {
    this.specimenSubmitterId = specimenSubmitterId;
    return this;
  }

  @JsonProperty("specimenClass")
  public SpecimenClass getSpecimenClass() {
    return specimenClass;
  }

  @JsonProperty("specimenClass")
  public void setSpecimenClass(SpecimenClass specimenClass) {
    this.specimenClass = specimenClass;
  }

  public Specimen withSpecimenClass(SpecimenClass specimenClass) {
    this.specimenClass = specimenClass;
    return this;
  }

  @JsonProperty("specimenType")
  public SpecimenType getSpecimenType() {
    return specimenType;
  }

  @JsonProperty("specimenType")
  public void setSpecimenType(SpecimenType specimenType) {
    this.specimenType = specimenType;
  }

  public Specimen withSpecimenType(SpecimenType specimenType) {
    this.specimenType = specimenType;
    return this;
  }

  @JsonProperty("samples")
  public Collection<Sample> getSamples() {
    return samples;
  }

  @JsonProperty("samples")
  public void setSamples(Collection<Sample> samples) {
    this.samples = samples;
  }

  public void addSample(Sample sample) {
    samples.add(sample);
  }

  public Specimen withSamples(Collection<Sample> samples) {
    this.samples = samples;
    return this;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }

  @JsonAnyGetter
  public Map<String, Object> getAdditionalProperties() {
    return this.additionalProperties;
  }

  @JsonAnySetter
  public void setAdditionalProperty(String name, Object value) {
    this.additionalProperties.put(name, value);
  }

  public Specimen withAdditionalProperty(String name, Object value) {
    this.additionalProperties.put(name, value);
    return this;
  }

  public static enum SpecimenClass {

    NORMAL("Normal"), TUMOUR("Tumour"), ADJACENT_NORMAL("Adjacent normal");

    private final String value;
    private final static Map<String, SpecimenClass> CONSTANTS = new HashMap<String, SpecimenClass>();

    static {
      for (SpecimenClass c : SpecimenClass.values()) {
        CONSTANTS.put(c.value, c);
      }
    }

    private SpecimenClass(String value) {
      this.value = value;
    }

    @Override
    public String toString() {
      return this.value;
    }

    @JsonValue
    public String value() {
      return this.value;
    }

    @JsonCreator
    public static SpecimenClass fromValue(String value) {
      SpecimenClass constant = CONSTANTS.get(value);
      if (constant == null) {
        throw new IllegalArgumentException(value);
      } else {
        return constant;
      }
    }
  }

  public static enum SpecimenType {

    NORMAL_SOLID_TISSUE("Normal - solid tissue"), NORMAL_BLOOD_DERIVED("Normal - blood derived"), NORMAL_BONE_MARROW("Normal - bone marrow"), NORMAL_TISSUE_ADJACENT_TO_PRIMARY("Normal - tissue adjacent to primary"), NORMAL_BUCCAL_CELL("Normal - buccal cell"), NORMAL_EBV_IMMORTALIZED("Normal - EBV immortalized"), NORMAL_LYMPH_NODE("Normal - lymph node"), NORMAL_OTHER("Normal - other"), PRIMARY_TUMOUR_SOLID_TISSUE("Primary tumour - solid tissue"), PRIMARY_TUMOUR_BLOOD_DERIVED_PERIPHERAL_BLOOD("Primary tumour - blood derived (peripheral blood)"), PRIMARY_TUMOUR_BLOOD_DERIVED_BONE_MARROW("Primary tumour - blood derived (bone marrow)"), PRIMARY_TUMOUR_ADDITIONAL_NEW_PRIMARY("Primary tumour - additional new primary"), PRIMARY_TUMOUR_OTHER("Primary tumour - other"), RECURRENT_TUMOUR_SOLID_TISSUE("Recurrent tumour - solid tissue"), RECURRENT_TUMOUR_BLOOD_DERIVED_PERIPHERAL_BLOOD("Recurrent tumour - blood derived (peripheral blood)"), RECURRENT_TUMOUR_BLOOD_DERIVED_BONE_MARROW("Recurrent tumour - blood derived (bone marrow)"), RECURRENT_TUMOUR_OTHER("Recurrent tumour - other"), METASTATIC_TUMOUR_NOS("Metastatic tumour - NOS"), METASTATIC_TUMOUR_LYMPH_NODE("Metastatic tumour - lymph node"), METASTATIC_TUMOUR_METASTASIS_LOCAL_TO_LYMPH_NODE("Metastatic tumour - metastasis local to lymph node"), METASTATIC_TUMOUR_METASTASIS_TO_DISTANT_LOCATION("Metastatic tumour - metastasis to distant location"), METASTATIC_TUMOUR_ADDITIONAL_METASTATIC("Metastatic tumour - additional metastatic"), XENOGRAFT_DERIVED_FROM_PRIMARY_TUMOUR("Xenograft - derived from primary tumour"), XENOGRAFT_DERIVED_FROM_TUMOUR_CELL_LINE("Xenograft - derived from tumour cell line"), CELL_LINE_DERIVED_FROM_TUMOUR("Cell line - derived from tumour"), PRIMARY_TUMOUR_LYMPH_NODE("Primary tumour - lymph node"), METASTATIC_TUMOUR_OTHER("Metastatic tumour - other"), CELL_LINE_DERIVED_FROM_XENOGRAFT_TUMOUR("Cell line - derived from xenograft tumour");

    private final String value;
    private final static Map<String, SpecimenType> CONSTANTS = new HashMap<String, SpecimenType>();

    static {
      for (SpecimenType c : SpecimenType.values()) {
        CONSTANTS.put(c.value, c);
      }
    }

    private SpecimenType(String value) {
      this.value = value;
    }

    @Override
    public String toString() {
      return this.value;
    }

    @JsonValue
    public String value() {
      return this.value;
    }

    @JsonCreator
    public static SpecimenType fromValue(String value) {
      SpecimenType constant = CONSTANTS.get(value);
      if (constant == null) {
        throw new IllegalArgumentException(value);
      } else {
        return constant;
      }
    }
  }
}
