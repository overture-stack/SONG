/*
 * Copyright (c) 2018. Ontario Institute for Cancer Research
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package bio.overture.song.client.command;

import bio.overture.song.client.cli.Status;
import bio.overture.song.client.config.Config;
import bio.overture.song.client.register.Registry;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Objects.nonNull;
import static bio.overture.song.client.command.rules.ModeRule.createModeRule;
import static bio.overture.song.client.command.rules.ParamTerm.createParamTerm;
import static bio.overture.song.client.command.rules.RuleProcessor.createRuleProcessor;

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
