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

import static java.util.Objects.nonNull;

@RequiredArgsConstructor
@Parameters(separators = "=", commandDescription = "Search for analysis objects for the current studyId" )
public class SearchCommand extends Command {

  private static final String F_SWITCH =  "-f";
  private static final String SA_SWITCH =  "-sa";
  private static final String SP_SWITCH =  "-sp";
  private static final String D_SWITCH =  "-d";
  private static final String A_SWITCH =  "-a";
  private static final String FILE_ID_SWITCH  = "--file-id" ;
  private static final String SAMPLE_ID_SWITCH = "--sample-id" ;
  private static final String SPECIMEN_ID_SWITCH = "--specimen-id" ;
  private static final String DONOR_ID_SWITCH =  "--donor-id" ;
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

  @NonNull
  private Registry registry;

  @NonNull
  private Config config;

  @Override
  public void run() throws IOException {
    val status = checkOnlyAnalysisIdSwitch();
    if (!status.hasErrors()){
      if (isAnalysisIdMode()){
        status.save(registry.getAnalysis(config.getStudyId(), analysisId));
      } else {
        status.save(registry.idSearch(config.getStudyId(), sampleId, specimenId, donorId, fileId ));
      }
    }
    save(status);
  }

  private boolean isAnalysisIdMode(){
    return nonNull(analysisId);
  }

  private Status checkOnlyAnalysisIdSwitch(){
    val status = new Status();
    status.save(checkParamNotDefined(F_SWITCH, FILE_ID_SWITCH, fileId));
    status.save(checkParamNotDefined(SA_SWITCH, SAMPLE_ID_SWITCH, sampleId));
    status.save(checkParamNotDefined(SP_SWITCH, SPECIMEN_ID_SWITCH, specimenId));
    status.save(checkParamNotDefined(D_SWITCH, DONOR_ID_SWITCH, donorId));
    return status;
  }

  private Status checkParamNotDefined(String shortSwitch, String longSwitch,  Object param){
    val status = new Status();
    if (isAnalysisIdMode() && nonNull(param)){
        status.err("the %s/%s switch and the %s/%s switch are mutually exclusive\n",
            shortSwitch,longSwitch, A_SWITCH, ANALYSIS_ID_SWITCH);
    }
    return status;
  }

}
