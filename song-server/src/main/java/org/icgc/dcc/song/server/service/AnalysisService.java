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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.icgc.dcc.song.server.kafka.AnalysisMessage;
import org.icgc.dcc.song.server.kafka.Sender;
import org.icgc.dcc.song.server.model.SampleSet;
import org.icgc.dcc.song.server.model.SampleSetPK;
import org.icgc.dcc.song.server.model.ScoreObject;
import org.icgc.dcc.song.server.model.analysis.AbstractAnalysis;
import org.icgc.dcc.song.server.model.analysis.Analysis;
import org.icgc.dcc.song.server.model.analysis.SequencingReadAnalysis;
import org.icgc.dcc.song.server.model.analysis.VariantCallAnalysis;
import org.icgc.dcc.song.server.model.entity.composites.CompositeEntity;
import org.icgc.dcc.song.server.model.entity.file.impl.File;
import org.icgc.dcc.song.server.model.enums.AnalysisStates;
import org.icgc.dcc.song.server.model.enums.AnalysisTypes;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.String.format;
import static org.icgc.dcc.common.core.util.Joiners.COMMA;
import static org.icgc.dcc.common.core.util.stream.Collectors.toImmutableList;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.ANALYSIS_ID_NOT_FOUND;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.ANALYSIS_MISSING_FILES;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.ANALYSIS_MISSING_SAMPLES;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.DUPLICATE_ANALYSIS_ATTEMPT;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.ENTITY_NOT_RELATED_TO_STUDY;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.MISMATCHING_STORAGE_OBJECT_CHECKSUMS;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.MISMATCHING_STORAGE_OBJECT_SIZES;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.MISSING_STORAGE_OBJECTS;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.SEQUENCING_READ_NOT_FOUND;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.UNKNOWN_ERROR;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.VARIANT_CALL_NOT_FOUND;
import static org.icgc.dcc.song.core.exceptions.ServerException.buildServerException;
import static org.icgc.dcc.song.core.exceptions.ServerException.checkServer;
import static org.icgc.dcc.song.core.utils.JsonUtils.toJson;
import static org.icgc.dcc.song.core.utils.Responses.ok;
import static org.icgc.dcc.song.server.kafka.AnalysisMessage.createAnalysisMessage;
import static org.icgc.dcc.song.server.model.SampleSetPK.createSampleSetPK;
import static org.icgc.dcc.song.server.model.enums.AnalysisStates.PUBLISHED;
import static org.icgc.dcc.song.server.model.enums.AnalysisStates.SUPPRESSED;
import static org.icgc.dcc.song.server.model.enums.AnalysisStates.UNPUBLISHED;
import static org.icgc.dcc.song.server.model.enums.Constants.SEQUENCING_READ_TYPE;
import static org.icgc.dcc.song.server.model.enums.Constants.VARIANT_CALL_TYPE;
import static org.icgc.dcc.song.server.repository.search.SearchTerm.createMultiSearchTerms;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalysisService {

  private static final Map<String, Class<? extends AbstractAnalysis>> ANALYSIS_CLASS_MAP =
      new HashMap<String, Class<? extends AbstractAnalysis>>(){{
        put(AnalysisTypes.SEQUENCING_READ.toString(), SequencingReadAnalysis.class);
        put(AnalysisTypes.VARIANT_CALL.toString(), VariantCallAnalysis.class);
      }};

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
  private final ScoreService scoreService;
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

  public String create(String studyId, AbstractAnalysis a, boolean ignoreAnalysisIdCollisions) {
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
    checkServer(!isAnalysisExist(id), this.getClass(), DUPLICATE_ANALYSIS_ATTEMPT,
        "Attempted to create a duplicate analysis. Please "
            + "delete the analysis for analysisId '%s' and re-save", id);
    a.setAnalysisId(id);
    a.setStudy(studyId);

    val analysesCreateRequest = new Analysis();
    analysesCreateRequest.setWith(a);
    repository.save(analysesCreateRequest);
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
    sendAnalysisMessage(createAnalysisMessage(id, UNPUBLISHED));
   return id;
  }

  @Transactional
  public ResponseEntity<String> updateAnalysis(String studyId, AbstractAnalysis analysis) {
    val id = analysis.getAnalysisId();
    sampleSetRepository.deleteAllBySampleSetPK_AnalysisId(id);
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
  public List<AbstractAnalysis> getAnalysis(@NonNull String studyId) {
    val analysisList = repository.findAllByStudy(studyId)
        .stream()
        .map(this::cloneAnalysis)
        .collect(toImmutableList());
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
  public List<AbstractAnalysis> idSearch(@NonNull String studyId, @NonNull IdSearchRequest request){
    val analysisList = searchRepository.idSearch(studyId,request).stream()
        .map(AbstractAnalysis::getAnalysisId)
        .map(x -> securedDeepRead(studyId, x))
        .collect(toImmutableList());
    if (analysisList.isEmpty()){
      studyService.checkStudyExist(studyId);
    }
    return analysisList;
  }

  public List<InfoSearchResponse> infoSearch(@NonNull String studyId,
      boolean includeInfo, @NonNull MultiValueMap<String, String> multiKeyValueMap){
    val searchTerms = multiKeyValueMap.entrySet()
        .stream()
        .map(x -> createMultiSearchTerms(x.getKey(), x.getValue()))
        .flatMap(Collection::stream)
        .collect(toImmutableList());
    return searchRepository.infoSearch(studyId, includeInfo, searchTerms);
  }

  public List<InfoSearchResponse> infoSearch(@NonNull String studyId,
      @NonNull InfoSearchRequest request){
    return searchRepository.infoSearch(studyId, request.isIncludeInfo(), request.getSearchTerms());
  }

  public boolean isAnalysisExist(@NonNull String id){
    return repository.existsById(id);
  }

  public void checkAnalysisExists(String id){
    validateAnalysisExistence(isAnalysisExist(id), id);
  }

  public void checkAnalysisAndStudyRelated(@NonNull String studyId, @NonNull String id){
    val numAnalyses = repository.countAllByStudyAndAnalysisId(studyId, id);
    if (numAnalyses < 1){
      studyService.checkStudyExist(studyId);
      val analysis = shallowRead(id);
      throw buildServerException(getClass(), ENTITY_NOT_RELATED_TO_STUDY,
          "The analysisId '%s' is not related to the input studyId '%s'. It is actually related to studyId '%s'",
          id, studyId, analysis.getStudy() );
    }
  }

  /**
   * Unsecurely reads an analysis WITH all of its files, samples and info,
   * but does not verify if the studyId used in the request is allowed to read this analysis
   */
  public AbstractAnalysis unsecuredDeepRead(@NonNull String id) {
    val analysis = shallowRead(id);
    analysis.setInfo(analysisInfoService.readNullableInfo(id));

    analysis.setFile(unsecuredReadFiles(id));
    analysis.setSample(readSamples(id));
    return cloneAnalysis(analysis);
  }

  /**
   * Securely reads an analysis WITH all of its files, samples and info, and verifies the input
   * studyId is related to the requested analysisId
   */
  public AbstractAnalysis securedDeepRead(@NonNull String studyId, String id) {
    checkAnalysisAndStudyRelated(studyId, id);
    return unsecuredDeepRead(id);
  }

  public List<File> securedReadFiles(@NonNull String studyId, String id) {
    checkAnalysisAndStudyRelated(studyId, id);
    return unsecuredReadFiles(id);
  }

  public List<File> unsecuredReadFiles(@NonNull String id) {
    val files = fileRepository.findAllByAnalysisId(id).stream()
        .peek(f -> f.setInfo(fileInfoService.readNullableInfo(f.getObjectId()) ))
        .collect(toImmutableList());

    // Check there are files, and if not throw an exception
    if (files.isEmpty()){
      checkAnalysisExists(id);
      throw buildServerException(getClass(), ANALYSIS_MISSING_FILES,
          "The analysis with analysisId '%s' is missing files and is therefore corrupted. "
              + "It should contain at least 1 file", id);
    }
    return files;
  }

  @Transactional
  public ResponseEntity<String> publish(@NonNull String accessToken,
      @NonNull String studyId, @NonNull String id, boolean ignoreUndefinedMd5) {
    checkAnalysisAndStudyRelated(studyId, id);
    val files = unsecuredReadFiles(id);
    checkMissingFiles(accessToken, id, files);
    val file2scoreObjectMap = getScoreObjectsForFiles(accessToken, files);
    checkMismatchingFileSizes(id, file2scoreObjectMap);
    checkMismatchingFileMd5sums(id, file2scoreObjectMap, ignoreUndefinedMd5 );
    checkedUpdateState(id, PUBLISHED);
    return ok("AnalysisId %s successfully published", id);
  }

  @Transactional
  public ResponseEntity<String> suppress(@NonNull String studyId, @NonNull String id) {
    checkAnalysisAndStudyRelated(studyId, id);
    checkedUpdateState(id, SUPPRESSED);
    return ok("AnalysisId %s was suppressed",id);
  }

  public List<CompositeEntity> readSamples(String id) {
    val samples = sampleSetRepository.findAllBySampleSetPK_AnalysisId(id).stream()
        .map(SampleSet::getSampleSetPK)
        .map(SampleSetPK::getSampleId)
        .map(compositeEntityService::read)
        .collect(toImmutableList());

    // If there are no samples, check that the analysis even exists. If it does, then the analysis is corrupted since
    // it is missing samples
    if (samples.isEmpty()){
      checkAnalysisExists(id);
      throw buildServerException(getClass(), ANALYSIS_MISSING_SAMPLES,
          "The analysis with analysisId '%s' is missing samples and is therefore corrupted. "
          + "It should map to at least 1 sample",id);
    }
    return samples;
  }

  @Transactional
  public void securedUpdateState(@NonNull String studyId,
      @NonNull String id, @NonNull AnalysisStates analysisState) {
    checkAnalysisAndStudyRelated(studyId, id);
    checkedUpdateState(id, analysisState);
  }

  public AnalysisStates readState(@NonNull String id){
    checkAnalysisExists(id);
    return repository.findById(id)
        .map(AbstractAnalysis::getAnalysisState)
        .map(AnalysisStates::resolveAnalysisState)
        .get();
  }

  private Map<File, ScoreObject> getScoreObjectsForFiles(String accessToken, List<File> files){
    val map = ImmutableMap.<File, ScoreObject>builder();
    for (val file : files){
      val scoreObject = scoreService.downloadObject(accessToken, file.getObjectId());
      map.put(file, scoreObject);
    }
    return map.build();
  }

  private void checkMissingFiles(String accessToken, String analysisId, List<File> files){
    val missingFileIds = files.stream()
        .filter(f -> !confirmUploaded(accessToken, f.getObjectId()))
        .collect(toImmutableList());
    val isMissingFiles = missingFileIds.size() > 0;
    checkServer(!isMissingFiles,getClass(), MISSING_STORAGE_OBJECTS,
        "The following storage objectIds must be uploaded to the storage server before the "
            + "analysisId %s can be published: %s",
        analysisId, COMMA.join(missingFileIds));
  }

  private List<String> saveCompositeEntities(String studyId, String id, List<CompositeEntity> samples) {
    return samples.stream()
        .map(sample -> compositeEntityService.save(studyId,sample))
        .peek(sampleId -> sampleSetRepository.save(buildSampleSet(id, sampleId)))
        .collect(toImmutableList()) ;
  }

  private SampleSet buildSampleSet(String id, String sampleId){
    val s = new SampleSet();
    s.setSampleSetPK(createSampleSetPK(id, sampleId));
    return s;
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

  private AbstractAnalysis cloneAnalysis(AbstractAnalysis a){
    val newAnalysis = instantiateAnalysis(a.getAnalysisType());
    newAnalysis.setWith(a);
    if (newAnalysis instanceof SequencingReadAnalysis) {
      ((SequencingReadAnalysis) newAnalysis).setExperiment(readSequencingRead(a.getAnalysisId()));
    } else if (newAnalysis instanceof VariantCallAnalysis) {
      ((VariantCallAnalysis) newAnalysis).setExperiment(readVariantCall(a.getAnalysisId()));
    }
    return newAnalysis;
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
    val analysis = shallowRead(id);
    analysis.setAnalysisState(state);
    val analysisUpdateRequest = new Analysis();
    analysisUpdateRequest.setWith(analysis);
    repository.save(analysisUpdateRequest);
    sendAnalysisMessage(createAnalysisMessage(id, analysisState));
  }

  private boolean confirmUploaded(String accessToken, String fileId) {
    return scoreService.isObjectExist(accessToken,fileId);
  }

  /**
   * Adds all child entities for each analysis
   * This method should be watched in case performance becomes a problem.
   * @param analysisList list of Analysis to be updated
   * @return returns a List of analysis with the child entities
   */
  private List<AbstractAnalysis> processAnalysisList(List<AbstractAnalysis> analysisList){
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
  private AbstractAnalysis processAnalysis(AbstractAnalysis analysis) {
    String id = analysis.getAnalysisId();
    analysis.setFile(unsecuredReadFiles(id));
    analysis.setSample(readSamples(id));
    analysis.setInfo(analysisInfoService.readNullableInfo(id));
    return cloneAnalysis(analysis);
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

  private void validateAnalysisExistence(boolean isAnalysisExist, String id){
    checkServer(isAnalysisExist,
        AnalysisService.class, ANALYSIS_ID_NOT_FOUND,
        "The analysisId '%s' was not found", id );
  }

  /**
   * Reads an analysis WITHOUT any files, samples or info
   */
  private AbstractAnalysis shallowRead(String id){
    val analysisResult = repository.findById(id);

    validateAnalysisExistence(analysisResult.isPresent(), id);

    val analysis = analysisResult.get();
    AbstractAnalysis out;
    if (analysis.getAnalysisType().equals(SEQUENCING_READ_TYPE)){
      out = new SequencingReadAnalysis();
    } else if(analysis.getAnalysisType().equals(VARIANT_CALL_TYPE)){
      out = new VariantCallAnalysis();
    } else {
      throw buildServerException(AnalysisService.class, UNKNOWN_ERROR,
          "unknown analysisType: %s", analysis.getAnalysisType());
    }
    out.setWith(analysis);
    return out;
  }

  private void sendAnalysisMessage(AnalysisMessage message){
    sender.send(toJson(message));
  }

  @SneakyThrows
  private static AbstractAnalysis instantiateAnalysis(@NonNull String analysisType){
    return ANALYSIS_CLASS_MAP.get(analysisType).newInstance();
  }

  private static void checkMismatchingFileSizes(String analysisId, Map<File, ScoreObject> fileScoreObjectMap){
    val mismatchingFileSizes = Lists.<String>newArrayList();
    for (val entry : fileScoreObjectMap.entrySet()){
      val file = entry.getKey();
      val scoreObject = entry.getValue();
      val sizeMatch = file.getFileSize().equals(scoreObject.getFileSize());
      if (!sizeMatch){
        mismatchingFileSizes.add(file.getObjectId());
      }
    }
    checkServer(mismatchingFileSizes.isEmpty(), AnalysisService.class,
        MISMATCHING_STORAGE_OBJECT_SIZES,
        "The following file objectIds have mismatching object sizes in the storage server. "
            + "The analysisId '%s' cannot be published until they all match." , analysisId);
  }

  private static void checkMismatchingFileMd5sums(String analysisId, Map<File, ScoreObject> fileScoreObjectMap,
      boolean ignoreUndefinedMd5){
    val mismatchingFileMd5sums = Lists.<String>newArrayList();
    val undefinedMd5ObjectIds = Lists.<String>newArrayList();
    for (val entry : fileScoreObjectMap.entrySet()){
      val file = entry.getKey();
      val scoreObject = entry.getValue();
      val md5Mismatch = !file.getFileMd5sum().equals(scoreObject.getFileMd5sum());
      val objectMd5Undefined = isNullOrEmpty(scoreObject.getFileMd5sum());
      if(objectMd5Undefined){
        undefinedMd5ObjectIds.add(file.getObjectId());
      } else if(md5Mismatch){
        mismatchingFileMd5sums.add(file.getObjectId());
      }
    }

    String message = format(
        "Found files with a mismatching md5 checksum in the storage server. "
            + "Files with an undefined md5 checksum: %s. "
            + "File with a defined and mismatching md5 checksum: %s. "
            + "The analysisId '%s' cannot be published until all "
            + "undefined checksum are ignored and defined checksums are matching",
        COMMA.join(undefinedMd5ObjectIds),
        COMMA.join(mismatchingFileMd5sums),
        analysisId);

    if (ignoreUndefinedMd5){
      message = "[WARNING]: Ignoring objectIds with an undefined MD5 checksum. "+message;
    }

    val noMd5Errors = mismatchingFileMd5sums.isEmpty() && (ignoreUndefinedMd5 || undefinedMd5ObjectIds.isEmpty());
    checkServer(noMd5Errors, AnalysisService.class, MISMATCHING_STORAGE_OBJECT_CHECKSUMS, message);
  }

}
