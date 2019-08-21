package db.migration;

import bio.overture.song.server.model.enums.ModelAttributeNames;
import bio.overture.song.server.model.enums.TableAttributeNames;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.flywaydb.core.api.migration.spring.SpringJdbcMigration;
import org.icgc.dcc.common.core.util.Joiners;
import org.springframework.jdbc.core.JdbcTemplate;

import static db.migration.V1_2__dynamic_schema_integration.NonNullObjectNodeBuilder.createNonNullObjectNode;
import static java.util.Objects.isNull;
import static bio.overture.song.core.utils.JsonDocUtils.getJsonNodeFromClasspath;
import static bio.overture.song.core.utils.JsonUtils.mapper;
import static bio.overture.song.server.config.SchemaConfig.SCHEMA_ANALYSIS_PATH;
import static bio.overture.song.server.model.enums.ModelAttributeNames.MATCHED_NORMAL_SAMPLE_SUBMITTER_ID;
import static bio.overture.song.server.model.enums.ModelAttributeNames.VARIANT_CALLING_TOOL;
import static bio.overture.song.server.utils.JsonSchemas.buildSchema;
import static bio.overture.song.server.utils.JsonSchemas.validateWithSchema;

@Slf4j
public class V1_2__dynamic_schema_integration implements SpringJdbcMigration {

  private static final String SEQUENCING_READ_LEGACY_R_PATH = "legacy/sequencingRead.json";
  private static final String VARIANT_CALL_LEGACY_R_PATH = "legacy/variantCall.json";
  private static final ObjectMapper OBJECT_MAPPER = mapper();
  private static final Schema LEGACY_VARIANT_CALL_SCHEMA =
      buildSchema(SCHEMA_ANALYSIS_PATH, VARIANT_CALL_LEGACY_R_PATH);
  private static final Schema LEGACY_SEQUENCING_READ_SCHEMA =
      buildSchema(SCHEMA_ANALYSIS_PATH, SEQUENCING_READ_LEGACY_R_PATH);

  @Override
  public void migrate(JdbcTemplate jdbcTemplate) throws Exception {
    log.info(
        "Flyway java migration: V1_2__dynamic_schema_integration******************************");

    boolean runWithTest = false;

    // Test data
    if (runWithTest) {
      createTestData(jdbcTemplate);
    }

    // Alter Analysis Table to accomedate analysis_schema relationship
    jdbcTemplate.execute("ALTER TABLE Analysis ADD analysis_schema_id INTEGER");
    jdbcTemplate.execute(
        "ALTER TABLE Analysis ADD CONSTRAINT analysis_schema_id_fk "
            + "FOREIGN KEY (analysis_schema_id) REFERENCES analysis_schema(id)");

    // Create analysis_data table to store json data for an analysis
    jdbcTemplate.execute(
        "CREATE TABLE analysis_data (id BIGSERIAL PRIMARY KEY, data jsonb NOT NULL)"); // , FOREIGN
    // KEY (id)
    // REFERENCES
    // analysis(id))");

    // Add an analysis_data_id join column
    jdbcTemplate.execute("ALTER TABLE Analysis ADD analysis_data_id INTEGER");
    jdbcTemplate.execute(
        "ALTER TABLE Analysis ADD CONSTRAINT analysis_data_id_fk "
            + "FOREIGN KEY (analysis_data_id) REFERENCES analysis_data(id)");

    // Drop views for now, but will be recreated in the next migration
    jdbcTemplate.execute("DROP VIEW idview");
    jdbcTemplate.execute("DROP VIEW fullview");

    // ******************************************************************************
    // *  VariantCall Migration Step
    // ******************************************************************************

    // Register variantCall analyis schema
    val variantCall =
        getJsonNodeFromClasspath(
            SCHEMA_ANALYSIS_PATH.resolve(VARIANT_CALL_LEGACY_R_PATH).toString());
    jdbcTemplate.update(
        "INSERT INTO analysis_schema (name, schema, version) VALUES ('variantCall', ?, 1)",
        variantCall.toString());

    // Update all variantCall analyses to point to variantCall analysis_schema
    jdbcTemplate.execute("UPDATE Analysis SET analysis_schema_id = 1 WHERE type = 'variantCall'");

    // Convert all stored variantCall data to json format and into the analysis_data table
    migrateVariantCall(jdbcTemplate);

    // Drop the variantCall table
    jdbcTemplate.execute("DROP TABLE variantcall");

    // ******************************************************************************
    // *  SequencingRead Migration Step
    // ******************************************************************************

    // Register sequencingRead analyis schema
    val sequencingRead =
        getJsonNodeFromClasspath(
            SCHEMA_ANALYSIS_PATH.resolve(SEQUENCING_READ_LEGACY_R_PATH).toString());

    // Update all variantCall analyses to point to variantCall analysis_schema
    jdbcTemplate.update(
        "INSERT INTO analysis_schema (name, schema, version) VALUES ('sequencingRead', ?, 1)",
        sequencingRead.toString());

    // Convert all stored variantCall data to json format and into the analysis_data table
    jdbcTemplate.execute(
        "UPDATE Analysis SET analysis_schema_id = 2 WHERE type = 'sequencingRead'");

    // Drop the variantCall table
    migrateSequencingRead(jdbcTemplate);
    jdbcTemplate.execute("DROP TABLE sequencingread");

    // Test queries to ensure all is good (if flag set to true)
    if (runWithTest) {
      testUuidMigration(jdbcTemplate);
    }

    log.info(
        "****************************** Flyway java migration: V1_2__dynamic_schema_integration******************************complete");
  }

  private void createTestData(JdbcTemplate jdbcTemplate) {}

  private void migrateVariantCall(JdbcTemplate jdbcTemplate) {
    log.info("Starting VariantCall migration");
    val variantCalls = jdbcTemplate.queryForList("SELECT * FROM variantcall");
    for (val vc : variantCalls) {
      val analysisId = vc.get("id").toString();
      val experiment =
          createNonNullObjectNode()
              .putString(VARIANT_CALLING_TOOL, vc.get(TableAttributeNames.VARIANT_CALLING_TOOL))
              .putString(
                  MATCHED_NORMAL_SAMPLE_SUBMITTER_ID,
                  vc.get(TableAttributeNames.MATCHED_NORMAL_SAMPLE_SUBMITTER_ID))
              .build();
      val analysisData = OBJECT_MAPPER.createObjectNode().set("experiment", experiment);
      try {
        validateWithSchema(LEGACY_VARIANT_CALL_SCHEMA, analysisData);
      } catch (ValidationException e) {
        log.error("Variant Call Errors:   {}", Joiners.COMMA.join(e.getAllMessages()));
        throw e;
      }
      jdbcTemplate.update(
          "INSERT INTO analysis_data(analysis_id, data) VALUES (?,?)",
          analysisId,
          analysisData.toString());
    }
    log.info("Finished VariantCall migration");
  }

  private void migrateSequencingRead(JdbcTemplate jdbcTemplate) {
    log.info("Starting SequencingRead migration");
    val sequencingReads = jdbcTemplate.queryForList("SELECT * FROM sequencingread");
    for (val sr : sequencingReads) {
      val analysisId = sr.get("id").toString();
      val experiment =
          createNonNullObjectNode()
              .putString(
                  ModelAttributeNames.LIBRARY_STRATEGY,
                  sr.get(TableAttributeNames.LIBRARY_STRATEGY))
              .putBoolean(ModelAttributeNames.PAIRED_END, sr.get(TableAttributeNames.PAIRED_END))
              .putLong(ModelAttributeNames.INSERT_SIZE, sr.get(TableAttributeNames.INSERT_SIZE))
              .putBoolean(ModelAttributeNames.ALIGNED, sr.get(TableAttributeNames.ALIGNED))
              .putString(
                  ModelAttributeNames.ALIGNMENT_TOOL, sr.get(TableAttributeNames.ALIGNMENT_TOOL))
              .putString(
                  ModelAttributeNames.REFERENCE_GENOME,
                  sr.get(TableAttributeNames.REFERENCE_GENOME))
              .build();
      val analysisData = OBJECT_MAPPER.createObjectNode().set("experiment", experiment);
      try {
        validateWithSchema(LEGACY_SEQUENCING_READ_SCHEMA, analysisData);
      } catch (ValidationException e) {
        log.error("SequncingRead validationErrors:   {}", Joiners.COMMA.join(e.getAllMessages()));
        throw e;
      }
      jdbcTemplate.update(
          "INSERT INTO analysis_data(analysis_id, data) VALUES (?,?)",
          analysisId,
          analysisData.toString());
    }
    log.info("Finished SequencingRead migration");
  }

  private void testUuidMigration(JdbcTemplate jdbcTemplate) {}

  @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
  public static class NonNullObjectNodeBuilder {

    @NonNull private final ObjectNode root;

    public NonNullObjectNodeBuilder putObject(String key) {
      return createNonNullObjectNode(root.putObject(key));
    }

    public NonNullObjectNodeBuilder putString(String key, Object value) {
      if (!isNull(value)) {
        root.put(key, (String) value);
      }
      return this;
    }

    public NonNullObjectNodeBuilder putInteger(String key, Object value) {
      if (!isNull(value)) {
        root.put(key, (Integer) value);
      }
      return this;
    }

    public NonNullObjectNodeBuilder putLong(String key, Object value) {
      if (!isNull(value)) {
        root.put(key, (Long) value);
      }
      return this;
    }

    public NonNullObjectNodeBuilder putBoolean(String key, Object value) {
      if (!isNull(value)) {
        root.put(key, (Boolean) value);
      }
      return this;
    }

    public ObjectNode build() {
      return root;
    }

    public static NonNullObjectNodeBuilder createNonNullObjectNode() {
      return createNonNullObjectNode(OBJECT_MAPPER.createObjectNode());
    }

    public static NonNullObjectNodeBuilder createNonNullObjectNode(ObjectNode root) {
      return new NonNullObjectNodeBuilder(root);
    }
  }
}
