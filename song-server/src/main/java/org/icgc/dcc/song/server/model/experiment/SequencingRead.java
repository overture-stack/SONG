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

package org.icgc.dcc.song.server.model.experiment;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.val;

import static org.icgc.dcc.song.server.model.enums.Constants.LIBRARY_STRATEGY;
import static org.icgc.dcc.song.server.model.enums.Constants.validate;

@EqualsAndHashCode(callSuper=true)
@JsonPropertyOrder({ "analysisId", "aligned", "alignmentTool", "insertSize", "libraryStrategy", "pairedEnd", "referenceGenome", "info" })
@JsonInclude(JsonInclude.Include.NON_ABSENT)
@ToString(callSuper = true)
@Data
public class SequencingRead extends Experiment {
  private String analysisId;
  private Boolean aligned;
  private String alignmentTool;
  private Long insertSize;
  private String libraryStrategy;
  private Boolean pairedEnd;
  private String referenceGenome;

  public static SequencingRead create(String id, Boolean aligned, String tool, Long size, String strategy,
                               Boolean isPaired, String genome) {
    val s = new SequencingRead();
    s.setAnalysisId(id);
    s.setAligned(aligned);
    s.setAlignmentTool(tool);
    s.setInsertSize(size);
    s.setLibraryStrategy(strategy);
    s.setPairedEnd(isPaired);
    s.setReferenceGenome(genome);

    return s;
  }

  public void setLibraryStrategy(String strategy) {
    validate(LIBRARY_STRATEGY, strategy);
    libraryStrategy = strategy;
  }

}
