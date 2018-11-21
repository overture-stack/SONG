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
package bio.overture.song.server.service;

import bio.overture.song.core.model.enums.AnalysisStates;
import bio.overture.song.server.kafka.AnalysisMessage;
import bio.overture.song.server.kafka.Sender;
import bio.overture.song.server.model.SampleSet;
import bio.overture.song.server.model.SampleSetPK;
import bio.overture.song.server.model.StorageObject;
import bio.overture.song.server.model.analysis.AbstractAnalysis;
import bio.overture.song.server.model.analysis.Analysis;
import bio.overture.song.server.model.analysis.SequencingReadAnalysis;
import bio.overture.song.server.model.analysis.VariantCallAnalysis;
import bio.overture.song.server.model.entity.FileEntity;
import bio.overture.song.server.model.entity.composites.CompositeEntity;
import bio.overture.song.server.model.enums.Constants;
import bio.overture.song.server.model.experiment.SequencingRead;
import bio.overture.song.server.model.experiment.VariantCall;
import bio.overture.song.server.repository.AnalysisRepository;
import bio.overture.song.server.repository.FileRepository;
import bio.overture.song.server.repository.FullViewRepository;
import bio.overture.song.server.repository.SampleSetRepository;
import bio.overture.song.server.repository.SequencingReadRepository;
import bio.overture.song.server.repository.VariantCallRepository;
import bio.overture.song.server.repository.search.IdSearchRequest;
import bio.overture.song.server.repository.search.InfoSearchRequest;
import bio.overture.song.server.repository.search.InfoSearchResponse;
import bio.overture.song.server.repository.search.SearchRepository;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
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
import java.util.Set;
import java.util.function.Function;

import static com.google.common.collect.Iterables.partition;
import static com.google.common.collect.Lists.newArrayList;
import static java.lang.String.format;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;
import static org.icgc.dcc.common.core.util.Joiners.COMMA;
import static org.icgc.dcc.common.core.util.stream.Collectors.toImmutableList;
import static org.icgc.dcc.common.core.util.stream.Collectors.toImmutableMap;
import static org.icgc.dcc.common.core.util.stream.Collectors.toImmutableSet;
import static bio.overture.song.core.exceptions.ServerErrors.ANALYSIS_ID_NOT_FOUND;
import static bio.overture.song.core.exceptions.ServerErrors.ANALYSIS_MISSING_FILES;
import static bio.overture.song.core.exceptions.ServerErrors.ANALYSIS_MISSING_SAMPLES;
import static bio.overture.song.core.exceptions.ServerErrors.DUPLICATE_ANALYSIS_ATTEMPT;
import static bio.overture.song.core.exceptions.ServerErrors.ENTITY_NOT_RELATED_TO_STUDY;
import static bio.overture.song.core.exceptions.ServerErrors.MALFORMED_PARAMETER;
import static bio.overture.song.core.exceptions.ServerErrors.MISMATCHING_STORAGE_OBJECT_CHECKSUMS;
import static bio.overture.song.core.exceptions.ServerErrors.MISMATCHING_STORAGE_OBJECT_SIZES;
import static bio.overture.song.core.exceptions.ServerErrors.MISSING_STORAGE_OBJECTS;
import static bio.overture.song.core.exceptions.ServerErrors.NOT_IMPLEMENTED_YET;
import static bio.overture.song.core.exceptions.ServerErrors.SEQUENCING_READ_NOT_FOUND;
import static bio.overture.song.core.exceptions.ServerErrors.SUPPRESSED_STATE_TRANSITION;
import static bio.overture.song.core.exceptions.ServerErrors.UNKNOWN_ERROR;
import static bio.overture.song.core.exceptions.ServerErrors.VARIANT_CALL_NOT_FOUND;
import static bio.overture.song.core.exceptions.ServerException.buildServerException;
import static bio.overture.song.core.exceptions.ServerException.checkServer;
import static bio.overture.song.core.model.enums.AnalysisStates.PUBLISHED;
import static bio.overture.song.core.model.enums.AnalysisStates.SUPPRESSED;
import static bio.overture.song.core.model.enums.AnalysisStates.UNPUBLISHED;
import static bio.overture.song.core.model.enums.AnalysisStates.findIncorrectAnalysisStates;
import static bio.overture.song.core.model.enums.AnalysisStates.resolveAnalysisState;
import static bio.overture.song.core.utils.JsonUtils.toJson;
import static bio.overture.song.core.utils.Responses.ok;
import static bio.overture.song.server.converter.FullViewConverter.processAnalysisForType;
import static bio.overture.song.server.kafka.AnalysisMessage.createAnalysisMessage;
import static bio.overture.song.server.model.enums.AnalysisTypes.SEQUENCING_READ;
import static bio.overture.song.server.model.enums.AnalysisTypes.VARIANT_CALL;
import static bio.overture.song.server.model.enums.AnalysisTypes.resolveAnalysisType;
import static bio.overture.song.server.repository.search.SearchTerm.createMultiSearchTerms;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalysisService {

  private static final Joiner SPACED_COMMA = Joiner.on(" , ");
  private static final Set<String> DEFAULT_ANALYSIS_STATES = ImmutableSet.of(PUBLISHED.toString());

  private static final Map<String, Class<? extends AbstractAnalysis>> ANALYSIS_CLASS_MAP =
      new HashMap<String, Class<? extends AbstractAnalysis>>(){{
        put(SEQUENCING_READ.toString(), SequencingReadAnalysis.class);
        put(VARIANT_CALL.toString(), VariantCallAnalysis.class);
      }};
  private static final int BATCH_SIZE = 5000;

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
  private final StorageService storageService;
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
  @Autowired
  private final FullViewRepository fullViewRepository;

  @Transactional
  public String create(String studyId, AbstractAnalysis a, boolean ignoreAnalysisIdCollisions) {
    studyService.checkStudyExist(studyId);
    val candidateAnalysisId = a.getAnalysisId();

    // This doesnt commit the id to the id server
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
    // If there were no errors before, then commit the id to the id server.
    // If the id was created by some other client on the id server in the time
    // between the resolveAnalysisId method and the createAnalysisId method,
    // then commit anyways. Entities have already been created using the id,
    // as well, the probability of a collision is very low
    idService.createAnalysisId(id);
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
   * @param analysisStates only return analyses that have values from this non-empty list
   * @return returns a List of analysis with the child entities.
   *
   */
  public List<AbstractAnalysis> getAnalysisByView(@NonNull String studyId, @NonNull Set<String> analysisStates) {
    studyService.checkStudyExist(studyId);
    val finalStates = resolveSelectedAnalysisStates(analysisStates);
    val results = fullViewRepository.findAllByStudyIdAndAnalysisStateIn(studyId, ImmutableList.copyOf(finalStates));
    val analysisTypeMap = results.stream().collect(groupingBy(x -> resolveAnalysisType(x.getAnalysisType())));
    val outputList = ImmutableList.<AbstractAnalysis>builder();

    for (val analysisTypeEntry : analysisTypeMap.entrySet()){
      val analysisType = analysisTypeEntry.getKey();
      val analysisTypeResults = analysisTypeEntry.getValue();
      val analysesForType = processAnalysisForType(analysisTypeResults, analysisType, AnalysisService::instantiateAnalysis);
      val partitions = partition(analysesForType, BATCH_SIZE);

      for (val analysesForPartition : partitions){
        if (analysisType == SEQUENCING_READ){
          processSequencingReadsInPlace(analysesForPartition);
        } else if (analysisType == VARIANT_CALL){
          processVariantCallsInPlace(analysesForPartition);
        } else {
          throw buildServerException(getClass(), NOT_IMPLEMENTED_YET,
              "The analysisType '%s' has not been implemented yet", analysisType);
        }
        outputList.addAll(analysesForPartition);
      }
    }
    return outputList.build();
  }

  /**
   * NOTE: this was the older implementation that is now used as a reference for testing.It has been
   * replaced with getAnalysisByView. Refer to SONG-338
   */
  @Deprecated
  public List<AbstractAnalysis> getAnalysis(@NonNull String studyId, @NonNull Set<String> analysisStates) {

    Set<String> finalStates = DEFAULT_ANALYSIS_STATES;
    if (!analysisStates.isEmpty()) {
      val errorSet = findIncorrectAnalysisStates(analysisStates);
      checkServer(errorSet.isEmpty(), getClass(), MALFORMED_PARAMETER,
          "The following are not AnalysisStates: '%s'", Joiner.on("', '").join(errorSet) );
      finalStates = analysisStates;
    }

    //TODO: this implementation is not efficient since a query is made for each analysisState.
    // Once hibernate is properly integrated with song, we will be able to use the criteria api to create queries.
    val analysisList = finalStates
        .stream()
        .map(state -> repository.findAllByStudyAndAnalysisState(studyId, state))
        .flatMap(Collection::stream)
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

  public List<FileEntity> securedReadFiles(@NonNull String studyId, String id) {
    checkAnalysisAndStudyRelated(studyId, id);
    return unsecuredReadFiles(id);
  }

  public List<FileEntity> unsecuredReadFiles(@NonNull String id) {
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
    val file2storageObjectMap = getStorageObjectsForFiles(accessToken, files);
    checkMismatchingFileSizes(id, file2storageObjectMap);
    checkMismatchingFileMd5sums(id, file2storageObjectMap, ignoreUndefinedMd5 );
    checkedUpdateState(id, PUBLISHED);
    return ok("AnalysisId %s successfully published", id);
  }

  @Transactional
  public ResponseEntity<String> unpublish(@NonNull String studyId, @NonNull String id) {
    checkAnalysisAndStudyRelated(studyId, id);
    checkNotSuppressed(id, "Cannot change the analysis state for analysisId '%s' from '%s' to '%s'",
    id, SUPPRESSED, UNPUBLISHED);
    checkedUpdateState(id, UNPUBLISHED);
    return ok("AnalysisId %s successfully unpublished", id);
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

  private Map<FileEntity, StorageObject> getStorageObjectsForFiles(String accessToken, List<FileEntity> files){
    return transformToMap(files, f -> storageService.downloadObject(accessToken, f.getObjectId()));
  }

  private void checkNotSuppressed(String id, String format, Object...args){
    checkServer(resolveAnalysisState(shallowRead(id).getAnalysisState()) != SUPPRESSED,
        getClass(), SUPPRESSED_STATE_TRANSITION, format, args);
  }

  private void checkMissingFiles(String accessToken, String analysisId, List<FileEntity> files){
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
    s.setSampleSetPK(SampleSetPK.createSampleSetPK(id, sampleId));
    return s;
  }

  private List<String> saveFiles(String id, String studyId, List<FileEntity> files) {
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
    return storageService.isObjectExist(accessToken,fileId);
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
    if (analysis.getAnalysisType().equals(Constants.SEQUENCING_READ_TYPE)){
      out = new SequencingReadAnalysis();
    } else if(analysis.getAnalysisType().equals(Constants.VARIANT_CALL_TYPE)){
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

  private void processSequencingReadsInPlace(List<AbstractAnalysis> analyses){
    val analysisIds = analyses.stream()
        .map(AbstractAnalysis::getAnalysisId)
        .collect(toImmutableSet());
    val srMap = sequencingReadRepository.findAllByAnalysisIdIn(newArrayList(analysisIds))
        .stream()
        .collect(toMap(SequencingRead::getAnalysisId, x->x));
    val srInfoMap = sequencingReadInfoService.getInfoMap(analysisIds);

    analyses.forEach(x -> {
      SequencingReadAnalysis sra = (SequencingReadAnalysis)x;
      SequencingRead sr = srMap.get(x.getAnalysisId());
      sr.setInfo(srInfoMap.get(x.getAnalysisId()));
      sra.setExperiment(sr);
    });
  }

  private void processVariantCallsInPlace(List<AbstractAnalysis> analyses){
    val analysisIds = analyses.stream()
        .map(AbstractAnalysis::getAnalysisId)
        .collect(toImmutableSet());

    val vcMap = variantCallRepository.findAllByAnalysisIdIn(newArrayList(analysisIds))
        .stream()
        .collect(toMap(VariantCall::getAnalysisId, x->x));

    val vcInfoMap = variantCallInfoService.getInfoMap(analysisIds);

    analyses.forEach(x -> {
      VariantCallAnalysis vca = (VariantCallAnalysis)x;
      VariantCall vc = vcMap.get(x.getAnalysisId());
      vc.setInfo(vcInfoMap.get(x.getAnalysisId()));
      vca.setExperiment(vc);
    });
  }

  @SneakyThrows
  private static AbstractAnalysis instantiateAnalysis(@NonNull String analysisType){
    return ANALYSIS_CLASS_MAP.get(analysisType).newInstance();
  }

  private static void checkMismatchingFileSizes(String analysisId, Map<FileEntity, StorageObject> fileStorageObjectMap){
    val mismatchingFileSizes = fileStorageObjectMap.entrySet().stream()
        .filter(x -> isSizeMismatch(x.getKey(), x.getValue()))
        .map(Map.Entry::getKey)
        .map(FileEntity::getObjectId)
        .collect(toImmutableList());

    checkServer(mismatchingFileSizes.isEmpty(), AnalysisService.class,
        MISMATCHING_STORAGE_OBJECT_SIZES,
        "The following file objectIds have mismatching object sizes in the storage server: [%s]. "
            + "The analysisId '%s' cannot be published until they all match." ,
        SPACED_COMMA.join(mismatchingFileSizes), analysisId);
  }

  private static void checkMismatchingFileMd5sums(String analysisId, Map<FileEntity, StorageObject> fileStorageObjectMap,
      boolean ignoreUndefinedMd5){

    val undefinedMd5ObjectIds = fileStorageObjectMap.entrySet().stream()
        .map(Map.Entry::getValue)
        .filter(x -> !x.isMd5Defined())
        .map(StorageObject::getObjectId)
        .collect(toImmutableList());

    val mismatchingFileMd5sums =  fileStorageObjectMap.entrySet().stream()
        .filter(x -> x.getValue().isMd5Defined())
        .filter(x -> isMd5Mismatch(x.getKey(), x.getValue()))
        .map(Map.Entry::getValue)
        .map(StorageObject::getObjectId)
        .collect(toImmutableList());

    val sb = new StringBuilder();
    if (ignoreUndefinedMd5){
      sb.append("[WARNING]: Ignoring objectIds with an undefined MD5 checksum. ");
    }

    sb.append(format("Found files with a mismatching md5 checksum in the storage server. ",
        mismatchingFileMd5sums.size()));

    if (!undefinedMd5ObjectIds.isEmpty()){
      if (ignoreUndefinedMd5){
        sb.append("IGNORED objectIds ");
      }else{
        sb.append("ObjectIds ");
      }
      sb.append(format("with an undefined md5 checksum(%s): [%s]. ",
          undefinedMd5ObjectIds.size(), SPACED_COMMA.join( undefinedMd5ObjectIds)) );
    }
    if (!mismatchingFileMd5sums.isEmpty()){
      sb.append(format("ObjectIds with a defined and mismatching md5 checksum(%s): [%s]. ",
          mismatchingFileMd5sums.size(), SPACED_COMMA.join(mismatchingFileMd5sums)));
    }
    sb.append(format("The analysisId '%s' cannot be published until all files with an undefined checksum ", analysisId));
    sb.append("are ignored and ones with a defined checksum are matching");

    val noMd5Errors = mismatchingFileMd5sums.isEmpty() && (ignoreUndefinedMd5 || undefinedMd5ObjectIds.isEmpty());
    checkServer(noMd5Errors, AnalysisService.class, MISMATCHING_STORAGE_OBJECT_CHECKSUMS, sb.toString());
  }

  private static boolean isMd5Mismatch(FileEntity file, StorageObject storageObject){
    return !file.getFileMd5sum().equals(storageObject.getFileMd5sum());
  }

  private static boolean isSizeMismatch(FileEntity file, StorageObject storageObject){
    return !file.getFileSize().equals(storageObject.getFileSize());
  }

  private static <T, R> Map<T,R> transformToMap(List<T> input, Function<T, R> functionCallback){
    return input.stream()
        .collect(toImmutableMap( x -> x, functionCallback));
  }

  private static Set<String> resolveSelectedAnalysisStates(Set<String> analysisStates){
    Set<String> finalStates = DEFAULT_ANALYSIS_STATES;
    if (!analysisStates.isEmpty()) {
      val errorSet = findIncorrectAnalysisStates(analysisStates);
      checkServer(errorSet.isEmpty(), AnalysisService.class, MALFORMED_PARAMETER,
          "The following are not AnalysisStates: '%s'", Joiner.on("', '").join(errorSet) );
      finalStates = analysisStates;
    }
    return ImmutableSet.copyOf(finalStates);
  }


}
