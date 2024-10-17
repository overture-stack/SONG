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
package bio.overture.song.server.service;

import static bio.overture.song.core.utils.JsonUtils.fromSingleQuoted;
import static bio.overture.song.core.utils.JsonUtils.objectToTree;
import static bio.overture.song.core.utils.JsonUtils.readTree;
import static bio.overture.song.core.utils.JsonUtils.toJson;
import static bio.overture.song.core.utils.ResourceFetcher.ResourceType.TEST;
import static bio.overture.song.server.model.enums.ModelAttributeNames.STUDY_ID;
import static com.google.common.collect.Lists.newArrayList;
import static net.javacrumbs.jsonunit.JsonAssert.assertJsonEquals;
import static net.javacrumbs.jsonunit.JsonAssert.when;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_ARRAY_ORDER;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import bio.overture.song.core.model.AnalysisTypeId;
import bio.overture.song.core.utils.JsonUtils;
import bio.overture.song.core.utils.ResourceFetcher;
import bio.overture.song.server.model.dto.Payload;
import bio.overture.song.server.model.entity.FileEntity;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.Test;

@Slf4j
public class SerializationTest {

  private static final String FILEPATH = "src/test/resources/fixtures/";

  @Test
  public void testAnalysisTypeId() {
    val a1 = AnalysisTypeId.builder().name("something").build();

    val a2 = AnalysisTypeId.builder().version(33).build();

    val a3 = AnalysisTypeId.builder().build();

    val a4 = AnalysisTypeId.builder().name("something").version(33).build();

    val r1 = objectToTree(a1);
    assertTrue(r1.hasNonNull("name"));
    assertFalse(r1.hasNonNull("version"));
    assertEquals(r1.path("name").textValue(), "something");

    val r2 = objectToTree(a2);
    assertTrue(r2.hasNonNull("version"));
    assertFalse(r2.hasNonNull("name"));
    assertEquals(r2.path("version").intValue(), 33);

    val r3 = objectToTree(a3);
    assertFalse(r3.hasNonNull("version"));
    assertFalse(r3.hasNonNull("name"));

    val r4 = objectToTree(a4);
    assertTrue(r4.hasNonNull("version"));
    assertTrue(r4.hasNonNull("name"));
    assertEquals(r4.path("version").intValue(), 33);
    assertEquals(r4.path("name").textValue(), "something");
  }

  @Test
  @SneakyThrows
  public void testConvertValue() {
    val json = "{}";

    @SuppressWarnings("rawtypes")
    val m = JsonUtils.fromJson(json, Map.class);
    assertEquals(Collections.emptyMap(), m);
  }

  private static final ResourceFetcher RESOURCE_FETCHER =
      ResourceFetcher.builder()
          .resourceType(TEST)
          .dataDir(Paths.get("documents/deserialization"))
          .build();

  @Test
  @SneakyThrows
  public void testPayloadSerializationAndDeserialization() {
    val inputJson = RESOURCE_FETCHER.readJsonNode("sequencingRead-input-serialization.json");

    val f1 =
        FileEntity.builder()
            .fileAccess("controlled")
            .fileType("VCF")
            .dataType("SOME_DATA_TYPE")
            .fileName(
                "a3bc0998a-3521-43fd-fa10-a834f3874e01.MUSE_1-0rc-vcf.20170711.somatic.snv_mnv.vcf.gz")
            .fileSize(132394L)
            .fileMd5sum("d3cec975acc69a42b8cc3b76ec01ec21")
            .build();
    f1.setInfo("compression", "gzip");

    val f2 =
        FileEntity.builder()
            .fileType("IDX")
            .fileName(
                "a3bc0998a-3521-43fd-fa10-a834f3874e01.MUSE_1-0rc-vcf.20170711.somatic.snv_mnv.vcf.gz.idx")
            .fileSize(2394L)
            .dataType("SOME_DATA_TYPE")
            .fileAccess("controlled")
            .fileMd5sum("65ef4aac7bffcb9f3595a69e48ff2f79")
            .build();

    val expectedPayload =
        Payload.builder()
            .analysisType(AnalysisTypeId.builder().name("sequencingRead").version(1).build())
            .files(newArrayList(f1, f2))
            .studyId(inputJson.path(STUDY_ID).textValue())
            .build();
    val data = RESOURCE_FETCHER.readJsonNode("sequencingRead-expected-data-only.json");
    expectedPayload.addData(data);

    // Assert proper Serialization
    val payloadString = toJson(expectedPayload);
    val actualJson = readTree(payloadString);
    val expectedJson = RESOURCE_FETCHER.readJsonNode("sequencingRead-expected-serialization.json");
    assertJsonEquals(expectedJson, actualJson, when(IGNORING_ARRAY_ORDER));

    // Assert proper deserialization
    val actualPayload = JsonUtils.fromJson(payloadString, Payload.class);
    assertEquals(expectedPayload, actualPayload);
  }

  private static <T, R> void assertEqualField(Function<T, R> fieldFunction, T expected, T actual) {
    assertEquals(fieldFunction.apply(expected), fieldFunction.apply(actual));
  }

  @Test
  public void testListFile() throws IOException {
    val singleQuotedJson =
        "{'files':[ { 'objectId': 'FI12345', 'fileName':'dna3.bam', 'fileMd5':'A1B2C3D4E5F6'},"
            + "{'objectId': 'FI34567', 'fileName': 'dna7.fasta', 'fileType':'BAM', 'fileSize':1234, 'fileMd5': 'F1E2D3'}]}";

    val json = fromSingleQuoted(singleQuotedJson);
    val root = readTree(json);
    val files = root.get("files");
    String fileJson = toJson(files);

    List<FileEntity> f = Arrays.asList(JsonUtils.fromJson(fileJson, FileEntity[].class));

    assertEquals(f.size(), 2);
    assertEquals(f.get(0).getFileName(), "dna3.bam");
  }

  @SneakyThrows
  private String readFile(String name) {
    return new String(Files.readAllBytes(new java.io.File("..", name).toPath()));
  }

  @Test
  public void testSequencingReadPayloadFromJson() throws IOException {
    val json = readFile(FILEPATH + "sequencingRead.json");
    val payload = JsonUtils.fromJson(json, Payload.class);

    System.out.printf("*** Payload object='%s'\n", payload);
    assertEquals(payload.getFiles().size(), 2);

    val rootNode = JsonUtils.toJsonNode(payload.getData());
    val experimentNode = rootNode.path("experiment");
    assertEquals(experimentNode.path("libraryStrategy").textValue(), "WXS");
    assertEquals(experimentNode.path("insertSize").longValue(), 900L);
    assertEquals(experimentNode.path("alignmentTool").textValue(), "MUSE variant call pipeline");
  }

  @Test
  public void testVariantCallPayloadFromJson() throws IOException {
    val json = readFile(FILEPATH + "variantCall.json");
    val payload = JsonUtils.fromJson(json, Payload.class);
    System.out.printf("*** Analysis object='%s'\n", payload);
    assertEquals(payload.getFiles().size(), 2);
  }
}
