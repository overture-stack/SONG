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
import bio.overture.song.server.model.analysis.Analysis;
import bio.overture.song.server.model.analysis.AnalysisData;
import bio.overture.song.server.model.dto.Payload;
import bio.overture.song.server.model.entity.FileEntity;
import bio.overture.song.server.model.entity.composites.CompositeEntity;
import bio.overture.song.server.repository.AnalysisDataRepository;
import bio.overture.song.server.repository.AnalysisRepository;
import bio.overture.song.server.repository.AnalysisSchemaRepository;
import bio.overture.song.server.repository.FileRepository;
import bio.overture.song.server.repository.SampleSetRepository;
import bio.overture.song.server.repository.search.IdSearchRequest;
import bio.overture.song.server.repository.search.InfoSearchRequest;
import bio.overture.song.server.repository.search.InfoSearchResponse;
import bio.overture.song.server.repository.search.SearchRepository;
import bio.overture.song.server.repository.specification.AnalysisSpecificationBuilder;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableSet;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;

import javax.transaction.Transactional;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import static java.lang.String.format;
import static org.icgc.dcc.common.core.util.Joiners.COMMA;
import static org.icgc.dcc.common.core.util.stream.Collectors.toImmutableList;
import static org.icgc.dcc.common.core.util.stream.Collectors.toImmutableMap;
import static bio.overture.song.core.exceptions.ServerErrors.ANALYSIS_ID_NOT_FOUND;
import static bio.overture.song.core.exceptions.ServerErrors.ANALYSIS_MISSING_FILES;
import static bio.overture.song.core.exceptions.ServerErrors.ANALYSIS_MISSING_SAMPLES;
import static bio.overture.song.core.exceptions.ServerErrors.ANALYSIS_TYPE_NOT_FOUND;
import static bio.overture.song.core.exceptions.ServerErrors.DUPLICATE_ANALYSIS_ATTEMPT;
import static bio.overture.song.core.exceptions.ServerErrors.ENTITY_NOT_RELATED_TO_STUDY;
import static bio.overture.song.core.exceptions.ServerErrors.MALFORMED_PARAMETER;
import static bio.overture.song.core.exceptions.ServerErrors.MISMATCHING_STORAGE_OBJECT_CHECKSUMS;
import static bio.overture.song.core.exceptions.ServerErrors.MISMATCHING_STORAGE_OBJECT_SIZES;
import static bio.overture.song.core.exceptions.ServerErrors.MISSING_STORAGE_OBJECTS;
import static bio.overture.song.core.exceptions.ServerErrors.SUPPRESSED_STATE_TRANSITION;
import static bio.overture.song.core.exceptions.ServerException.buildServerException;
import static bio.overture.song.core.exceptions.ServerException.checkServer;
import static bio.overture.song.core.model.enums.AnalysisStates.PUBLISHED;
import static bio.overture.song.core.model.enums.AnalysisStates.SUPPRESSED;
import static bio.overture.song.core.model.enums.AnalysisStates.UNPUBLISHED;
import static bio.overture.song.core.model.enums.AnalysisStates.findIncorrectAnalysisStates;
import static bio.overture.song.core.model.enums.AnalysisStates.resolveAnalysisState;
import static bio.overture.song.core.utils.JsonUtils.toJson;
import static bio.overture.song.core.utils.JsonUtils.toJsonNode;
import static bio.overture.song.core.utils.Responses.ok;
import static bio.overture.song.server.kafka.AnalysisMessage.createAnalysisMessage;
import static bio.overture.song.server.repository.search.SearchTerm.createMultiSearchTerms;
import static bio.overture.song.server.service.AnalysisTypeService.parseAnalysisTypeId;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalysisService {

  private static final Joiner SPACED_COMMA = Joiner.on(" , ");
  private static final Set<String> DEFAULT_ANALYSIS_STATES = ImmutableSet.of(PUBLISHED.toString());

  @Value("${song.id}")
  private String songServerId;

  @Autowired private final AnalysisRepository repository;
  @Autowired private final FileInfoService fileInfoService;
  @Autowired private final IdService idService;
  @Autowired private final CompositeEntityService compositeEntityService;
  @Autowired private final FileService fileService;
  @Autowired private final StorageService storageService;
  @Autowired private final SearchRepository searchRepository;
  @Autowired private final Sender sender;
  @Autowired private final StudyService studyService;
  @Autowired private final SampleSetRepository sampleSetRepository;
  @Autowired private final FileRepository fileRepository;
  @Autowired private final AnalysisSchemaRepository analysisSchemaRepository;
  @Autowired private final AnalysisDataRepository analysisDataRepository;

  @Transactional
  public String create(String studyId, Payload payload, boolean ignoreAnalysisIdCollisions) {
    studyService.checkStudyExist(studyId);
    val candidateAnalysisId = payload.getAnalysisId();

    // This doesnt commit the id to the id server
    val id = idService.resolveAnalysisId(candidateAnalysisId, ignoreAnalysisIdCollisions);
    /**
     * [Summary]: Guard from misleading response [Details]: If user attempts to save an uploadId a
     * second time, an error is thrown. This restricts the user from doing updates to the uploadId
     * after saving, and then re-saving. The following edge case explains why an error is thrown
     * instead of returning the existing analysisId: - user does upload1 which defines the
     * analysisId field as AN123 - user does save for upload1 and gets analysisId AN123 - user
     * realizes a mistake, and corrects upload1 which has the analysisId AN123 as explicitly stated
     * - user re-uploads upload1, returning the same uploadId since the analysisId has not changed -
     * user re-saves upload1 and gets the existing analysisId AN123 back. - user thinks they updated
     * the analysis with the re-upload.
     */
    checkServer(
        !isAnalysisExist(id),
        this.getClass(),
        DUPLICATE_ANALYSIS_ATTEMPT,
        "Attempted to create a duplicate analysis. Please "
            + "delete the analysis for analysisId '%s' and re-save",
        id);

    val analysisTypeId = parseAnalysisTypeId(payload.getAnalysisTypeId());
    val analysisSchemaOpt =
        analysisSchemaRepository.findByNameAndVersion(
            analysisTypeId.getName(), analysisTypeId.getVersion());
    checkServer(
        analysisSchemaOpt.isPresent(),
        getClass(),
        ANALYSIS_TYPE_NOT_FOUND,
        "Could not find the analysisType with name = '%s' and version = '%s'",
        analysisTypeId.getName(),
        analysisTypeId.getVersion());
    val analysisSchema = analysisSchemaOpt.get();
    val analysisData = AnalysisData.builder().data(toJsonNode(payload.getData())).build();
    analysisDataRepository.save(analysisData);

    val a = new Analysis();
    a.setFile(payload.getFile());
    a.setSample(payload.getSample());
    a.setAnalysisId(id);
    a.setAnalysisState(UNPUBLISHED.name());
    a.setStudy(payload.getStudy());

    analysisData.setAnalysis(a);
    analysisSchema.addAnalysis(a);

    saveCompositeEntities(studyId, id, a.getSample());
    saveFiles(id, studyId, a.getFile());


    // If there were no errors before, then commit the id to the id server.
    // If the id was created by some other client on the id server in the time
    // between the resolveAnalysisId method and the createAnalysisId method,
    // then commit anyways. Entities have already been created using the id,
    // as well, the probability of a collision is very low
    idService.createAnalysisId(id);
    sendAnalysisMessage(createAnalysisMessage(id, studyId, UNPUBLISHED, songServerId));
    return id;
  }

  @Transactional
  public ResponseEntity<String> updateAnalysis(String studyId, Analysis analysis) {
    val id = analysis.getAnalysisId();
    sampleSetRepository.deleteAllBySampleSetPK_AnalysisId(id);
    saveCompositeEntities(studyId, id, analysis.getSample());
    fileRepository.deleteAllByAnalysisId(id);
    analysis.getFile().forEach(f -> fileInfoService.delete(f.getObjectId()));
    saveFiles(id, studyId, analysis.getFile());
    analysisDataRepository.save(analysis.getAnalysisData());
    return ok("AnalysisId %s was updated successfully", analysis.getAnalysisId());
  }

  /**
   * Gets all analysis for a given study. This method should be watched in case performance becomes
   * a problem.
   *
   * @param studyId the study ID
   * @param analysisStates only return analyses that have values from this non-empty list
   * @return returns a List of analysis with the child entities.
   */
  public List<Analysis> getAnalysisByView(
      @NonNull String studyId, @NonNull Set<String> analysisStates) {
    studyService.checkStudyExist(studyId);
    val finalStates = resolveSelectedAnalysisStates(analysisStates);
    return repository.findAll(
        new AnalysisSpecificationBuilder(true, true)
            .buildByStudyAndAnalysisStates(studyId, finalStates));
  }

  /**
   * NOTE: this was the older implementation that is now used as a reference for testing.It has been
   * replaced with getAnalysisByView. Refer to SONG-338
   */
  @Deprecated
  public List<Analysis> getAnalysis(@NonNull String studyId, @NonNull Set<String> analysisStates) {
    return getAnalysisByView(studyId, analysisStates);
  }

  /**
   * Searches all analysis matching the IdSearchRequest
   *
   * @param request which defines the query
   * @return returns a list of analysis with child entities in response to the search request. If
   *     nothing is found, an empty list is returned.
   */
  public List<Analysis> idSearch(@NonNull String studyId, @NonNull IdSearchRequest request) {
    val analysisList =
        searchRepository.idSearch(studyId, request).stream()
            .map(Analysis::getAnalysisId)
            .map(x -> securedDeepRead(studyId, x))
            .collect(toImmutableList());
    if (analysisList.isEmpty()) {
      studyService.checkStudyExist(studyId);
    }
    return analysisList;
  }

  public List<InfoSearchResponse> infoSearch(
      @NonNull String studyId,
      boolean includeInfo,
      @NonNull MultiValueMap<String, String> multiKeyValueMap) {
    val searchTerms =
        multiKeyValueMap.entrySet().stream()
            .map(x -> createMultiSearchTerms(x.getKey(), x.getValue()))
            .flatMap(Collection::stream)
            .collect(toImmutableList());
    return searchRepository.infoSearch(studyId, includeInfo, searchTerms);
  }

  public List<InfoSearchResponse> infoSearch(
      @NonNull String studyId, @NonNull InfoSearchRequest request) {
    return searchRepository.infoSearch(studyId, request.isIncludeInfo(), request.getSearchTerms());
  }

  public boolean isAnalysisExist(@NonNull String id) {
    return repository.existsById(id);
  }

  public void checkAnalysisExists(String id) {
    validateAnalysisExistence(isAnalysisExist(id), id);
  }

  public void checkAnalysisAndStudyRelated(@NonNull String studyId, @NonNull String id) {
    val numAnalyses = repository.countAllByStudyAndAnalysisId(studyId, id);
    if (numAnalyses < 1) {
      studyService.checkStudyExist(studyId);
      val analysis = shallowRead(id);
      throw buildServerException(
          getClass(),
          ENTITY_NOT_RELATED_TO_STUDY,
          "The analysisId '%s' is not related to the input studyId '%s'. It is actually related to studyId '%s'",
          id,
          studyId,
          analysis.getStudy());
    }
  }

  /**
   * Unsecurely reads an analysis WITH all of its files, samples and info, but does not verify if
   * the studyId used in the request is allowed to read this analysis
   */
  public Analysis unsecuredDeepRead(@NonNull String id) {
    val analysis = shallowRead(id);
    analysis.setFile(unsecuredReadFiles(id));
    analysis.setSample(readSamples(id));
    return analysis;
  }

  /**
   * Securely reads an analysis WITH all of its files, samples and info, and verifies the input
   * studyId is related to the requested analysisId
   */
  public Analysis securedDeepRead(@NonNull String studyId, String id) {
    checkAnalysisAndStudyRelated(studyId, id);
    return unsecuredDeepRead(id);
  }

  public List<FileEntity> securedReadFiles(@NonNull String studyId, String id) {
    checkAnalysisAndStudyRelated(studyId, id);
    return unsecuredReadFiles(id);
  }

  public List<FileEntity> unsecuredReadFiles(@NonNull String id) {
    val files =
        fileRepository.findAllByAnalysisId(id).stream()
            .peek(f -> f.setInfo(fileInfoService.readNullableInfo(f.getObjectId())))
            .collect(toImmutableList());

    // Check there are files, and if not throw an exception
    if (files.isEmpty()) {
      checkAnalysisExists(id);
      throw buildServerException(
          getClass(),
          ANALYSIS_MISSING_FILES,
          "The analysis with analysisId '%s' is missing files and is therefore corrupted. "
              + "It should contain at least 1 file",
          id);
    }
    return files;
  }

  @Transactional
  public ResponseEntity<String> publish(
      @NonNull String studyId, @NonNull String id, boolean ignoreUndefinedMd5) {
    checkAnalysisAndStudyRelated(studyId, id);
    val files = unsecuredReadFiles(id);
    checkMissingFiles(id, files);
    val file2storageObjectMap = getStorageObjectsForFiles(files);
    checkMismatchingFileSizes(id, file2storageObjectMap);
    checkMismatchingFileMd5sums(id, file2storageObjectMap, ignoreUndefinedMd5);
    checkedUpdateState(id, PUBLISHED);
    return ok("AnalysisId %s successfully published", id);
  }

  @Transactional
  public ResponseEntity<String> unpublish(@NonNull String studyId, @NonNull String id) {
    checkAnalysisAndStudyRelated(studyId, id);
    checkNotSuppressed(
        id,
        "Cannot change the analysis state for analysisId '%s' from '%s' to '%s'",
        id,
        SUPPRESSED,
        UNPUBLISHED);
    checkedUpdateState(id, UNPUBLISHED);
    return ok("AnalysisId %s successfully unpublished", id);
  }

  @Transactional
  public ResponseEntity<String> suppress(@NonNull String studyId, @NonNull String id) {
    checkAnalysisAndStudyRelated(studyId, id);
    checkedUpdateState(id, SUPPRESSED);
    return ok("AnalysisId %s was suppressed", id);
  }

  public List<CompositeEntity> readSamples(String id) {
    val samples =
        sampleSetRepository.findAllBySampleSetPK_AnalysisId(id).stream()
            .map(SampleSet::getSampleSetPK)
            .map(SampleSetPK::getSampleId)
            .map(compositeEntityService::read)
            .collect(toImmutableList());

    // If there are no samples, check that the analysis even exists. If it does, then the analysis
    // is corrupted since
    // it is missing samples
    if (samples.isEmpty()) {
      checkAnalysisExists(id);
      throw buildServerException(
          getClass(),
          ANALYSIS_MISSING_SAMPLES,
          "The analysis with analysisId '%s' is missing samples and is therefore corrupted. "
              + "It should map to at least 1 sample",
          id);
    }
    return samples;
  }

  @Transactional
  public void securedUpdateState(
      @NonNull String studyId, @NonNull String id, @NonNull AnalysisStates analysisState) {
    checkAnalysisAndStudyRelated(studyId, id);
    checkedUpdateState(id, analysisState);
  }

  public AnalysisStates readState(@NonNull String id) {
    checkAnalysisExists(id);
    return repository
        .findById(id)
        .map(Analysis::getAnalysisState)
        .map(AnalysisStates::resolveAnalysisState)
        .get();
  }

  private Map<FileEntity, StorageObject> getStorageObjectsForFiles(List<FileEntity> files) {
    return transformToMap(files, f -> storageService.downloadObject(f.getObjectId()));
  }

  private void checkNotSuppressed(String id, String format, Object... args) {
    checkServer(
        resolveAnalysisState(shallowRead(id).getAnalysisState()) != SUPPRESSED,
        getClass(),
        SUPPRESSED_STATE_TRANSITION,
        format,
        args);
  }

  private void checkMissingFiles(String analysisId, List<FileEntity> files) {
    val missingFileIds =
        files.stream().filter(f -> !confirmUploaded(f.getObjectId())).collect(toImmutableList());
    val isMissingFiles = missingFileIds.size() > 0;
    checkServer(
        !isMissingFiles,
        getClass(),
        MISSING_STORAGE_OBJECTS,
        "The following storage objectIds must be uploaded to the storage server before the "
            + "analysisId %s can be published: %s",
        analysisId,
        COMMA.join(missingFileIds));
  }

  private List<String> saveCompositeEntities(
      String studyId, String id, List<CompositeEntity> samples) {
    return samples.stream()
        .map(sample -> compositeEntityService.save(studyId, sample))
        .peek(sampleId -> sampleSetRepository.save(buildSampleSet(id, sampleId)))
        .collect(toImmutableList());
  }

  private SampleSet buildSampleSet(String id, String sampleId) {
    val s = new SampleSet();
    s.setSampleSetPK(SampleSetPK.createSampleSetPK(id, sampleId));
    return s;
  }

  private List<String> saveFiles(String id, String studyId, List<FileEntity> files) {
    return files.stream().map(f -> fileService.save(id, studyId, f)).collect(toImmutableList());
  }

  private void checkedUpdateState(String id, AnalysisStates analysisState) {
    val state = analysisState.name();
    val analysis = shallowRead(id);
    analysis.setAnalysisState(state);
    val analysisUpdateRequest = new Analysis();
    analysisUpdateRequest.setWith(analysis);
    repository.save(analysisUpdateRequest);
    sendAnalysisMessage(
        createAnalysisMessage(id, analysis.getStudy(), analysisState, songServerId));
  }

  private boolean confirmUploaded(String fileId) {
    return storageService.isObjectExist(fileId);
  }

  private void validateAnalysisExistence(boolean isAnalysisExist, String id) {
    checkServer(
        isAnalysisExist,
        AnalysisService.class,
        ANALYSIS_ID_NOT_FOUND,
        "The analysisId '%s' was not found",
        id);
  }

  /** Reads an analysis WITHOUT any files, samples or info */
  private Analysis shallowRead(String id) {
    val analysisResult =
        repository.findOne(new AnalysisSpecificationBuilder(true, true).buildById(id));

    validateAnalysisExistence(analysisResult.isPresent(), id);
    return analysisResult.get();
  }

  private void sendAnalysisMessage(AnalysisMessage message) {
    sender.send(toJson(message));
  }

  private static void checkMismatchingFileSizes(
      String analysisId, Map<FileEntity, StorageObject> fileStorageObjectMap) {
    val mismatchingFileSizes =
        fileStorageObjectMap.entrySet().stream()
            .filter(x -> isSizeMismatch(x.getKey(), x.getValue()))
            .map(Map.Entry::getKey)
            .map(FileEntity::getObjectId)
            .collect(toImmutableList());

    checkServer(
        mismatchingFileSizes.isEmpty(),
        AnalysisService.class,
        MISMATCHING_STORAGE_OBJECT_SIZES,
        "The following file objectIds have mismatching object sizes in the storage server: [%s]. "
            + "The analysisId '%s' cannot be published until they all match.",
        SPACED_COMMA.join(mismatchingFileSizes),
        analysisId);
  }

  private static void checkMismatchingFileMd5sums(
      String analysisId,
      Map<FileEntity, StorageObject> fileStorageObjectMap,
      boolean ignoreUndefinedMd5) {

    val undefinedMd5ObjectIds =
        fileStorageObjectMap.entrySet().stream()
            .map(Map.Entry::getValue)
            .filter(x -> !x.isMd5Defined())
            .map(StorageObject::getObjectId)
            .collect(toImmutableList());

    val mismatchingFileMd5sums =
        fileStorageObjectMap.entrySet().stream()
            .filter(x -> x.getValue().isMd5Defined())
            .filter(x -> isMd5Mismatch(x.getKey(), x.getValue()))
            .map(Map.Entry::getValue)
            .map(StorageObject::getObjectId)
            .collect(toImmutableList());

    val sb = new StringBuilder();
    if (ignoreUndefinedMd5) {
      sb.append("[WARNING]: Ignoring objectIds with an undefined MD5 checksum. ");
    }

    sb.append(
        format(
            "Found files with a mismatching md5 checksum in the storage server. ",
            mismatchingFileMd5sums.size()));

    if (!undefinedMd5ObjectIds.isEmpty()) {
      if (ignoreUndefinedMd5) {
        sb.append("IGNORED objectIds ");
      } else {
        sb.append("ObjectIds ");
      }
      sb.append(
          format(
              "with an undefined md5 checksum(%s): [%s]. ",
              undefinedMd5ObjectIds.size(), SPACED_COMMA.join(undefinedMd5ObjectIds)));
    }
    if (!mismatchingFileMd5sums.isEmpty()) {
      sb.append(
          format(
              "ObjectIds with a defined and mismatching md5 checksum(%s): [%s]. ",
              mismatchingFileMd5sums.size(), SPACED_COMMA.join(mismatchingFileMd5sums)));
    }
    sb.append(
        format(
            "The analysisId '%s' cannot be published until all files with an undefined checksum ",
            analysisId));
    sb.append("are ignored and ones with a defined checksum are matching");

    val noMd5Errors =
        mismatchingFileMd5sums.isEmpty() && (ignoreUndefinedMd5 || undefinedMd5ObjectIds.isEmpty());
    checkServer(
        noMd5Errors, AnalysisService.class, MISMATCHING_STORAGE_OBJECT_CHECKSUMS, sb.toString());
  }

  private static boolean isMd5Mismatch(FileEntity file, StorageObject storageObject) {
    return !file.getFileMd5sum().equals(storageObject.getFileMd5sum());
  }

  private static boolean isSizeMismatch(FileEntity file, StorageObject storageObject) {
    return !file.getFileSize().equals(storageObject.getFileSize());
  }

  private static <T, R> Map<T, R> transformToMap(List<T> input, Function<T, R> functionCallback) {
    return input.stream().collect(toImmutableMap(x -> x, functionCallback));
  }

  private static Set<String> resolveSelectedAnalysisStates(Set<String> analysisStates) {
    Set<String> finalStates = DEFAULT_ANALYSIS_STATES;
    if (!analysisStates.isEmpty()) {
      val errorSet = findIncorrectAnalysisStates(analysisStates);
      checkServer(
          errorSet.isEmpty(),
          AnalysisService.class,
          MALFORMED_PARAMETER,
          "The following are not AnalysisStates: '%s'",
          Joiner.on("', '").join(errorSet));
      finalStates = analysisStates;
    }
    return ImmutableSet.copyOf(finalStates);
  }
}
