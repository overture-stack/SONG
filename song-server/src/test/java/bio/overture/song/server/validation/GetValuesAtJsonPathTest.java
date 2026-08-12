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
package bio.overture.song.server.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import bio.overture.song.server.service.ValidationService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.function.Supplier;
import lombok.SneakyThrows;
import lombok.val;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

public class GetValuesAtJsonPathTest {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  private ValidationService service;

  @BeforeEach
  @SuppressWarnings("unchecked")
  public void setUp() {
    val schemaSupplier = (Supplier<Schema>) mock(Supplier.class);
    when(schemaSupplier.get()).thenReturn(mock(Schema.class));

    service =
        new ValidationService(
            false,
            mock(bio.overture.song.server.validation.SchemaValidator.class),
            mock(bio.overture.song.server.service.AnalysisTypeService.class),
            schemaSupplier,
            mock(bio.overture.song.server.repository.UploadRepository.class),
            mock(RestTemplate.class),
            null);
  }

  @Test
  @SneakyThrows
  public void getValuesAtJsonPath_rootLevelProperty_returnsValue() {
    val payload = parseJson("{\"donorId\": \"DONOR_001\"}");
    val result = service.getValuesAtJsonPath(payload, "donorId");
    assertEquals(List.of("DONOR_001"), result);
  }

  @Test
  @SneakyThrows
  public void getValuesAtJsonPath_nestedProperty_returnsValue() {
    val payload = parseJson("{\"experiment\": {\"donorId\": \"DONOR_002\"}}");
    val result = service.getValuesAtJsonPath(payload, "experiment.donorId");
    assertEquals(List.of("DONOR_002"), result);
  }

  @Test
  @SneakyThrows
  public void getValuesAtJsonPath_pathNotFound_returnsEmptyList() {
    val payload = parseJson("{\"experiment\": {\"donorId\": \"DONOR_003\"}}");
    val result = service.getValuesAtJsonPath(payload, "experiment.specimenId");
    assertTrue(result.isEmpty());
  }

  @Test
  public void getValuesAtJsonPath_nonStringProperty_throwsValidationException() {
    val payload = parseJson("{\"experiment\": {\"count\": 42}}");
    assertThrows(
        ValidationException.class, () -> service.getValuesAtJsonPath(payload, "experiment.count"));
  }

  @Test
  @SneakyThrows
  public void getValuesAtJsonPath_arrayOfStrings_returnsAllStrings() {
    val payload = parseJson("{\"tags\": [\"alpha\", \"beta\", \"gamma\"]}");
    val result = service.getValuesAtJsonPath(payload, "tags[*]");
    assertEquals(List.of("alpha", "beta", "gamma"), result);
  }

  @Test
  @SneakyThrows
  public void getValuesAtJsonPath_wildcardOnArrayOfObjects_returnsAllMatchingValues() {
    val payload =
        parseJson(
            "{\"donors\": ["
                + "{\"donorId\": \"DONOR_001\"},"
                + "{\"donorId\": \"DONOR_002\"},"
                + "{\"donorId\": \"DONOR_003\"}"
                + "]}");
    val result = service.getValuesAtJsonPath(payload, "donors[*].donorId");
    assertEquals(List.of("DONOR_001", "DONOR_002", "DONOR_003"), result);
  }

  @Test
  public void getValuesAtJsonPath_wildcardOnArrayWithSparseProperty_returnsOnlyPresentValues() {
    // Some objects in the array have donorId, some do not - only present values are returned.
    val payload =
        parseJson(
            "{\"donors\": ["
                + "{\"donorId\": \"DONOR_001\"},"
                + "{\"specimenId\": \"SP_001\"},"
                + "{\"donorId\": \"DONOR_003\"}"
                + "]}");
    val result = service.getValuesAtJsonPath(payload, "donors[*].donorId");
    assertEquals(List.of("DONOR_001", "DONOR_003"), result);
  }

  @Test
  public void getValuesAtJsonPath_wildcardOnArrayWithNonStringValue_throwsValidationException() {
    val payload = parseJson("{\"donors\": [" + "{\"count\": 1}," + "{\"count\": 2}" + "]}");
    assertThrows(
        ValidationException.class, () -> service.getValuesAtJsonPath(payload, "donors[*].count"));
  }

  @Test
  public void getValuesAtJsonPath_mixedArrayWithNonStringValue_throwsValidationException() {
    // An array mixing strings and non-strings should fail — ambiguous intent is rejected.
    val payload = parseJson("{\"tags\": [\"alpha\", 42, \"gamma\"]}");
    assertThrows(ValidationException.class, () -> service.getValuesAtJsonPath(payload, "tags[*]"));
  }

  @Test
  @SneakyThrows
  public void getValuesAtJsonPath_specificIndexInArray_returnsSingleValue() {
    val payload =
        parseJson(
            "{\"donors\": ["
                + "{\"donorId\": \"DONOR_001\"},"
                + "{\"donorId\": \"DONOR_002\"},"
                + "{\"donorId\": \"DONOR_003\"}"
                + "]}");
    val result = service.getValuesAtJsonPath(payload, "donors[1].donorId");
    assertEquals(List.of("DONOR_002"), result);
  }

  @SneakyThrows
  private static JsonNode parseJson(String json) {
    return MAPPER.readTree(json);
  }
}
