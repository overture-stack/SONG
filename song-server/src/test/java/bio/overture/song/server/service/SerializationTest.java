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

import static bio.overture.song.core.utils.JsonUtils.readTree;
import static bio.overture.song.core.utils.JsonUtils.toJson;
import static bio.overture.song.core.utils.ResourceFetcher.ResourceType.TEST;
import static com.google.common.collect.Lists.newArrayList;
import static java.lang.String.format;
import static net.javacrumbs.jsonunit.JsonAssert.assertJsonEquals;
import static net.javacrumbs.jsonunit.JsonAssert.when;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_ARRAY_ORDER;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import bio.overture.song.core.utils.JsonUtils;
import bio.overture.song.core.utils.ResourceFetcher;
import bio.overture.song.server.model.dto.Payload;
import bio.overture.song.server.model.entity.Donor;
import bio.overture.song.server.model.entity.FileEntity;
import bio.overture.song.server.model.entity.Specimen;
import bio.overture.song.server.model.entity.composites.CompositeEntity;
import bio.overture.song.server.model.entity.composites.DonorWithSpecimens;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.Test;

@Slf4j
public class SerializationTest {

  private static final String FILEPATH = "src/test/resources/fixtures/";

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
            .fileAccess("controlled")
            .fileMd5sum("65ef4aac7bffcb9f3595a69e48ff2f79")
            .build();

    val sp =
        Specimen.builder()
            .specimenSubmitterId("internal_specimen_123456789_01")
            .specimenClass("Normal")
            .specimenType("Normal - solid tissue")
            .build();

    val d =
        Donor.builder().donorGender("male").donorSubmitterId("internal_donor_123456789_01").build();
    d.setInfo("ageCategory", "18-25");
    d.setInfo("riskCategory", "3b");

    val sa = new CompositeEntity();
    sa.setSampleSubmitterId("internal_sample_123456789_01");
    sa.setSampleType("Total RNA");
    sa.setInfo("tissueCollectionMethod", "IV");
    sa.setSpecimen(sp);
    sa.setDonor(d);

    val expectedPayload =
        Payload.builder()
            .analysisId(inputJson.path("analysisId").textValue())
            .analysisTypeId("sequencingRead:1")
            .file(newArrayList(f1, f2))
            .sample(newArrayList(sa))
            .study(inputJson.path("study").textValue())
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

  @Test
  @SneakyThrows
  public void testDonorSpecimens() {
    val donorId = "DO1234";
    val submitter = "1234";
    val study = "X2345-QRP";
    val gender = "female";

    val single =
        format(
            "{'donorId':'%s','donorSubmitterId':'%s','studyId':'%s','donorGender':'%s',"
                + "'roses':'red','violets':'blue'}",
            donorId, submitter, study, gender);
    val metadata = JsonUtils.fromSingleQuoted("{'roses':'red','violets':'blue'}");
    val json = JsonUtils.fromSingleQuoted(single);
    val donor = JsonUtils.fromJson(json, DonorWithSpecimens.class);
    assertEquals(donor.getDonorId(), donorId);
    assertEquals(donor.getDonorSubmitterId(), submitter);
    assertEquals(donor.getStudyId(), study);
    assertEquals(donor.getDonorGender(), gender);
    assertEquals(donor.getSpecimens(), Collections.emptyList());
    assertEquals(donor.getInfoAsString(), metadata);
  }

  @Test
  public void testDonorToJson() {
    val donor = new Donor();
    val json = toJson(donor);

    val expected =
        "{'donorId':null,'donorSubmitterId':null,'studyId':null,'donorGender':null," + "'info':{}}";
    val expectedJson = JsonUtils.fromSingleQuoted(expected);
    assertEquals(json, expectedJson);
  }

  @Test
  public void testDonorSettings() {
    val donor = new Donor();
    donor.setDonorId(null);
    val json = toJson(donor);
    System.err.printf("json='%s'\n", json);
    val expected =
        "{'donorId':null,'donorSubmitterId':null,'studyId':null,'donorGender':null," + "'info':{}}";
    val expectedJson = JsonUtils.fromSingleQuoted(expected);
    assertEquals(json, expectedJson);
  }

  @Test
  public void testDonorValues() {
    val id = "DO000123";
    val submitterId = "123";
    val studyId = "X23-CA";
    val gender = "male";
    val metadata = "";

    val donor =
        Donor.builder()
            .donorId(id)
            .donorSubmitterId(submitterId)
            .studyId(studyId)
            .donorGender(gender)
            .build();
    donor.setInfo(metadata);

    val json = toJson(donor);

    val expected =
        format(
            "{'donorId':'%s','donorSubmitterId':'%s','studyId':'%s','donorGender':'%s',"
                + "'info':{%s}}",
            id, submitterId, studyId, gender, metadata);
    val expectedJson = JsonUtils.fromSingleQuoted(expected);
    assertEquals(json, expectedJson);
  }

  @Test
  public void testInvalidValues() {
    val id = "DO000123";
    val submitterId = "123";
    val studyId = "X23-CA";
    val gender = "potatoes";

    boolean failed = false;
    try {
      val donor =
          Donor.builder()
              .donorId(id)
              .donorSubmitterId(submitterId)
              .studyId(studyId)
              .donorGender(gender)
              .build();
    } catch (IllegalArgumentException e) {
      failed = true;
    }

    assertTrue(failed);
  }

  @Test
  public void testListFile() throws IOException {
    val singleQuotedJson =
        "{'file':[ { 'objectId': 'FI12345', 'fileName':'dna3.bam', 'fileMd5':'A1B2C3D4E5F6'},"
            + "{'objectId': 'FI34567', 'fileName': 'dna7.fasta', 'fileType':'BAM', 'fileSize':1234, 'fileMd5': 'F1E2D3'}]}";

    val json = JsonUtils.fromSingleQuoted(singleQuotedJson);
    val root = readTree(json);
    val files = root.get("file");
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
    assertEquals(payload.getAnalysisTypeId(), "sequencingRead:1");
    assertEquals(payload.getFile().size(), 2);
    assertEquals(
        payload.getSample().get(0).getDonor().getDonorSubmitterId(), "internal_donor_123456789-00");

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
    assertEquals(payload.getAnalysisTypeId(), "variantCall:1");
    assertEquals(payload.getFile().size(), 2);
    assertEquals(
        payload.getSample().get(0).getDonor().getDonorSubmitterId(), "internal_donor_123456789-00");
  }
}
