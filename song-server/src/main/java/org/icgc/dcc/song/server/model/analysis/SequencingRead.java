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

package org.icgc.dcc.song.server.model.analysis;

import static org.icgc.dcc.song.server.model.enums.Constants.LIBRARY_STRATEGY;
import static org.icgc.dcc.song.server.model.enums.Constants.validate;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonInclude;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;
import lombok.val;
@JsonPropertyOrder({ "analysisType", "analysisId", "aligned", "alignmentTool", "insertSize", "libraryStrategy", "pairedEnd", "info" })
@JsonInclude(JsonInclude.Include.NON_ABSENT)
@Data
public class SequencingRead extends Experiment {
  private String analysisId;
  private boolean aligned;
  private String alignmentTool;
  private int insertSize;
  private String libraryStrategy;
  private boolean pairedEnd;
  private String referenceGenome;

  public static SequencingRead create(String id, boolean aligned, String tool, int size, String strategy,
                               boolean isPaired, String info) {
    val s = new SequencingRead();
    s.setAnalysisId(id);
    s.setAligned(aligned);
    s.setAlignmentTool(tool);
    s.setInsertSize(size);
    s.setLibraryStrategy(strategy);
    s.setPairedEnd(isPaired);
    s.addInfo(info);
    return s;
  }

  public void setLibraryStrategy(String strategy) {
    validate(LIBRARY_STRATEGY, strategy);
    libraryStrategy = strategy;
  }

  //@JsonGetter
  //public String getAnalysisType() {
   // return "sequencingRead";
  //}

}
