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
package org.icgc.dcc.song.client.command;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.icgc.dcc.song.client.cli.Status;
import org.icgc.dcc.song.client.register.Registry;
import org.icgc.dcc.song.core.model.ExportedPayload;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.collect.Lists.newArrayList;
import static java.lang.String.format;
import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.exists;
import static java.nio.file.Files.newBufferedWriter;
import static java.util.Objects.isNull;
import static org.icgc.dcc.common.core.util.stream.Streams.stream;
import static org.icgc.dcc.song.client.command.rules.ModeRule.createModeRule;
import static org.icgc.dcc.song.client.command.rules.ParamTerm.createParamTerm;
import static org.icgc.dcc.song.client.command.rules.RuleProcessor.createRuleProcessor;
import static org.icgc.dcc.song.core.utils.JsonUtils.fromJson;
import static org.icgc.dcc.song.core.utils.JsonUtils.toPrettyJson;

@Slf4j
@RequiredArgsConstructor
@Parameters(separators = "=", commandDescription = "Export a payload" )
public class ExportCommand extends Command {

  private static final String STUDY_SWITCH_SHORT = "-s";
  private static final String STUDY_SWITCH_LONG = "--studyId";
  private static final String ANALYSIS_SWITCH_SHORT = "-a";
  private static final String ANALYSIS_SWITCH_LONG = "--analysisIds";
  private static final String ANALYSIS_ID = "analysisId";
  private static final String STUDY_MODE = "STUDY_MODE";
  private static final String ANALYSIS_MODE = "ANALYSIS_MODE";
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  /**
   * Config
   */
  @Parameter(names = { STUDY_SWITCH_SHORT, STUDY_SWITCH_LONG },
  description = "Export payloads using a studyId. Can only be used in "+STUDY_MODE)
  private String studyId;

  @Parameter(names = { ANALYSIS_SWITCH_SHORT, ANALYSIS_SWITCH_LONG },
      description = "Export payloads using analysisIds. Can only be used in "+ANALYSIS_MODE,
      variableArity = true)
  private List<String> analysisIds = newArrayList();

  @Parameter(names = { "-o", "--output-dir"},
      description = "Directory to save the export in (if not set, displays the payloads in standard output)",
      required = true)
  private String outputDir;

  /**
   * Dependencies
   */
  @NonNull
  private Registry registry;

  /**
   * State
   */
  private int fileCount = 0;


  @Override
  public void run() throws IOException {
    // Process rules
    val studyTerm  = createParamTerm(STUDY_SWITCH_SHORT, STUDY_SWITCH_LONG,  studyId, x -> !isNullOrEmpty(x));
    val analysisTerm = createParamTerm(ANALYSIS_SWITCH_SHORT, ANALYSIS_SWITCH_LONG, analysisIds, x -> !isNull(x) && !x.isEmpty());
    val studyMode = createModeRule(STUDY_MODE, studyTerm );
    val analysisMode = createModeRule(ANALYSIS_MODE, analysisTerm );
    val ruleProcessor = createRuleProcessor(studyMode, analysisMode);
    val ruleStatus = ruleProcessor.check();

    if (ruleStatus.hasErrors()){
      save(ruleStatus);
      return;
    }

    // Get data from ExportService
    Status exportStatus;
    if (studyMode.isModeDefined()){
      exportStatus = registry.exportStudy(studyId);
    } else if(analysisMode.isModeDefined()){
      exportStatus = registry.exportAnalyses(analysisIds);
    } else {
      throw new IllegalStateException("Unsupported mode");
    }

    if (exportStatus.hasErrors()) {
      save(exportStatus);
      return;
    }

    // Extract payloads and store to outputDir
    val json = exportStatus.getOutputs();
    val root = OBJECT_MAPPER.readTree(json);
    val dirPath = Paths.get(outputDir);
    stream(root.iterator())
        .map(j -> fromJson(j, ExportedPayload.class))
        .forEach(x -> exportedPayloadToFile(x, dirPath));
    output("Successfully exported payloads to output directory %s", dirPath.toAbsolutePath().toString());
  }

  @SneakyThrows
  private void exportedPayloadToFile(ExportedPayload exportedPayload, Path dirPath){
    val studyDir = dirPath.resolve(exportedPayload.getStudyId());
    if(!exists(studyDir)){
      createDirectories(studyDir);
    }
    for (val jsonNode : exportedPayload.getPayloads()){
      String fileName;
      if(jsonNode.has(ANALYSIS_ID)){
        fileName = format("%s.json", jsonNode.path(ANALYSIS_ID).textValue());
      } else {
        fileName = format("payload_%s.json", fileCount++);
      }
      val filePath = studyDir.resolve(fileName);
      val bw = newBufferedWriter(filePath);
      bw.write(toPrettyJson(jsonNode));
      bw.close();
    }
  }

}
