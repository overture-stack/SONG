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
@JsonPropertyOrder({ "sampleId", "sampleSubmitterId", "sampleType", "files"
})
public class Sample implements Entity {

  @JsonProperty("sampleId")
  private String sampleId;

  @JsonProperty("sampleSubmitterId")
  private String sampleSubmitterId;

  @JsonProperty("sampleType")
  private SampleType sampleType;

  @JsonProperty("files")
  private Collection<File> files = null;

  @JsonIgnore
  private Map<String, Object> additionalProperties = new HashMap<String, Object>();

  @JsonProperty("sampleId")
  public String getSampleId() {
    return sampleId;
  }

  @JsonProperty("sampleId")
  public void setSampleId(String sampleId) {
    this.sampleId = sampleId;
  }

  public Sample withSampleId(String sampleId) {
    this.sampleId = sampleId;
    return this;
  }

  @JsonProperty("sampleSubmitterId")
  public String getSampleSubmitterId() {
    return sampleSubmitterId;
  }

  @JsonProperty("sampleSubmitterId")
  public void setSampleSubmitterId(String sampleSubmitterId) {
    this.sampleSubmitterId = sampleSubmitterId;
  }

  public Sample withSampleSubmitterId(String sampleSubmitterId) {
    this.sampleSubmitterId = sampleSubmitterId;
    return this;
  }

  @JsonProperty("sampleType")
  public SampleType getSampleType() {
    return sampleType;
  }

  @JsonProperty("sampleType")
  public void setSampleType(SampleType sampleType) {
    this.sampleType = sampleType;
  }

  public Sample withSampleType(SampleType sampleType) {
    this.sampleType = sampleType;
    return this;
  }

  @JsonProperty("files")
  public Collection<File> getFiles() {
    return files;
  }

  @JsonProperty("files")
  public void setFiles(Collection<File> files) {
    this.files = files;
  }

  public Sample withFiles(Collection<File> files) {
    this.files = files;
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

  public Sample withAdditionalProperty(String name, Object value) {
    this.additionalProperties.put(name, value);
    return this;
  }

  public void addFile(File f) {
    files.add(f);
  }

  public static enum SampleType {

    DNA("DNA"), FFPE_DNA("FFPE DNA"), AMPLIFIED_DNA("Amplified DNA"), RNA("RNA"), TOTAL_RNA("Total RNA"), FFPE_RNA("FFPE RNA");

    private final String value;
    private final static Map<String, SampleType> CONSTANTS = new HashMap<String, SampleType>();

    static {
      for (SampleType c : SampleType.values()) {
        CONSTANTS.put(c.value, c);
      }
    }

    private SampleType(String value) {
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
    public static SampleType fromValue(String value) {
      SampleType constant = CONSTANTS.get(value);
      if (constant == null) {
        throw new IllegalArgumentException(value);
      } else {
        return constant;
      }
    }

  }
}
