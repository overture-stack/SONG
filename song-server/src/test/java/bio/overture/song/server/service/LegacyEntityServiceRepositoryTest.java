package bio.overture.song.server.service;

import static bio.overture.song.core.exceptions.ServerErrors.ILLEGAL_FILTER_PARAMETER;
import static bio.overture.song.core.exceptions.ServerErrors.ILLEGAL_QUERY_PARAMETER;
import static bio.overture.song.core.exceptions.ServerErrors.LEGACY_ENTITY_NOT_FOUND;
import static bio.overture.song.core.testing.SongErrorAssertions.assertSongError;
import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import bio.overture.song.server.model.legacy.LegacyDto;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.StreamSupport;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.LinkedMultiValueMap;

/**
 * Tests LegacyEntityService against the real repository and the seeded files FI1-FI4 of study
 * ABC123 (FI1, FI2 in analysis AN1; FI3, FI4 in AN2).
 */
@SpringBootTest
@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class LegacyEntityServiceRepositoryTest {

  @Autowired private LegacyEntityService legacyEntityService;

  private static List<String> contentIds(JsonNode page) {
    return StreamSupport.stream(page.path("content").spliterator(), false)
        .map(entity -> entity.path("id").textValue())
        .collect(toList());
  }

  private static LegacyDto probe(String projectCode, String gnosId) {
    val probe = new LegacyDto();
    probe.setProjectCode(projectCode);
    probe.setGnosId(gnosId);
    return probe;
  }

  @Test
  public void getEntity_seededFile_returnsLegacyFields() {
    val entity = legacyEntityService.getEntity("FI1");
    assertEquals("FI1", entity.getId());
    assertEquals("AN1", entity.getGnosId());
    assertEquals("ABC-TC285G7-A5-ae3458712345.bam", entity.getFileName());
    assertEquals("ABC123", entity.getProjectCode());
    assertEquals("open", entity.getAccess());
  }

  @Test
  public void getEntity_unknownId_notFound() {
    assertSongError(() -> legacyEntityService.getEntity("NO-SUCH-ID"), LEGACY_ENTITY_NOT_FOUND);
  }

  @Test
  public void find_byProjectCode_pagesSortedById() {
    val firstPage =
        legacyEntityService.find(
            new LinkedMultiValueMap<>(),
            probe("ABC123", null),
            PageRequest.of(0, 3, Sort.by("id")));
    assertEquals(List.of("FI1", "FI2", "FI3"), contentIds(firstPage));
    assertEquals(4, firstPage.path("totalElements").intValue());
    assertEquals(2, firstPage.path("totalPages").intValue());
    assertTrue(firstPage.path("first").booleanValue());
    assertFalse(firstPage.path("last").booleanValue());

    val secondPage =
        legacyEntityService.find(
            new LinkedMultiValueMap<>(),
            probe("ABC123", null),
            PageRequest.of(1, 3, Sort.by("id")));
    assertEquals(List.of("FI4"), contentIds(secondPage));
  }

  @Test
  public void find_byGnosId_matchesAnalysisFiles() {
    val page =
        legacyEntityService.find(
            new LinkedMultiValueMap<>(), probe(null, "AN2"), PageRequest.of(0, 10, Sort.by("id")));
    assertEquals(List.of("FI3", "FI4"), contentIds(page));
  }

  @Test
  public void find_fieldsFilter_limitsContentFields() {
    val parameters = new LinkedMultiValueMap<String, String>();
    parameters.add("fields", "id");
    parameters.add("fields", "access");
    val page =
        legacyEntityService.find(
            parameters, probe(null, "AN1"), PageRequest.of(0, 10, Sort.by("id")));
    for (val entity : page.path("content")) {
      val fieldNames = new TreeSet<String>();
      entity.fieldNames().forEachRemaining(fieldNames::add);
      assertEquals(new TreeSet<>(List.of("id", "access")), fieldNames);
    }
    // The filter applies to page content only; the page metadata is kept.
    assertEquals(2, page.path("totalElements").intValue());
  }

  @Test
  public void find_unknownFilterField_illegalFilterParameter() {
    val parameters = new LinkedMultiValueMap<String, String>();
    parameters.add("fields", "notAField");
    assertSongError(
        () ->
            legacyEntityService.find(
                parameters, probe(null, null), PageRequest.of(0, 10, Sort.by("id"))),
        ILLEGAL_FILTER_PARAMETER);
  }

  @Test
  public void find_unknownQueryParameter_illegalQueryParameter() {
    val parameters = new LinkedMultiValueMap<String, String>();
    parameters.add("notAField", "x");
    assertSongError(
        () ->
            legacyEntityService.find(
                parameters, probe(null, null), PageRequest.of(0, 10, Sort.by("id"))),
        ILLEGAL_QUERY_PARAMETER);
  }
}
