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

import static bio.overture.song.client.command.rules.ModeRule.createModeRule;
import static bio.overture.song.client.command.rules.ParamTerm.createParamTerm;
import static bio.overture.song.client.command.rules.RuleProcessor.createRuleProcessor;
import static java.util.Objects.nonNull;

import bio.overture.song.client.cli.Status;
import bio.overture.song.client.config.CustomRestClientConfig;
import bio.overture.song.sdk.SongApi;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import java.io.IOException;
import java.util.Objects;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor
@Parameters(
    separators = "=",
    commandDescription = "Search for analysis objects for the current studyId")
public class SearchCommand extends Command {

  /** Mode Name Constants */
  private static final String ANALYSIS_MODE = "ANALYSIS_MODE";

  private static final String ID_MODE = "ID_MODE";

  /** Short Switch Constants */
  private static final String F_SWITCH = "-f";

  private static final String SA_SWITCH = "-sa";
  private static final String SP_SWITCH = "-sp";
  private static final String D_SWITCH = "-d";
  private static final String A_SWITCH = "-a";

  /** Long Switch Constants */
  private static final String FILE_ID_SWITCH = "--file-id";

  private static final String ANALYSIS_ID_SWITCH = "--analysis-id";

  @Parameter(
      names = {F_SWITCH, FILE_ID_SWITCH},
      required = false)
  private String fileId;

  @Parameter(
      names = {A_SWITCH, ANALYSIS_ID_SWITCH},
      required = false)
  private String analysisId;

  @NonNull private CustomRestClientConfig config;
  @NonNull private SongApi songApi;

  @Override
  public void run() throws IOException {
    val status = checkRules();
    if (!status.hasErrors()) {
      if (isIdSearchMode()) {
        status.outputPrettyJson(songApi.idSearch(config.getStudyId(), fileId));
      } else if (isAnalysisSearchMode()) {
        status.outputPrettyJson(songApi.getAnalysis(config.getStudyId(), analysisId));
      } else {
        status.err("Must define at least one switch for the 'search' command\n");
      }
    }
    save(status);
  }

  private boolean isAnalysisSearchMode() {
    return nonNull(analysisId);
  }

  private boolean isIdSearchMode() {
    return nonNull(fileId);
  }

  private Status checkRules() {
    val fileTerm = createParamTerm(F_SWITCH, FILE_ID_SWITCH, fileId, Objects::nonNull);
    val analysisIdTerm =
        createParamTerm(A_SWITCH, ANALYSIS_ID_SWITCH, analysisId, Objects::nonNull);

    val idSearchMode = createModeRule(ID_MODE, fileTerm);
    val analysisSearchMode = createModeRule(ANALYSIS_MODE, analysisIdTerm);
    val ruleProcessor = createRuleProcessor(idSearchMode, analysisSearchMode);
    return ruleProcessor.check();
  }
}
