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
package org.icgc.dcc.song.server.service;

import com.google.common.collect.Lists;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.icgc.dcc.song.server.kafka.Sender;
import org.icgc.dcc.song.server.model.SampleSet;
import org.icgc.dcc.song.server.model.analysis.Analysis;
import org.icgc.dcc.song.server.model.analysis.SequencingReadAnalysis;
import org.icgc.dcc.song.server.model.analysis.VariantCallAnalysis;
import org.icgc.dcc.song.server.model.entity.File;
import org.icgc.dcc.song.server.model.entity.composites.CompositeEntity;
import org.icgc.dcc.song.server.model.enums.AnalysisStates;
import org.icgc.dcc.song.server.model.experiment.SequencingRead;
import org.icgc.dcc.song.server.model.experiment.VariantCall;
import org.icgc.dcc.song.server.repository.AnalysisRepository;
import org.icgc.dcc.song.server.repository.FileRepository;
import org.icgc.dcc.song.server.repository.SampleSetRepository;
import org.icgc.dcc.song.server.repository.SequencingReadRepository;
import org.icgc.dcc.song.server.repository.VariantCallRepository;
import org.icgc.dcc.song.server.repository.search.IdSearchRequest;
import org.icgc.dcc.song.server.repository.search.InfoSearchRequest;
import org.icgc.dcc.song.server.repository.search.InfoSearchResponse;
import org.icgc.dcc.song.server.repository.search.SearchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Objects.isNull;
import static org.icgc.dcc.common.core.util.Joiners.COMMA;
import static org.icgc.dcc.common.core.util.stream.Collectors.toImmutableList;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.ANALYSIS_ID_NOT_FOUND;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.ANALYSIS_MISSING_FILES;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.ANALYSIS_MISSING_SAMPLES;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.ANALYSIS_REPOSITORY_CREATE_RECORD;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.ANALYSIS_STATE_UPDATE_FAILED;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.DUPLICATE_ANALYSIS_ATTEMPT;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.SEQUENCING_READ_NOT_FOUND;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.SEQUENCING_READ_REPOSITORY_CREATE_RECORD;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.UNPUBLISHED_FILE_IDS;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.VARIANT_CALL_NOT_FOUND;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.VARIANT_CALL_REPOSITORY_CREATE_RECORD;
import static org.icgc.dcc.song.core.exceptions.ServerException.buildServerException;
import static org.icgc.dcc.song.core.exceptions.ServerException.checkServer;
import static org.icgc.dcc.song.core.utils.Responses.ok;
import static org.icgc.dcc.song.server.model.enums.AnalysisStates.PUBLISHED;
import static org.icgc.dcc.song.server.model.enums.AnalysisStates.SUPPRESSED;
import static org.icgc.dcc.song.server.repository.search.SearchTerm.createMultiSearchTerms;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalysisService {

  @Autowired
  private final AnalysisRepository repository;
  @Autowired
  private final AnalysisInfoService analysisInfoService;
  @Autowired
  private final FileInfoService fileInfoService;
  @Autowired
  private final SequencingReadInfoService sequencingReadInfoService;
  @Autowired
  private final VariantCallInfoService variantCallInfoService;
  @Autowired
  private final IdService idService;
  @Autowired
  private final CompositeEntityService compositeEntityService;
  @Autowired
  private final FileService fileService;
  @Autowired
  private final ExistenceService existence;
  @Autowired
  private final SearchRepository searchRepository;
  @Autowired
  private final Sender sender;
  @Autowired
  private final StudyService studyService;
  @Autowired
  private final SequencingReadRepository sequencingReadRepository;
  @Autowired
  private final VariantCallRepository variantCallRepository;
  @Autowired
  private final SampleSetRepository sampleSetRepository;
  @Autowired
  private final FileRepository fileRepository;

  public boolean doesAnalysisIdExist(String id){
    return repository.existsById(id);
  }

  public String create(String studyId, Analysis a, boolean ignoreAnalysisIdCollisions) {
    studyService.checkStudyExist(studyId);
    val candidateAnalysisId = a.getAnalysisId();
    val id = idService.resolveAnalysisId(candidateAnalysisId, ignoreAnalysisIdCollisions);
    /**
     * [Summary]: Guard from misleading response
     * [Details]: If user attempts to save an uploadId a second time, an error is thrown.
     * This restricts the user from doing updates to the uploadId after saving, and then
     * re-saving. The following edge case explains why an error is thrown instead of returning
     * the existing analysisId:
     *  - user does upload1 which defines the analysisId field as AN123
     *  - user does save for upload1 and gets analysisId AN123
     *  - user realizes a mistake, and corrects upload1 which has the analysisId AN123 as explicitly stated
     *  - user re-uploads upload1, returning the same uploadId since the analysisId has not changed
     *  - user re-saves upload1 and gets the existing analysisId AN123 back.
     *  - user thinks they updated the analysis with the re-upload.
     */
    checkServer(!doesAnalysisIdExist(id), this.getClass(), DUPLICATE_ANALYSIS_ATTEMPT,
        "Attempted to create a duplicate analysis. Please "
            + "delete the analysis for analysisId '%s' and re-save", id);
    a.setAnalysisId(id);
    a.setStudy(studyId);

    repository.save(a);
    analysisInfoService.create(id, a.getInfoAsString());

    saveCompositeEntities(studyId, id, a.getSample() );
    saveFiles(id, studyId, a.getFile());

   if (a instanceof SequencingReadAnalysis) {
     val experiment = ((SequencingReadAnalysis) a).getExperiment();
     createSequencingRead(id, experiment);
   } else if (a instanceof VariantCallAnalysis) {
     val experiment = ((VariantCallAnalysis) a).getExperiment();
     createVariantCall(id, experiment);
   } else {
     // shouldn't be possible if we validated our JSON first...
     throw new IllegalArgumentException("Invalid analysis type");
   }
   sender.send(String.format("{\"analysis_id\": %s, \"state\": \"UNPUBLISHED\"}", id));
   return id;
  }

  private void createSequencingRead(String id, SequencingRead experiment) {
    experiment.setAnalysisId(id);
    sequencingReadRepository.save(experiment);
    sequencingReadInfoService.create(id, experiment.getInfoAsString());
  }

  private void createVariantCall(String id, VariantCall experiment) {
    experiment.setAnalysisId(id);
    variantCallRepository.save(experiment);
    variantCallInfoService.create(id, experiment.getInfoAsString());
  }

  public ResponseEntity<String> updateAnalysis(String studyId, Analysis analysis) {
    val id = analysis.getAnalysisId();
    sampleSetRepository.deleteById(id);
    saveCompositeEntities(studyId, id, analysis.getSample());
    fileRepository.deleteAllByAnalysisId(id);
    analysis.getFile().forEach(f -> fileInfoService.delete(f.getObjectId()));
    saveFiles(id, studyId, analysis.getFile());
    analysisInfoService.update(id, analysis.getInfoAsString());

    if (analysis instanceof SequencingReadAnalysis ) {
      val experiment = ((SequencingReadAnalysis) analysis).getExperiment();
      updateSequencingRead(id, experiment);
    } else if (analysis instanceof VariantCallAnalysis) {
      val experiment = ((VariantCallAnalysis) analysis).getExperiment();
      updateVariantCall(id, experiment);
    }
    return ok("AnalysisId %s was updated successfully", analysis.getAnalysisId());
  }

  /**
   * Gets all analysis for a given study.
   * This method should be watched in case performance becomes a problem.
   * @param studyId the study ID
   * @return returns a List of analysis with the child entities.
   */
  public List<Analysis> getAnalysis(@NonNull String studyId) {
    val analysisList = repository.findAllByStudy(studyId);
    if (analysisList.isEmpty()){
      studyService.checkStudyExist(studyId);
      return analysisList;
    }
    return processAnalysisList(analysisList);
  }

  /**
   * Searches all analysis matching the IdSearchRequest
   * @param request which defines the query
   * @return returns a list of analysis with child entities in response to the search request. If nothing is found,
   *          an empty list is returned.
   */
  public List<Analysis> idSearch(@NonNull String studyId, @NonNull IdSearchRequest request){
//    val analysisList = repository.idSearch(studyId,
//        request.getDonorId(),
//        request.getSpecimenId(),
//        request.getSampleId(),
//        request.getFileId() );
//    if (analysisList.isEmpty()){
//      studyService.checkStudyExist(studyId);
//      return analysisList;
//    }
//    return processAnalysisList(analysisList);
    return newArrayList();
  }

  public List<InfoSearchResponse> infoSearch(@NonNull String studyId,
      boolean includeInfo, @NonNull MultiValueMap<String, String> multiKeyValueMap){
    val searchTerms = multiKeyValueMap.entrySet()
        .stream()
        .map(x -> createMultiSearchTerms(x.getKey(), x.getValue()))
        .flatMap(Collection::stream)
        .collect(toImmutableList());
    return searchRepository.infoSearch(includeInfo, searchTerms);
  }

  public List<InfoSearchResponse> infoSearch(@NonNull String studyId,
      @NonNull InfoSearchRequest request){
    return searchRepository.infoSearch(request.isIncludeInfo(), request.getSearchTerms());
  }

  public Analysis read(String id) {
    val analysis = checkAnalysis(id);
    analysis.setInfo(analysisInfoService.readNullableInfo(id));

    analysis.setFile(readFiles(id));
    analysis.setSample(readSamples(id));

    if (analysis instanceof SequencingReadAnalysis) {
      val experiment = readSequencingRead(id);
      ((SequencingReadAnalysis) analysis).setExperiment(experiment);
    } else if (analysis instanceof VariantCallAnalysis) {
      val experiment =readVariantCall(id);
      ((VariantCallAnalysis) analysis).setExperiment(experiment);
    }

    return analysis;
  }

  public List<File> readFiles(String id) {
    val files = fileRepository.findAllByAnalysisId(id).stream()
        .peek(f -> f.setInfo(fileInfoService.readNullableInfo(f.getObjectId()) ))
        .collect(toImmutableList());

    // If there are no files, check that the analysis even exits, or if the analysis is corrupted
    if (files.isEmpty()){
      checkAnalysis(id);
      throw buildServerException(getClass(), ANALYSIS_MISSING_FILES,
          "The analysis with analysisId '%s' is missing files and is therefore corrupted. "
              + "It should contain at least 1 file", id);
    }
    return files;
  }

  public ResponseEntity<String> publish(@NonNull String accessToken, @NonNull String id) {
    val files = readFiles(id);
    val missingFileIds = files.stream()
        .filter(f -> !confirmUploaded(accessToken, f.getObjectId()))
        .collect(toImmutableList());
    val isMissingFiles = missingFileIds.size() > 0;
    checkServer(!isMissingFiles,getClass(),UNPUBLISHED_FILE_IDS,
        "The following file ids must be published before analysisId %s can be published: %s",
        id, COMMA.join(missingFileIds));

    checkedUpdateState(id, PUBLISHED);
    sender.send(String.format("{\"analysis_id\": %s, \"state\": \"PUBLISHED\"}", id));
    return ok("AnalysisId %s successfully published", id);
  }

  public ResponseEntity<String> suppress(String id) {
    checkedUpdateState(id, SUPPRESSED);
    return ok("AnalysisId %s was suppressed",id);
  }

  public List<CompositeEntity> readSamples(String id) {
    val samples = sampleSetRepository.findAllById(newArrayList(id)).stream()
        .map(SampleSet::getSampleId)
        .map(compositeEntityService::read)
        .collect(toImmutableList());

    // If there are no samples, check that the analysis even exists. If it does, then the analysis is corrupted since
    // it is missing samples
    if (samples.isEmpty()){
      checkAnalysis(id);
      throw buildServerException(getClass(), ANALYSIS_MISSING_SAMPLES,
          "The analysis with analysisId '%s' is missing samples and is therefore corrupted. "
          + "It should map to at least 1 sample",id);
    }
    return samples;
  }

  private List<String> saveCompositeEntities(String studyId, String id, List<CompositeEntity> samples) {
    return samples.stream()
        .map(sample -> compositeEntityService.save(studyId,sample))
        .peek(sampleId -> sampleSetRepository.save(buildSampleSet(id, sampleId)))
        .collect(toImmutableList()) ;
  }

  private SampleSet buildSampleSet(String id, String sampleId){
    return SampleSet.builder()
        .sampleId(sampleId)
        .analysisId(id)
        .build();
  }

  private List<String> saveFiles(String id, String studyId, List<File> files) {
    return files.stream()
        .map(f -> fileService.save(id, studyId, f))
        .collect(toImmutableList());
  }

  private void updateSequencingRead(String id, SequencingRead experiment) {
    sequencingReadRepository.save(experiment);
    sequencingReadInfoService.update(id, experiment.getInfoAsString());
  }

  private void updateVariantCall(String id, VariantCall experiment) {
    variantCallRepository.save(experiment);
    variantCallInfoService.update(id, experiment.getInfoAsString());
  }

  static Analysis checkAnalysis(AnalysisRepository analysisRepository, String id){
    val analysisResult = analysisRepository.findById(id);
    checkServer(analysisResult.isPresent(),
        AnalysisService.class, ANALYSIS_ID_NOT_FOUND,
        "The analysisId '%s' was not found", id );
    return analysisResult.get();

  }

  public Analysis checkAnalysis(String id){
    return checkAnalysis(repository, id);
  }

  SequencingRead readSequencingRead(String id) {
    val result = sequencingReadRepository.findById(id);
    checkServer(result.isPresent(), this.getClass(), SEQUENCING_READ_NOT_FOUND,
        "The SequencingRead with analysisId '%s' was not found", id);
    val experiment = result.get();
    experiment.setInfo(sequencingReadInfoService.readNullableInfo(id));
    return experiment;
  }

  VariantCall readVariantCall(String id) {
    val result = variantCallRepository.findById(id);
    checkServer(result.isPresent(), this.getClass(), VARIANT_CALL_NOT_FOUND,
        "The VariantCall with analysisId '%s' was not found", id);
    val experiment = result.get();
    experiment.setInfo(variantCallInfoService.readNullableInfo(id));
    return experiment;
  }

  private void checkedUpdateState(String id, AnalysisStates analysisState) {
    val state = analysisState.name();
    val analysis = read(id);
    analysis.setAnalysisState(state);
    repository.save(analysis);
  }

  private boolean confirmUploaded(String accessToken, String fileId) {
    return existence.isObjectExist(accessToken,fileId);
  }

  /**
   * Adds all child entities for each analysis
   * This method should be watched in case performance becomes a problem.
   * @param analysisList list of Analysis to be updated
   * @return returns a List of analysis with the child entities
   */
  private List<Analysis> processAnalysisList(List<Analysis> analysisList){
    analysisList.stream()
        .filter(Objects::nonNull)
        .forEach(this::processAnalysis);
    return analysisList;
  }

  /**
   * Adds child entities to analysis
   * This method should be watched in case performance becomes a problem.
   * @param analysis is the Analysis to be updated
   * @return updated analysis with the child entity
   */
  private Analysis processAnalysis(Analysis analysis) {
    String id = analysis.getAnalysisId();
    analysis.setFile(readFiles(id));
    analysis.setSample(readSamples(id));
    analysis.setInfo(analysisInfoService.readNullableInfo(id));

    if (analysis instanceof SequencingReadAnalysis) {
      ((SequencingReadAnalysis) analysis).setExperiment(readSequencingRead(id));
    } else if (analysis instanceof VariantCallAnalysis) {
      ((VariantCallAnalysis) analysis).setExperiment(readVariantCall(id));
    }
    return analysis;
  }

}
