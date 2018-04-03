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
import com.google.common.base.Charsets;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Maps;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.icgc.dcc.song.client.cli.Status;
import org.icgc.dcc.song.client.command.rules.ModeRule;
import org.icgc.dcc.song.client.command.rules.ParamTerm;
import org.icgc.dcc.song.client.register.Registry;
import org.icgc.dcc.song.core.model.ExportedPayload;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.partition;
import static com.google.common.collect.Sets.newHashSet;
import static com.google.common.io.Files.readLines;
import static java.lang.String.format;
import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.exists;
import static java.nio.file.Files.isRegularFile;
import static java.nio.file.Files.newBufferedWriter;
import static java.util.Objects.isNull;
import static java.util.concurrent.Executors.newFixedThreadPool;
import static java.util.concurrent.TimeUnit.SECONDS;
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

  private static final int BATCH_SIZE = 100;

  private static final String STUDY_SWITCH_SHORT = "-s";
  private static final String STUDY_SWITCH_LONG = "--studyId";
  private static final String ANALYSIS_SWITCH_SHORT = "-a";
  private static final String ANALYSIS_SWITCH_LONG = "--analysisIds";
  private static final String INPUT_FILE_SWITCH_SHORT = "-f";
  private static final String INPUT_FILE_SWITCH_LONG= "--inputFile";
  private static final String NUM_THREADS_SWITCH_SHORT = "-t";
  private static final String NUM_THREADS_SWITCH_LONG = "--threads";
  private static final String INCLUDE_ANALYSIS_ID_SHORT = "-ia";
  private static final String INCLUDE_ANALYSIS_ID_LONG = "--include-analysis-id";
  private static final String INCLUDE_OTHER_IDS_SHORT = "-io";
  private static final String INCLUDE_OTHER_IDS_LONG = "--include-other-ids";

  private static final String ANALYSIS_ID = "analysisId";
  private static final String STUDY_MODE = "STUDY_MODE";
  private static final String ANALYSIS_MODE = "ANALYSIS_MODE";
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  private static final int NUM_THREADS_AVAILABLE = Runtime.getRuntime().availableProcessors();
  private static final Function<String, Boolean> IS_STRING_DEFINED_FUNCTION = x -> !isNullOrEmpty(x);
  private static final Function<List<String>, Boolean> IS_STRING_LIST_DEFINED_FUNCTION = x -> !isNull(x) && !x.isEmpty();

  /**
   * Config
   */
  @Parameter(names = { STUDY_SWITCH_SHORT, STUDY_SWITCH_LONG },
  description = "["+STUDY_MODE+"] Export payloads using a studyId")
  private String studyId;

  @Parameter(names = { ANALYSIS_SWITCH_SHORT, ANALYSIS_SWITCH_LONG },
      description = "["+ANALYSIS_MODE+"] Export payloads using analysisIds",
      variableArity = true)
  private List<String> analysisIds = newArrayList();

  @Parameter(names = { INPUT_FILE_SWITCH_SHORT, INPUT_FILE_SWITCH_LONG},
      description = "["+ANALYSIS_MODE+"] Input file containing a single column of analysisIds on each new line")
  private String inputFilename;

  @Parameter(names = { NUM_THREADS_SWITCH_SHORT, NUM_THREADS_SWITCH_LONG},
      description = "["+ANALYSIS_MODE+"] Run "+ANALYSIS_MODE+" with multiple threads for large exports")
  private int numThreads = 1;

  @Parameter(names = { "-o", "--output-dir"},
      description = "Directory to save the export in (if not set, displays the payloads in standard output)",
      required = true)
  private String outputDir;

  @Parameter(names = { INCLUDE_ANALYSIS_ID_SHORT, INCLUDE_ANALYSIS_ID_LONG },
      description = "Include the analysisId field when exporting payloads", arity = 1)
  private boolean includeAnalysisId = true;

  @Parameter(names = { INCLUDE_OTHER_IDS_SHORT, INCLUDE_OTHER_IDS_LONG },
      description = "Include all other Id fields when exporting payloads", arity = 1)
  private boolean includeOtherIds = false;

  /**
   * Dependencies
   */
  @NonNull
  private Registry registry;

  /**
   * State
   */
  private AtomicInteger fileCount = new AtomicInteger(0);
  private ModeRule studyMode;
  private ModeRule analysisMode;
  private ParamTerm<String> inputFileTerm;
  private Stopwatch stopwatch = Stopwatch.createUnstarted();

  @Override
  public void run() {
    registry.checkServerAlive();
    // Process rules
    val ruleStatus = checkRules();
    if (ruleStatus.hasErrors()){
      save(ruleStatus);
      return;
    }
    checkNumThreads();

    // Get data from ExportService
    if (studyMode.isModeDefined()){
      processStudyMode();
    } else if(analysisMode.isModeDefined()){

      // Get a unique analysisIds
      val uniqueAnalysisIds = getUniqueAnalysisIds();

      // Partition and submit each partition to executor
      val futureMap = parallelProcessAnalyses(uniqueAnalysisIds);

      // Summarize
      summarize(futureMap, uniqueAnalysisIds);
    } else {
      throw new IllegalStateException("Unsupported mode");
    }
  }

  private Status checkRules(){
    // Create ParamTerms
    val studyTerm  = createParamTerm(STUDY_SWITCH_SHORT, STUDY_SWITCH_LONG,  studyId, IS_STRING_DEFINED_FUNCTION);
    val threadTerm = createParamTerm(NUM_THREADS_SWITCH_SHORT, NUM_THREADS_SWITCH_LONG, numThreads, x -> numThreads > 1);
    val analysisTerm = createParamTerm(ANALYSIS_SWITCH_SHORT, ANALYSIS_SWITCH_LONG, analysisIds, IS_STRING_LIST_DEFINED_FUNCTION);
    inputFileTerm = createParamTerm( INPUT_FILE_SWITCH_SHORT, INPUT_FILE_SWITCH_LONG, inputFilename, IS_STRING_DEFINED_FUNCTION);

    // Create Rules
    studyMode = createModeRule(STUDY_MODE, studyTerm);
    analysisMode = createModeRule(ANALYSIS_MODE, analysisTerm, threadTerm, inputFileTerm);

    // Process Rules
    val ruleProcessor = createRuleProcessor(studyMode, analysisMode);
    return ruleProcessor.check();
  }

  private void checkNumThreads(){
    if (numThreads > NUM_THREADS_AVAILABLE){
      output("WARNING: selected number of threads (%s) should not be greater than number of available threads (%s)\n"
          , numThreads, NUM_THREADS_AVAILABLE);
    }
  }

  private void processStudyMode(){
    val status = registry.exportStudy(studyId, includeAnalysisId, includeOtherIds);
    val json = status.getOutputs();
    if (status.hasErrors()){
      err(status.getErrors());
      return;
    }
    jsonToDisk("Study("+studyId+")",json);
    output("Successfully exported payloads for the studyId '%s' to output directory '%s'", studyId, outputDir);
  }

  @SneakyThrows
  private List<String> getUniqueAnalysisIds(){
    if (inputFileTerm.isDefined()){
      val filePath = Paths.get(inputFilename);
      checkState(exists(filePath) && isRegularFile(filePath),
          "The path '%s' does not exist or is not a file",
          filePath.toAbsolutePath().toString());
      analysisIds.addAll(readLines(filePath.toFile(), Charsets.UTF_8));
    }

    // Remove duplicates
    return newArrayList(newHashSet(analysisIds));
  }

  @SneakyThrows
  private Map<String, Future<Status>> parallelProcessAnalyses(List<String> uniqueAnalysisIds){
    val executorService = newFixedThreadPool(numThreads);
    // Partition and submit each partition to executor
    int batchCount = 0;
    val partitions = partition(uniqueAnalysisIds, BATCH_SIZE);
    val futureMap = Maps.<String, Future<Status>>newHashMap();
    log.debug("Partitioning {} analysisId export requests into {} batches...",
        uniqueAnalysisIds.size(), partitions.size());
    stopwatch.reset();
    stopwatch.start();
    for (val partition : partitions){
      val batchId = "batch_"+batchCount++;
      futureMap.put(batchId,
          executorService.submit(() -> processAnalysis(batchId, partition)));
      log.debug("Submitted {}", batchId);
    }
    log.debug("Waiting for {} export requests to complete...", partitions.size());
    executorService.shutdown();
    executorService.awaitTermination(5, TimeUnit.HOURS);
    stopwatch.stop();
    log.debug("Downloads have completed, generating summary output..");
    return futureMap;
  }

  @SneakyThrows
  private Status processAnalysis(String name, List<String> analysisIds){
    // Get data from ExportService
    Status exportStatus = new Status();
    try{
      exportStatus = registry.exportAnalyses(analysisIds, includeAnalysisId, includeOtherIds);
    } catch (Throwable t){
      exportStatus.err("ExportError: %s", t.getMessage());
    }

    if (exportStatus.hasErrors()) {
      return exportStatus;
    }

    // Extract payloads and store to outputDir
    val json = exportStatus.getOutputs();
    exportStatus.save(jsonToDisk(name, json));
    return exportStatus;
  }

  private Status jsonToDisk(String batchId, String json){
    val status = new Status();
    try {
      val root = OBJECT_MAPPER.readTree(json);
      val dirPath = Paths.get(outputDir);
      stream(root.iterator())
          .map(j -> fromJson(j, ExportedPayload.class))
          .forEach(x -> exportedPayloadToFile(x, dirPath));
      status.output("Successfully exported payloads for '%s' batch to output directory %s",
          batchId, dirPath.toAbsolutePath().toString());
    } catch(Exception e){
      status.err("ERROR [%s] -- (%s): %s ", batchId, e.getClass().getName(), e.getMessage());
    }
    return status;
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
        fileName = format("payload_%s.json", fileCount.getAndIncrement());
      }
      val filePath = studyDir.resolve(fileName);
      val bw = newBufferedWriter(filePath);
      bw.write(toPrettyJson(jsonNode));
      bw.close();
    }
  }

  @SneakyThrows
  private void summarize(Map<String, Future<Status>> futureMap, List<String> uniqueAnalysisIds){
    val errorStatus = new Status();
    for (val futureEntry : futureMap.entrySet()){
      val batchId = futureEntry.getKey();
      val future = futureEntry.getValue();
      val incomplete = future.isCancelled() || !future.isDone();
      val status = future.get();
      if (status.hasErrors()){
        errorStatus.err("ERROR[%s]: %s\n", batchId, status.getErrors());
      }
      if (incomplete){
        errorStatus.err("ERROR[%s]: The batchId '%s' was terminated prematurely", batchId);
      }
    }
    if (errorStatus.hasErrors()){
      save(errorStatus);
    } else {
      output("Successfully exported all %s analysisIds to output directory '%s'\n",
          uniqueAnalysisIds.size(), outputDir);
    }
    log.debug("[STOPWATCH]: took '%s' seconds to run %s batches, with %s analysisIds per batch and with %s threads",
        stopwatch.elapsed(SECONDS), futureMap.keySet().size(), BATCH_SIZE, numThreads);
  }

}
