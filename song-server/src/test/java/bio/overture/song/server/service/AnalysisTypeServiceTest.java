package bio.overture.song.server.service;

import static bio.overture.song.core.exceptions.ServerErrors.ANALYSIS_TYPE_NOT_FOUND;
import static bio.overture.song.core.exceptions.ServerErrors.MALFORMED_PARAMETER;
import static bio.overture.song.core.testing.SongErrorAssertions.assertSongError;
import static bio.overture.song.server.service.AnalysisTypeService.resolveAnalysisTypeId;
import static org.junit.jupiter.api.Assertions.assertEquals;

import bio.overture.song.core.model.AnalysisType;
import bio.overture.song.core.model.AnalysisTypeOptions;
import bio.overture.song.server.model.entity.AnalysisSchema;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.transaction.Transactional;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/** Tests AnalysisTypeService version lookup and analysisTypeId resolution. */
@Transactional
@SpringBootTest
@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class AnalysisTypeServiceTest {

  @Autowired private AnalysisTypeService analysisTypeService;

  private void registerVersion(String name) {
    val schema = new ObjectMapper().createObjectNode();
    schema.put("type", "object");
    schema.putObject("properties").putObject("note").put("type", "string");
    analysisTypeService.register(name, new AnalysisTypeOptions(), schema);
  }

  @Test
  public void getLatestVersionNumber_seededType_isOne() {
    assertEquals(1, analysisTypeService.getLatestVersionNumber("variantCall"));
  }

  @Test
  public void getLatestVersionNumber_countsRegisteredVersions() {
    registerVersion("versionCounting");
    assertEquals(1, analysisTypeService.getLatestVersionNumber("versionCounting"));
    registerVersion("versionCounting");
    registerVersion("versionCounting");
    assertEquals(3, analysisTypeService.getLatestVersionNumber("versionCounting"));
  }

  @Test
  public void getLatestVersionNumber_unknownName_notFound() {
    assertSongError(
        () -> analysisTypeService.getLatestVersionNumber("neverRegistered"),
        ANALYSIS_TYPE_NOT_FOUND);
  }

  @Test
  public void getLatestVersionNumber_malformedName_malformedParameter() {
    assertSongError(
        () -> analysisTypeService.getLatestVersionNumber("not a valid name!"), MALFORMED_PARAMETER);
  }

  @Test
  public void resolveAnalysisTypeId_fromNameAndVersion() {
    val analysisTypeId = resolveAnalysisTypeId("someName", 7);
    assertEquals("someName", analysisTypeId.getName());
    assertEquals(7, analysisTypeId.getVersion());
  }

  @Test
  public void resolveAnalysisTypeId_fromAnalysisSchema() {
    val analysisSchema = AnalysisSchema.builder().name("fromSchema").version(3).build();
    val analysisTypeId = resolveAnalysisTypeId(analysisSchema);
    assertEquals("fromSchema", analysisTypeId.getName());
    assertEquals(3, analysisTypeId.getVersion());
  }

  @Test
  public void resolveAnalysisTypeId_fromAnalysisType() {
    val analysisType = AnalysisType.builder().name("fromType").version(5).build();
    val analysisTypeId = resolveAnalysisTypeId(analysisType);
    assertEquals("fromType", analysisTypeId.getName());
    assertEquals(5, analysisTypeId.getVersion());
  }
}
