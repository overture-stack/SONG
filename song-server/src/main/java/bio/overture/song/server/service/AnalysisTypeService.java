package bio.overture.song.server.service;

import bio.overture.song.server.model.dto.AnalysisType;
import bio.overture.song.server.model.entity.AnalysisSchema;
import bio.overture.song.server.repository.AnalysisSchemaRepository;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.NonNull;
import lombok.val;
import org.everit.json.schema.Schema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkState;
import static org.icgc.dcc.common.core.util.stream.Collectors.toImmutableSet;
import static org.springframework.data.domain.Sort.Direction.ASC;
import static org.springframework.data.domain.Sort.Direction.DESC;
import static bio.overture.song.core.exceptions.ServerErrors.ANALYSIS_TYPE_NOT_FOUND;
import static bio.overture.song.core.exceptions.ServerErrors.MALFORMED_PARAMETER;
import static bio.overture.song.core.exceptions.ServerException.checkServer;
import static bio.overture.song.server.model.enums.TableAttributeNames.ID;

@Service
public class AnalysisTypeService {

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

  public AnalysisType getAnalysisType(@NonNull String name, @NonNull Integer version){
    checkServer(version > 0,getClass(), MALFORMED_PARAMETER,
        "The version '%s' must be greater than 0", version);

    val page = filterAnalysisSchemaByAscIndex(name, version-1);
    val latestVersion = page.getTotalElements();
    val analysisTypeNameExists = page.getTotalElements() > 0;
    val analysisTypeVersionExists = version <= latestVersion;
    val analysisTypes = page.getContent();

    checkServer(analysisTypeNameExists, getClass(), ANALYSIS_TYPE_NOT_FOUND,
        "The analysisType with name '%s' does not exist",
        name);
    checkServer(analysisTypeVersionExists, getClass(), ANALYSIS_TYPE_NOT_FOUND,
        "Version '%s' of analysisType with name '%s' does not exist however exists for the latest version '%s'",
        name, version, latestVersion);
    checkState(analysisTypes.size() == 1, "Should not be here. Only 1 analysisType should be returned");
    val schema = analysisTypes.get(0).getSchema();
    return AnalysisType.builder()
        .name(name)
        .version(version)
        .schema(schema)
        .build();
  }

  public AnalysisType commitAnalysisType(@NonNull String analysisTypeName, @NonNull JsonNode analysisTypeSchema) {
    val analysisType = AnalysisSchema.builder()
        .name(analysisTypeName)
        .schema(analysisTypeSchema)
        .build();
    analysisSchemaRepository.save(analysisType);
    val latestVersion = getLatestVersionNumber(analysisTypeName);
    return AnalysisType.builder()
        .name(analysisTypeName)
        .schema(analysisTypeSchema)
        .version(latestVersion)
        .build();
  }

  public AnalysisType getLatestAnalysisType(@NonNull String name) {
    val page = filterLatestAnalysisSchema(name);
    val latestVersion = (int)page.getTotalElements();
    val analysisTypeNameExists = page.getTotalElements() > 0;
    val analysisTypes = page.getContent();

    checkServer(analysisTypeNameExists, getClass(), ANALYSIS_TYPE_NOT_FOUND,
        "The analysisType with name '%s' does not exist",
        name);
    checkState(analysisTypes.size() == 1, "Should not be here. Only 1 analysisType should be returned");
    val schema = page.getContent().get(0).getSchema();
    return AnalysisType.builder()
        .name(name)
        .version(latestVersion)
        .schema(schema)
        .build();
  }

  public Set<String> listAnalysisTypeNames() {
    return analysisSchemaRepository.findDistinctBy(AnalysisSchemaNameView.class)
        .stream()
        .map(AnalysisSchemaNameView::getName)
        .collect(toImmutableSet());
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

  public interface AnalysisSchemaNameView {
    String getName();
  }

}
