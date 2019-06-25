package bio.overture.song.server.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.val;
import org.everit.json.schema.Schema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static bio.overture.song.core.utils.JsonDocUtils.toJsonObject;

@Service
public class SpecialSchemaService {

  private final Schema analysisRegistrationSchema;
  private final Schema analysisPayloadSchema;

  @Autowired
  public SpecialSchemaService(@NonNull Schema analysisRegistrationSchema,
      @NonNull Schema analysisPayloadSchema) {
    this.analysisRegistrationSchema = analysisRegistrationSchema;
    this.analysisPayloadSchema = analysisPayloadSchema;
  }

  // TODO: add listener to describe errors
  public void validateAnalysisRegistration(@NonNull JsonNode j) {
    validateWithSchema(analysisRegistrationSchema, j);
  }

  // TODO: add listener to describe errors
  public void validateAnalysisPayload(@NonNull JsonNode j) {
    validateWithSchema(analysisPayloadSchema, j);
  }

  @SneakyThrows
  private static void validateWithSchema(Schema schema, JsonNode j){
    val jsonObject = toJsonObject(j);
    schema.validate(jsonObject);

  }

}
