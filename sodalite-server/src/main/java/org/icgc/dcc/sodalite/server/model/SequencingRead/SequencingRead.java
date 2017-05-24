
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

package org.icgc.dcc.sodalite.server.model.SequencingRead;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "analysisId", "aligned", "alignmentTool", "insertSize", "libraryStrategy", "pairedEnd", "referenceGenome"
})

public class SequencingRead {

  @JsonProperty("analysisId")
  private String analysisId;
  @JsonProperty("aligned")
  private boolean aligned;
  @JsonProperty("alignmentTool")
  private String alignmentTool;
  @JsonProperty("insertSize")
  private int insertSize;
  @JsonProperty("libraryStrategy")
  private LibraryStrategy libraryStrategy;
  @JsonProperty("pairedEnd")
  private boolean pairedEnd;
  @JsonProperty("referenceGenome")
  private String referenceGenome;

  @JsonIgnore
  private Map<String, Object> additionalProperties = new HashMap<String, Object>();

  @JsonProperty("analysisId")
  public String getAnalysisId() {
    return analysisId;
  }

  @JsonProperty("analysisId")
  public void setAnalysisId(String analysisId) {
    this.analysisId = analysisId;
  }

  public SequencingRead withAnalysisId(String analysisId) {
    this.analysisId = analysisId;
    return this;
  }

  @JsonProperty("aligned")
  public boolean isAligned() {
    return aligned;
  }

  @JsonProperty("aligned")
  public void setAligned(boolean aligned) {
    this.aligned = aligned;
  }

  public SequencingRead withAligned(boolean aligned) {
    this.aligned = aligned;
    return this;
  }

  @JsonProperty("alignmentTool")
  public String getAlignmentTool() {
    return alignmentTool;
  }

  @JsonProperty("alignmentTool")
  public void setAlignmentTool(String alignmentTool) {
    this.alignmentTool = alignmentTool;
  }

  public SequencingRead withAlignmentTool(String alignmentTool) {
    this.alignmentTool = alignmentTool;
    return this;
  }

  @JsonProperty("insertSize")
  public int getInsertSize() {
    return insertSize;
  }

  @JsonProperty("insertSize")
  public void setInsertSize(int insertSize) {
    this.insertSize = insertSize;
  }

  public SequencingRead withInsertSize(int insertSize) {
    this.insertSize = insertSize;
    return this;
  }

  @JsonProperty("libraryStrategy")
  public LibraryStrategy getLibraryStrategy() {
    return libraryStrategy;
  }

  @JsonProperty("libraryStrategy")
  public void setLibraryStrategy(LibraryStrategy libraryStrategy) {
    this.libraryStrategy = libraryStrategy;
  }

  public SequencingRead withLibraryStrategy(LibraryStrategy libraryStrategy) {
    this.libraryStrategy = libraryStrategy;
    return this;
  }

  @JsonProperty("pairedEnd")
  public boolean isPairedEnd() {
    return pairedEnd;
  }

  @JsonProperty("pairedEnd")
  public void setPairedEnd(boolean pairedEnd) {
    this.pairedEnd = pairedEnd;
  }

  public SequencingRead withPairedEnd(boolean pairedEnd) {
    this.pairedEnd = pairedEnd;
    return this;
  }

  @JsonProperty("referenceGenome")
  public String getReferenceGenome() {
    return referenceGenome;
  }

  @JsonProperty("referenceGenome")
  public void setReferenceGenome(String referenceGenome) {
    this.referenceGenome = referenceGenome;
  }

  public SequencingRead withReferenceGenome(String referenceGenome) {
    this.referenceGenome = referenceGenome;
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

  public SequencingRead withAdditionalProperty(String name, Object value) {
    this.additionalProperties.put(name, value);
    return this;
  }
}
