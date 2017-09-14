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
package org.icgc.dcc.song.client.command;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.icgc.dcc.song.client.cli.Status;
import org.icgc.dcc.song.client.config.Config;
import org.icgc.dcc.song.client.register.Registry;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Objects.nonNull;
import static org.icgc.dcc.song.client.command.rules.ModeRule.createModeRule;
import static org.icgc.dcc.song.client.command.rules.ParamTerm.createParamTerm;
import static org.icgc.dcc.song.client.command.rules.RuleProcessor.createRuleProcessor;

@RequiredArgsConstructor
@Parameters(separators = "=", commandDescription = "Search for analysis objects for the current studyId" )
public class SearchCommand extends Command {

  /**
   * Mode Name Constants
   */
  private static final String ANALYSIS_MODE = "ANALYSIS_MODE";
  private static final String ID_MODE = "ID_MODE";
  private static final String INFO_MODE = "INFO_MODE";

  /**
   * Short Switch Constants
   */
  private static final String F_SWITCH = "-f";
  private static final String SA_SWITCH = "-sa";
  private static final String SP_SWITCH = "-sp";
  private static final String D_SWITCH = "-d";
  private static final String I_SWITCH = "-i";
  private static final String T_SWITCH = "-t";
  private static final String A_SWITCH =  "-a";

  /**
   * Long Switch Constants
   */
  private static final String FILE_ID_SWITCH = "--file-id";
  private static final String SAMPLE_ID_SWITCH = "--sample-id";
  private static final String SPECIMEN_ID_SWITCH = "--specimen-id";
  private static final String DONOR_ID_SWITCH = "--donor-id";
  private static final String INFO_SWITCH = "--info";
  private static final String SEARCH_TERMS_SWITCH = "--search-terms";
  private static final String ANALYSIS_ID_SWITCH = "--analysis-id" ;


  @Parameter(names = { F_SWITCH, FILE_ID_SWITCH }, required = false)
  private String fileId;

  @Parameter(names = { SA_SWITCH, SAMPLE_ID_SWITCH }, required = false)
  private String sampleId;

  @Parameter(names = { SP_SWITCH, SPECIMEN_ID_SWITCH }, required = false)
  private String specimenId;

  @Parameter(names = { D_SWITCH, DONOR_ID_SWITCH }, required = false)
  private String donorId;

  @Parameter(names = { A_SWITCH, ANALYSIS_ID_SWITCH }, required = false)
  private String analysisId;

  @Parameter(names = { I_SWITCH, INFO_SWITCH }, required = false)
  private boolean includeInfo = false;

  @Parameter(names = { T_SWITCH, SEARCH_TERMS_SWITCH }, required = false, variableArity = true)
  private List<String> infoSearchTerms = newArrayList();

  @NonNull
  private Registry registry;

  @NonNull
  private Config config;

  @Override
  public void run() throws IOException {
    val status = checkRules();
    if ( !status.hasErrors()){
      if (isIdSearchMode()){
        status.save(registry.idSearch(config.getStudyId(), sampleId, specimenId, donorId, fileId ));
      } else if (isInfoSearchMode()) {
        status.save(registry.infoSearch(config.getStudyId(), includeInfo, infoSearchTerms));
      } else if (isAnalysisSearchMode()){
        status.save(registry.getAnalysis(config.getStudyId(), analysisId));
      } else {
        status.err("Must define at least one switch for the 'search' command\n");
      }
    }
    save(status);
  }

  private boolean isInfoSearchMode(){
    return infoSearchTerms.size() > 0;
  }

  private boolean isAnalysisSearchMode(){
    return nonNull(analysisId);
  }

  private boolean isIdSearchMode(){
    return nonNull(fileId)
        || nonNull(sampleId)
        || nonNull(specimenId)
        || nonNull(donorId);
  }

  private Status checkRules() {
    val fileTerm = createParamTerm(F_SWITCH, FILE_ID_SWITCH, fileId, Objects::nonNull);
    val sampleTerm = createParamTerm(SA_SWITCH, SAMPLE_ID_SWITCH, sampleId, Objects::nonNull);
    val specimenTerm = createParamTerm(SP_SWITCH, SPECIMEN_ID_SWITCH, specimenId, Objects::nonNull);
    val donorTerm = createParamTerm(D_SWITCH, DONOR_ID_SWITCH, donorId, Objects::nonNull);
    val analysisIdTerm = createParamTerm(A_SWITCH, ANALYSIS_ID_SWITCH, analysisId, Objects::nonNull);
    val infoTerm = createParamTerm(I_SWITCH, INFO_SWITCH, includeInfo, x -> x);
    val searchTerm = createParamTerm(T_SWITCH, SEARCH_TERMS_SWITCH, infoSearchTerms, x -> x.size() > 0);

    val idSearchMode = createModeRule(ID_MODE, fileTerm, sampleTerm, specimenTerm, donorTerm);
    val infoSearchMode = createModeRule(INFO_MODE, infoTerm, searchTerm);
    val analysisSearchMode = createModeRule(ANALYSIS_MODE, analysisIdTerm);
    val ruleProcessor = createRuleProcessor(idSearchMode, infoSearchMode, analysisSearchMode);
    return ruleProcessor.check();
  }

}
