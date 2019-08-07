package bio.overture.song.server.service;

import bio.overture.song.server.controller.analysisType.AnalysisTypePageableResolver.AnalysisTypePageable;
import bio.overture.song.server.model.analysis.AnalysisTypeId;
import bio.overture.song.server.model.dto.AnalysisType;
import bio.overture.song.server.model.entity.AnalysisSchema;
import bio.overture.song.server.repository.AnalysisSchemaRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Maps;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static java.lang.Integer.parseInt;
import static java.lang.String.format;
import static java.util.regex.Pattern.compile;
import static org.icgc.dcc.common.core.util.Joiners.COMMA;
import static org.icgc.dcc.common.core.util.stream.Collectors.toImmutableList;
import static org.springframework.data.domain.Sort.Direction.ASC;
import static org.springframework.data.domain.Sort.Direction.DESC;
import static bio.overture.song.core.exceptions.ServerErrors.ANALYSIS_TYPE_NOT_FOUND;
import static bio.overture.song.core.exceptions.ServerErrors.MALFORMED_PARAMETER;
import static bio.overture.song.core.exceptions.ServerErrors.SCHEMA_VIOLATION;
import static bio.overture.song.core.exceptions.ServerException.buildServerException;
import static bio.overture.song.core.exceptions.ServerException.checkServer;
import static bio.overture.song.core.utils.JsonSchemaUtils.validateWithSchema;
import static bio.overture.song.server.model.analysis.AnalysisTypeId.createAnalysisTypeId;
import static bio.overture.song.server.model.enums.ModelAttributeNames.ID;
import static bio.overture.song.server.utils.CollectionUtils.mapToImmutableSet;

@Slf4j
@Service
public class AnalysisTypeService {

  private static final String  ANALYSIS_TYPE_NAME_REGEX = "[a-zA-Z0-9\\._-]+";
  private static final Pattern ANALYSIS_TYPE_NAME_PATTERN = compile("^"+ANALYSIS_TYPE_NAME_REGEX+"$");
  private static final Pattern ANALYSIS_TYPE_ID_PATTERN = compile("^("+ANALYSIS_TYPE_NAME_REGEX+"):(\\d+)$");
  private static final String ANALYSIS_TYPE_ID_FORMAT = "%s:%s";

  private final Schema analysisTypeMetaSchema;
  private final AnalysisSchemaRepository analysisSchemaRepository;

  @Autowired
  public AnalysisTypeService(@NonNull Supplier<Schema> analysisTypeMetaSchemaSupplier,
      @NonNull AnalysisSchemaRepository analysisSchemaRepository) {
    this.analysisTypeMetaSchema = analysisTypeMetaSchemaSupplier.get();
    this.analysisSchemaRepository = analysisSchemaRepository;
  }

  public Schema getAnalysisTypeMetaSchema(){
    return analysisTypeMetaSchema;
  }

  public AnalysisType getAnalysisType(@NonNull String analysisTypeIdAsString){
    // Parse out the name and version
    val analysisTypeId = parseAnalysisTypeId(analysisTypeIdAsString);
    val name = analysisTypeId.getName();
    val version = analysisTypeId.getVersion();

    checkServer(version > 0,getClass(), MALFORMED_PARAMETER,
        "The version '%s' must be greater than 0", version);

    // Get a page of size 1 with the target version
    val page = filterAnalysisSchemaByAscIndex(name, version-1);
    val latestVersion = page.getTotalElements();
    val analysisTypeNameExists = page.getTotalElements() > 0;
    val analysisTypeVersionExists = version <= latestVersion;
    val analysisSchemas = page.getContent();

    checkServer(analysisTypeNameExists, getClass(), ANALYSIS_TYPE_NOT_FOUND,
        "The analysisType with name '%s' does not exist",
        name);

    checkServer(analysisTypeVersionExists, getClass(), ANALYSIS_TYPE_NOT_FOUND,
        "Version '%s' of analysisType with name '%s' does not exist however exists for the latest version '%s'",
        name, version, latestVersion);

    checkState(analysisSchemas.size() == 1, "Should not be here. Only 1 analysisType should be returned");
    val analysisSchema = analysisSchemas.get(0);
    log.debug("Found analysisType '{}' with version '{}'", name, version);
    return buildAnalysisType(name, version, analysisSchema.getSchema());
  }

  @Transactional
  public AnalysisType register(@NonNull String analysisTypeName, @NonNull JsonNode analysisTypeSchema){
    validateAnalysisTypeName(analysisTypeName);
    validateAnalysisTypeSchema(analysisTypeSchema);
    return commitAnalysisType(analysisTypeName, analysisTypeSchema);
  }

  public Page<AnalysisType> listAnalysisTypesFilterNames(@NonNull List<String> requestedNames, @NonNull Pageable pageable, boolean hideSchema){
    return findAnalysisTypes(p -> analysisSchemaRepository.findAllByNameIn(requestedNames, p), pageable, hideSchema);
  }

  public Page<AnalysisType> listAllAnalysisTypes(@NonNull Pageable pageable, boolean hideSchema){
    return findAnalysisTypes(analysisSchemaRepository::findAll, pageable, hideSchema);
  }

  private Map<Integer, Integer> buildIdVersionLookup(Collection<String> names){
    val projections = analysisSchemaRepository.findAllByNameInOrderByNameAscIdAsc(names);
    val idToVersionLookup = Maps.<Integer,Integer>newHashMap();

    if (!projections.isEmpty()) {
      String previousName = projections.get(0).getName();
      int version = 0;
      for (val projection : projections) {
        val id = projection.getId();
        val currentName = projection.getName();
        if (currentName.equals(previousName)) {
          version++;
        } else {
          version = 1;
          previousName = currentName;
        }
        idToVersionLookup.put(id, version);
      }
    }
    return idToVersionLookup;
  }

  private Page<AnalysisType> findAnalysisTypes(Function<Pageable, Page<AnalysisSchema>> findCallback, Pageable pageable, boolean hideSchema){
    // Just incase...
    checkArgument(pageable instanceof AnalysisTypePageable,
        "The input pageable object '%s' is not of type %s",
        pageable.getClass().getSimpleName(),
        AnalysisTypePageable.class.getSimpleName());

    val analysisSchemaPage = findCallback.apply(pageable);
    // Filter by version if not empty
    val analysisSchemas = analysisSchemaPage.getContent();
    if (analysisSchemas.isEmpty()){
      return Page.empty();
    }

    // Extract a set of names from the result
    val existingNames = mapToImmutableSet(analysisSchemas, AnalysisSchema::getName);

    // Create a lookup table of orderIds to versions for each of the names
    val idToVersionLookup = buildIdVersionLookup(existingNames);

    // convert to analysisTypes and filter only specified versions
    val analysisTypes = analysisSchemas
        .stream()
        .map(a -> convertToAnalysisType(a, idToVersionLookup, hideSchema) )
        .collect(toImmutableList());
    return new PageImpl<>(analysisTypes, pageable, analysisSchemaPage.getTotalElements());
  }


  private Page<AnalysisSchema> filterLatestAnalysisSchema(String name){
    return analysisSchemaRepository.findAllByName(name,
        PageRequest.of(0, 1,
            Sort.by(DESC, ID)));
  }

  private Page<AnalysisSchema> filterAnalysisSchemaByAscIndex(String name, Integer index){
    return analysisSchemaRepository.findAllByName(name,
        PageRequest.of(index, 1,
            Sort.by(ASC, ID)));
  }

  private Integer getLatestVersionNumber(String analysisTypeName){
    return analysisSchemaRepository.countAllByName(analysisTypeName);
  }

  @SneakyThrows
  private void validateAnalysisTypeSchema(@NonNull JsonNode analysisTypeSchema) {
    val metaSchema = getAnalysisTypeMetaSchema();
    try{
      validateWithSchema(metaSchema, analysisTypeSchema);
    } catch (ValidationException e){
      throw buildServerException(getClass(), SCHEMA_VIOLATION,
          COMMA.join(e.getAllMessages()));
    }
  }

  private AnalysisType commitAnalysisType(@NonNull String analysisTypeName, @NonNull JsonNode analysisTypeSchema) {
    val analysisSchema = AnalysisSchema.builder()
        .name(analysisTypeName)
        .schema(analysisTypeSchema)
        .build();
    analysisSchemaRepository.save(analysisSchema);
    val latestVersion = getLatestVersionNumber(analysisTypeName);
    log.debug("Registered analysisType '{}' with version '{}'", analysisTypeName, latestVersion );
    return buildAnalysisType(analysisTypeName, latestVersion, analysisSchema.getSchema());
  }

  private AnalysisType convertToAnalysisType(AnalysisSchema analysisSchema, Map<Integer, Integer> orderIdToVersionLookup, boolean hideSchema){
    val id = analysisSchema.getId();
    checkState(orderIdToVersionLookup.containsKey(id),
        "Could not find version for analysisSchema id '%s'", analysisSchema.getId());
    val name = analysisSchema.getName();
    val version = orderIdToVersionLookup.get(id);
    return buildAnalysisType(name, version, hideSchema ? null : analysisSchema.getSchema());
  }

  public static AnalysisType buildAnalysisType(@NonNull String name, int version, JsonNode schema){
    return AnalysisType.builder()
        .id(resolveAnalysisTypeId(name, version))
        .name(name)
        .version(version)
        .schema(schema)
        .build();
  }

  public static String resolveAnalysisTypeId(@NonNull AnalysisTypeId analysisTypeId){
    return resolveAnalysisTypeId(analysisTypeId.getName(), analysisTypeId.getVersion());
  }

  public static String resolveAnalysisTypeId(@NonNull String name, int version){
    return format(ANALYSIS_TYPE_ID_FORMAT, name, version);
  }

  public static String resolveAnalysisTypeId(@NonNull AnalysisType analysisType){
    return resolveAnalysisTypeId(analysisType.getName(), analysisType.getVersion());
  }

  private static AnalysisTypeId parseAnalysisTypeId(@NonNull String id){
    val matcher = ANALYSIS_TYPE_ID_PATTERN.matcher(id);
    checkServer(matcher.matches(), AnalysisTypeService.class, MALFORMED_PARAMETER,
        "The id '%s' does not match the regex '%s'",
        id, ANALYSIS_TYPE_ID_PATTERN.pattern());
    val name = matcher.group(1);
    val version = parseInt(matcher.group(2));
    return createAnalysisTypeId(name, version);
  }

  private static void validateAnalysisTypeName(@NonNull String analysisTypeName){
    checkServer(ANALYSIS_TYPE_NAME_PATTERN.matcher(analysisTypeName).matches(), AnalysisTypeService.class,
        MALFORMED_PARAMETER, "The analysisType name '%s' does not match the regex",
        analysisTypeName, ANALYSIS_TYPE_NAME_PATTERN.pattern());
  }

}
