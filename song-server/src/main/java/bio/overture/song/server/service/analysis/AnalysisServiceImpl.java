/*
 * Copyright (c) 2019. Ontario Institute for Cancer Research
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
package bio.overture.song.server.service.analysis;

import static bio.overture.song.core.exceptions.ServerErrors.*;
import static bio.overture.song.core.exceptions.ServerException.buildServerException;
import static bio.overture.song.core.exceptions.ServerException.checkServer;
import static bio.overture.song.core.model.enums.AnalysisStates.PUBLISHED;
import static bio.overture.song.core.model.enums.AnalysisStates.SUPPRESSED;
import static bio.overture.song.core.model.enums.AnalysisStates.UNPUBLISHED;
import static bio.overture.song.core.model.enums.AnalysisStates.findIncorrectAnalysisStates;
import static bio.overture.song.core.model.enums.AnalysisStates.resolveAnalysisState;
import static bio.overture.song.core.utils.JsonUtils.*;
import static bio.overture.song.core.utils.Separators.COMMA;
import static bio.overture.song.server.model.enums.ModelAttributeNames.*;
import static bio.overture.song.server.utils.JsonSchemas.PROPERTIES;
import static bio.overture.song.server.utils.JsonSchemas.REQUIRED;
import static bio.overture.song.server.utils.JsonSchemas.buildSchema;
import static bio.overture.song.server.utils.JsonSchemas.validateWithSchema;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static java.lang.String.format;
import static java.util.Objects.isNull;

import bio.overture.song.core.model.AnalysisTypeId;
import bio.overture.song.core.model.enums.AnalysisStates;
import bio.overture.song.server.model.SampleSet;
import bio.overture.song.server.model.SampleSetPK;
import bio.overture.song.server.model.StorageObject;
import bio.overture.song.server.model.analysis.*;
import bio.overture.song.server.model.dto.Payload;
import bio.overture.song.server.model.entity.*;
import bio.overture.song.server.model.entity.composites.CompositeEntity;
import bio.overture.song.server.repository.*;
import bio.overture.song.server.repository.search.IdSearchRequest;
import bio.overture.song.server.repository.search.SearchRepository;
import bio.overture.song.server.repository.specification.AnalysisSpecificationBuilder;
import bio.overture.song.server.service.AnalysisTypeService;
import bio.overture.song.server.service.CompositeEntityService;
import bio.overture.song.server.service.FileService;
import bio.overture.song.server.service.InfoService.FileInfoService;
import bio.overture.song.server.service.StorageService;
import bio.overture.song.server.service.StudyService;
import bio.overture.song.server.service.ValidationService;
import bio.overture.song.server.service.id.IdService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.fge.jsonpatch.JsonPatchException;
import com.github.fge.jsonpatch.mergepatch.JsonMergePatch;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableSet;
import io.vavr.Tuple3;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.everit.json.schema.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalysisServiceImpl implements AnalysisService {

  private static final Joiner SPACED_COMMA = Joiner.on(" , ");
  private static final Set<String> DEFAULT_ANALYSIS_STATES = ImmutableSet.of(PUBLISHED.toString());

  @Autowired private final String analysisUpdateBaseJson;

  @Autowired private final AnalysisRepository repository;
  @Autowired private final UpgradedAnalysisRepository upgradedAnalysisRepository;
  @Autowired private final FileInfoService fileInfoService;
  @Autowired private final IdService idService;
  @Autowired private final CompositeEntityService compositeEntityService;
  @Autowired private final FileService fileService;
  @Autowired private final StorageService storageService;
  @Autowired private final SearchRepository searchRepository;
  @Autowired private final StudyService studyService;
  @Autowired private final SampleSetRepository sampleSetRepository;
  @Autowired private final FileRepository fileRepository;
  @Autowired private final AnalysisDataRepository analysisDataRepository;
  @Autowired private final AnalysisTypeService analysisTypeService;
  @Autowired private final AnalysisStateChangeRepository analysisStateChangeRepository;
  @Autowired private final ValidationService validationService;

  @Override
  @Transactional
  public Analysis create(@NonNull String studyId, @NonNull Payload payload) {
    studyService.checkStudyExist(studyId);

    val analysisId = idService.generateAnalysisId();
    val analysisSchema =
        analysisTypeService.getAnalysisSchema(
            payload.getAnalysisType().getName(), payload.getAnalysisType().getVersion());

    val analysisData = AnalysisData.builder().data(toJsonNode(payload.getData())).build();
    analysisDataRepository.save(analysisData);

    val a = new Analysis();
    a.setFiles(payload.getFiles());
    a.setSamples(payload.getSamples());
    a.setAnalysisId(analysisId);
    a.setAnalysisState(UNPUBLISHED.name());
    a.setStudyId(studyId);
    a.setAnalysisSchema(analysisSchema);

    analysisData.setAnalysis(a);

    saveCompositeEntities(studyId, analysisId, a.getSamples());
    saveFiles(analysisId, studyId, a.getFiles());

    return a;
  }

  @Override
  @Transactional
  public Analysis updateAnalysis(
      @NonNull String studyId,
      @NonNull String analysisId,
      @NonNull JsonNode updateAnalysisRequest) {
    // Validate prerequisites
    checkAnalysisAndStudyRelated(studyId, analysisId);
    checkServer(
        updateAnalysisRequest.hasNonNull(ANALYSIS_TYPE),
        AnalysisService.class,
        MALFORMED_PARAMETER,
        "The updateAnalysisRequest does not contain the field '%s'",
        ANALYSIS_TYPE);

    // Extract the schema to validated against
    val analysisTypeId = fromJson(updateAnalysisRequest.path(ANALYSIS_TYPE), AnalysisTypeId.class);
    val newAnalysisSchema = analysisTypeService.getAnalysisSchema(analysisTypeId);

    // Validate the updateAnalysisRequest against the scheme
    validateUpdateRequest(updateAnalysisRequest, newAnalysisSchema);

    // Now that the request is validated, fetch the old analysis
    val analysis = get(analysisId, true, true, true);

    // Update the association between the old schema and new schema entities for the requested

    val oldAnalysisSchema = analysis.getAnalysisSchema();
    analysis.setAnalysisSchema(newAnalysisSchema);

    // Update the analysisData for the requested analysis
    val newData = buildUpdateRequestData(updateAnalysisRequest);
    analysis.getAnalysisData().setData(newData);
    analysis.setUpdatedAt(LocalDateTime.now());

    return analysis;
  }

  @Override
  @Transactional
  public Analysis patchUpdateAnalysis(
          @NonNull String studyId,
          @NonNull String analysisId,
          @NonNull JsonNode patchUpdateAnalysisRequest) {
    // Validate prerequisites
    checkAnalysisAndStudyRelated(studyId, analysisId);

    // Now that the request is validated, fetch the old analysis
    val analysis = get(analysisId, true, true, false);
    log.debug("analysis found:" + analysis);

    val originalData = analysis.getData();
    originalData.put("analysisType", analysis.getAnalysisType()); // we need this to validate against schema.

    val updatedAnalysis = mergePatchRequest(toJsonNode(originalData), patchUpdateAnalysisRequest);

    // get existing Analysis Schema
    val analysisSchema = analysis.getAnalysisSchema();

    // Validate the updatedAnalysis against the scheme
    validateUpdateRequest(updatedAnalysis, analysisSchema);

    // Update the association between the old schema and new schema entities for the requested
    analysis.setAnalysisSchema(analysisSchema);

    // Update the analysisData for the requested analysis
    val newData = buildUpdateRequestData(updatedAnalysis);
    analysis.getAnalysisData().setData(newData);
    analysis.setUpdatedAt(LocalDateTime.now());

    return analysis;
  }

  /**
   * Gets all analysis for a given study. This method should be watched in case performance becomes
   * a problem. Fix for SONG-338
   *
   * @param studyId the study ID
   * @param analysisStates only return analyses that have values from this non-empty list
   * @return returns a List of analysis with the child entities.
   */
  @Override
  public List<Analysis> getAnalysis(@NonNull String studyId, @NonNull Set<String> analysisStates) {
    studyService.checkStudyExist(studyId);
    val finalStates = resolveSelectedAnalysisStates(analysisStates);
    val analyses =
        repository.findAll(
            new AnalysisSpecificationBuilder(true, true, true)
                .buildByStudyAndAnalysisStates(studyId, finalStates));
    analyses.forEach(
        a -> {
          val id = a.getAnalysisId();
          a.setFiles(unsecuredReadFiles(id));
          a.setSamples(readSamples(id));
          a.populatePublishTimes();
        });
    return analyses;
  }

  @Override
  public GetAnalysisResponse getAnalysis(
      @NonNull String studyId,
      @NonNull Set<String> analysisStates,
      @NonNull int limit,
      @NonNull int offset) {
    studyService.checkStudyExist(studyId);
    val finalStates = resolveSelectedAnalysisStates(analysisStates);

    val totalCount = upgradedAnalysisRepository.getTotalAnalysisCount(studyId, finalStates);

    val result = upgradedAnalysisRepository.getAnalysisFromDB(studyId, finalStates, limit, offset);

    val stateChange =
        upgradedAnalysisRepository.getAnalysisStateChange(studyId, finalStates, limit, offset);

    val schemaList =
        upgradedAnalysisRepository.getAnalysisSchema(studyId, finalStates, limit, offset);

    val dataList = upgradedAnalysisRepository.getAnalysisData(studyId, finalStates, limit, offset);

    val dataMap = groupDataListByAnalysisId(dataList);

    val schemaMap = groupSchemaByAnalysisId(schemaList);

    val stateChangeMap = groupStateChangeByAnalysisId(stateChange);

    // build map from result
    val map = buildAnalysis(result);

    val analysisList = new ArrayList<Analysis>();
    for (Map.Entry<
            String, Tuple3<Analysis, HashMap<String, FileEntity>, HashMap<String, CompositeEntity>>>
        entry : map.entrySet()) {
      val tuple = entry.getValue();
      val fileList = new ArrayList<FileEntity>(tuple._2().values());
      val sampleList = new ArrayList<CompositeEntity>(tuple._3().values());
      tuple._1.setFiles(fileList);
      tuple._1.setSamples(sampleList);
      analysisList.add(tuple._1);
    }

    // attach analysis state changes
    analysisList.forEach(
        a -> {
          if (stateChangeMap.containsKey(a.getAnalysisId())) {
            a.setAnalysisStateHistory(stateChangeMap.get(a.getAnalysisId()));
            a.populatePublishTimes();
          }

          // attach analysisSchema
          if (schemaMap.containsKey(a.getAnalysisId())) {
            a.setAnalysisSchema(schemaMap.get(a.getAnalysisId()));
          }

          // attach analysisData
          if (dataMap.containsKey(a.getAnalysisId())) {
            a.setAnalysisData(dataMap.get(a.getAnalysisId()));
          }
        });

    val resp =
        GetAnalysisResponse.builder()
            .analyses(analysisList)
            .currentTotalAnalyses(analysisList.size())
            .totalAnalyses(totalCount.intValue())
            .build();

    return resp;
  }

  /**
   * Searches all analysis matching the IdSearchRequest
   *
   * @param request which defines the query
   * @return returns a list of analysis with child entities in response to the search request. If
   *     nothing is found, an empty list is returned.
   */
  @Override
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

  @Override
  public boolean isAnalysisExist(@NonNull String id) {
    return repository.existsById(id);
  }

  @Override
  public void checkAnalysisExists(String id) {
    validateAnalysisExistence(isAnalysisExist(id), id);
  }

  @Override
  public void checkAnalysisAndStudyRelated(@NonNull String studyId, @NonNull String id) {
    val numAnalyses = repository.countAllByStudyIdAndAnalysisId(studyId, id);
    if (numAnalyses < 1) {
      studyService.checkStudyExist(studyId);
      val analysis = shallowRead(id);
      throw buildServerException(
          getClass(),
          ENTITY_NOT_RELATED_TO_STUDY,
          "The analysisId '%s' is not related to the input studyId '%s'. It is actually related to studyId '%s'",
          id,
          studyId,
          analysis.getStudyId());
    }
  }

  @Override
  public List<Analysis> unsecuredDeepReads(@NonNull Collection<String> ids) {
    return ids.stream().map(this::unsecuredDeepRead).collect(Collectors.toList());
  }

  /**
   * Unsecurely reads an analysis WITH all of its files, samples and info, but does not verify if
   * the studyId used in the request is allowed to read this analysis
   */
  @Override
  public Analysis unsecuredDeepRead(@NonNull String id) {
    val analysis = get(id, true, true, true);
    analysis.setFiles(unsecuredReadFiles(id));
    analysis.setSamples(readSamples(id));
    analysis.populatePublishTimes();
    return analysis;
  }

  @Override
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

  @Override
  @Transactional
  public Analysis publish(@NonNull String studyId, @NonNull String id, boolean ignoreUndefinedMd5) {
    checkAnalysisAndStudyRelated(studyId, id);

    val a = shallowRead(id);

    // Validations before publishing
    val analysisSchema = a.getAnalysisSchema();
    checkAnalysisTypeVersion(analysisSchema);
    val files = unsecuredReadFiles(id);
    checkMissingFiles(id, files);
    val file2storageObjectMap = getStorageObjectsForFiles(files);
    checkMismatchingFileSizes(id, file2storageObjectMap);
    checkMismatchingFileMd5sums(id, file2storageObjectMap, ignoreUndefinedMd5);

    // Publish
    checkedUpdateState(id, PUBLISHED);

    // Recalculate publish times now that it has a new PUBLISHED state in history
    a.populatePublishTimes();

    return a;
  }

  @Override
  @Transactional
  public Analysis unpublish(@NonNull String studyId, @NonNull String id) {
    checkAnalysisAndStudyRelated(studyId, id);
    checkNotSuppressed(
        id,
        "Cannot change the analysis state for analysisId '%s' from '%s' to '%s'",
        id,
        SUPPRESSED,
        UNPUBLISHED);
    val analysis = checkedUpdateState(id, UNPUBLISHED);
    return analysis;
  }

  @Override
  @Transactional
  public Analysis suppress(@NonNull String studyId, @NonNull String id) {
    checkAnalysisAndStudyRelated(studyId, id);
    return checkedUpdateState(id, SUPPRESSED);
  }

  @Override
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

  @Override
  public AnalysisStates readState(@NonNull String id) {
    checkAnalysisExists(id);
    return repository
        .findById(id)
        .map(Analysis::getAnalysisState)
        .map(AnalysisStates::resolveAnalysisState)
        .get();
  }

  // iterates list,
  // builds Analysis, File, Sample, Specimen, and Donor out of the list of DB analysis,
  // puts the entities into Tuple3,
  // maps the entities to its analysisId.
  private LinkedHashMap<
          String, Tuple3<Analysis, HashMap<String, FileEntity>, HashMap<String, CompositeEntity>>>
      buildAnalysis(List<DataEntity> list) {
    val map =
        new LinkedHashMap<
            String,
            Tuple3<Analysis, HashMap<String, FileEntity>, HashMap<String, CompositeEntity>>>();

    list.forEach(
        entity -> {
          val analysis =
              Analysis.builder()
                  .analysisId(entity.getId())
                  .studyId(entity.getStudyId())
                  .analysisState(entity.getAnalysisState())
                  .createdAt(entity.getCreatedAt())
                  .updatedAt(entity.getUpdatedAt())
                  .analysisState(entity.getAnalysisState())
                  .build();

          val file =
              FileEntity.builder()
                  .objectId(entity.getFileId())
                  .studyId(entity.getFileStudyId())
                  .analysisId(entity.getAnalysisId())
                  .fileName(entity.getFileName())
                  .fileSize(entity.getFileSize())
                  .fileType(entity.getFileType())
                  .fileMd5sum(entity.getFileMd5sum())
                  .fileAccess(entity.getFileAccess())
                  .dataType(entity.getDataType())
                  .build();
          file.setInfo(entity.getFileInfo());

          val donor =
              Donor.builder()
                  .donorId(entity.getDonorId())
                  .studyId(entity.getDonorStudyId())
                  .submitterDonorId(entity.getSubmitterDonorId())
                  .gender(entity.getGender())
                  .build();
          donor.setInfo(entity.getDonorInfo());

          val specimen =
              Specimen.builder()
                  .specimenId(entity.getSpecimenId())
                  .donorId(entity.getSpecimenDonorId())
                  .submitterSpecimenId(entity.getSubmitterSpecimenId())
                  .specimenType(entity.getSpecimenType())
                  .specimenTissueSource(entity.getSpecimenTissueSource())
                  .tumourNormalDesignation(entity.getTumourNormalDesignation())
                  .build();
          specimen.setInfo(entity.getSpecimenInfo());

          val sample =
              CompositeEntity.compositeEntityBuilder().specimen(specimen).donor(donor).build();

          sample.setSampleId(entity.getSampleId());
          sample.setSubmitterSampleId(entity.getSampleSubmitterId());
          sample.setSampleType(entity.getSampleType());
          sample.setMatchedNormalSubmitterSampleId(entity.getMatchedNormalSubmitterSampleId());
          sample.setSpecimenId(entity.getSampleSpecimenId());
          sample.setInfo(entity.getSampleInfo());

          // maps Files and Samples to analysisId
          if (!map.containsKey(entity.getId())) {
            val fileMap = new HashMap<String, FileEntity>();
            fileMap.put(file.getObjectId(), file);
            val sampleMap = new HashMap<String, CompositeEntity>();
            sampleMap.put(sample.getSampleId(), sample);
            map.put(entity.getId(), new Tuple3(analysis, fileMap, sampleMap));
          } else {
            if (!map.get(entity.getId())._2().containsKey(file.getObjectId())) {
              map.get(entity.getId())._2().put(file.getObjectId(), file);
            }

            if (!map.get(entity.getId())._3().containsKey(sample.getSampleId())) {
              map.get(entity.getId())._3().put(sample.getSampleId(), sample);
            }
          }
        });
    return map;
  }

  private LinkedHashMap<String, Set<AnalysisStateChange>> groupStateChangeByAnalysisId(
      List<AnalysisStateChangeJoin> stateChange) {
    // using LinkedHashMap to preserve the original analysisId order in stateChange
    val stateChangeMap = new LinkedHashMap<String, Set<AnalysisStateChange>>();
    stateChange.forEach(
        change -> {
          // This condition is for avoiding nullpointer when analysis state change doesn't have the
          // following properties in DB
          if (change.getStateUpdatedAt() != null
              && change.getInitialState() != null
              && change.getAnalysisStateChangeId() != null
              && change.getUpdatedState() != null) {
            val analysisStateChange =
                AnalysisStateChange.builder()
                    .id(change.getAnalysisStateChangeId())
                    .initialState(change.getInitialState())
                    .updatedState(change.getUpdatedState())
                    .updatedAt(change.getStateUpdatedAt())
                    .build();
            if (!stateChangeMap.containsKey(change.getId())) {
              val set = new HashSet<AnalysisStateChange>();
              set.add(analysisStateChange);
              stateChangeMap.put(change.getId(), set);
            } else {
              val existingSet = stateChangeMap.get(change.getId());
              existingSet.add(analysisStateChange);
            }
          }
        });
    return stateChangeMap;
  }

  private LinkedHashMap<String, AnalysisSchema> groupSchemaByAnalysisId(
      List<AnalysisSchemaJoin> schemaList) {
    // using LinkedHashMap to preserve the original analysisId order in schemaList
    val schemaMap = new LinkedHashMap<String, AnalysisSchema>();
    schemaList.forEach(
        schema -> {
          val analysisSchema =
              AnalysisSchema.builder()
                  .id(schema.getAnalysisSchemaId())
                  .version(schema.getVersion())
                  .name(schema.getName())
                  .createdAt(schema.getCreatedAt())
                  .schema(schema.getSchema())
                  .build();

          if (!schemaMap.containsKey(schema.getAnalysisId())) {
            schemaMap.put(schema.getAnalysisId(), analysisSchema);
          } else {
            checkServer(
                false,
                getClass(),
                DUPLICATE_ANALYSIS_SCHEMA,
                "Data error: trying to associate duplicate analysis schemas to analysis id '%s': ",
                schema.getAnalysisId());
          }
        });
    return schemaMap;
  }

  private LinkedHashMap<String, AnalysisData> groupDataListByAnalysisId(
      List<AnalysisDataJoin> dataList) {
    // using LinkedHashMap to preserve the original analysisId order in dataList
    val dataMap = new LinkedHashMap<String, AnalysisData>();
    dataList.forEach(
        data -> {
          val analysisData =
              AnalysisData.builder().id(data.getAnalysisDataId()).data(data.getData()).build();
          if (!dataMap.containsKey(data.getAnalysisId())) {
            dataMap.put(data.getAnalysisId(), analysisData);
          } else {
            checkServer(
                false,
                getClass(),
                DUPLICATE_ANALYSIS_SCHEMA,
                "Data error: trying to associate duplicate analysis data to analysis id '%s': ",
                data.getAnalysisId());
          }
        });
    return dataMap;
  }

  private void checkAnalysisTypeVersion(AnalysisSchema a) {
    val errors = validationService.validateAnalysisTypeVersion(a.getName(), a.getVersion());
    checkServer(isNull(errors), getClass(), ANALYSIS_TYPE_INCORRECT_VERSION, errors);
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

  private Analysis checkedUpdateState(String id, AnalysisStates analysisState) {
    // Fetch Analysis
    val analysis = unsecuredDeepRead(id);

    // Create state history
    val initialState = analysis.getAnalysisState();
    val updatedState = analysisState.name();
    val stateChange =
        AnalysisStateChange.builder()
            .analysis(analysis)
            .initialState(initialState)
            .updatedState(updatedState)
            .updatedAt(LocalDateTime.now())
            .build();

    // Update analysis state and state history
    analysis.setAnalysisState(updatedState);
    analysis.getAnalysisStateHistory().add(stateChange);
    repository.save(analysis);

    return analysis;
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
    return get(id, false, false, false);
  }

  private void validateUpdateRequest(JsonNode request, AnalysisSchema analysisSchema) {
    checkAnalysisTypeVersion(analysisSchema);
    val renderedUpdateJsonSchema = renderUpdateJsonSchema(analysisSchema);
    val schema = buildSchema(renderedUpdateJsonSchema);
    try {
      validateWithSchema(schema, request);
    } catch (ValidationException e) {
      throw buildServerException(getClass(), SCHEMA_VIOLATION, COMMA.join(e.getAllMessages()));
    }
  }

  @SneakyThrows
  private JsonNode renderUpdateJsonSchema(AnalysisSchema analysisSchema) {
    val jsonSchema = (ObjectNode) readTree(analysisUpdateBaseJson);
    // Merge required fields
    val requiredNode = (ArrayNode) jsonSchema.path(REQUIRED);
    val coreRequiredNode = (ArrayNode) analysisSchema.getSchema().path(REQUIRED);
    requiredNode.addAll(coreRequiredNode);

    // Merge properties fields
    val propertiesNode = (ObjectNode) jsonSchema.path(PROPERTIES);
    val corePropertiesNode = (ObjectNode) analysisSchema.getSchema().path(PROPERTIES);
    propertiesNode.setAll(corePropertiesNode);

    return jsonSchema;
  }

  private Analysis get(
      @NonNull String id,
      boolean fetchAnalysisSchema,
      boolean fetchAnalysisData,
      boolean fetchStateHistory) {
    val analysisResult =
        repository.findOne(
            new AnalysisSpecificationBuilder(
                    fetchAnalysisSchema, fetchAnalysisData, fetchStateHistory)
                .buildById(id));

    validateAnalysisExistence(analysisResult.isPresent(), id);
    val analysis = analysisResult.get();
    if (fetchStateHistory) {
      analysis.populatePublishTimes();
    }
    return analysis;
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

  private static JsonNode buildUpdateRequestData(JsonNode updateAnalysisRequest) {
    val root = ((ObjectNode) updateAnalysisRequest);
    root.remove(ANALYSIS_TYPE);
    return root;
  }

  private JsonNode mergePatchRequest(JsonNode original, JsonNode patch){
    try {
      JsonMergePatch jsonMergePatch = JsonMergePatch.fromJson(patch);
      JsonNode updatedAnalysis = jsonMergePatch.apply(original);
      log.debug("updated analysis:" + updatedAnalysis);
      return updatedAnalysis;
    } catch (JsonPatchException e) {
      log.error(e.getMessage());
      throw buildServerException(
              getClass(),
              PAYLOAD_PARSING,
              "Unable to read the input payload");
    }
  }
}
