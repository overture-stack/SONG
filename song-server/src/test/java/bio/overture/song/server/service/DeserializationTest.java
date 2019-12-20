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

import static bio.overture.song.core.model.enums.AnalysisStates.UNPUBLISHED;
import static bio.overture.song.core.model.enums.FileTypes.BAM;
import static bio.overture.song.core.utils.JsonUtils.fromJson;
import static bio.overture.song.core.utils.JsonUtils.objectToTree;
import static bio.overture.song.core.utils.JsonUtils.readTree;
import static bio.overture.song.core.utils.JsonUtils.toJson;
import static bio.overture.song.core.utils.JsonUtils.toJsonNode;
import static bio.overture.song.server.model.enums.ModelAttributeNames.DONOR;
import static bio.overture.song.server.model.enums.ModelAttributeNames.INFO;
import static bio.overture.song.server.model.enums.ModelAttributeNames.SPECIMEN;
import static bio.overture.song.server.utils.TestConstants.SAMPLE_TYPE;
import static bio.overture.song.server.utils.TestConstants.SPECIMEN_CLASS;
import static bio.overture.song.server.utils.TestConstants.TUMOUR_NORMAL_DESIGNATION;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import bio.overture.song.core.model.AnalysisTypeId;
import bio.overture.song.core.utils.JsonUtils;
import bio.overture.song.server.model.analysis.Analysis;
import bio.overture.song.server.model.analysis.AnalysisData;
import bio.overture.song.server.model.dto.Payload;
import bio.overture.song.server.model.entity.AnalysisSchema;
import bio.overture.song.server.model.entity.Donor;
import bio.overture.song.server.model.entity.FileEntity;
import bio.overture.song.server.model.entity.Sample;
import bio.overture.song.server.model.entity.Specimen;
import bio.overture.song.server.model.entity.composites.CompositeEntity;
import bio.overture.song.server.model.enums.ModelAttributeNames;
import bio.overture.song.server.utils.TestFiles;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.Test;

@Slf4j
public class DeserializationTest {

  @Test
  @SneakyThrows
  public void nonEmptyInfoFields_allEmpty_success() {
    // Construct an analysis that contains empty info fields
    val a =
        Analysis.builder()
            .analysisId("AN1")
            .analysisState(UNPUBLISHED.toString())
            .studyId("ABC123")
            .build();

    val aData = new AnalysisData();
    aData.setAnalysis(a);
    aData.setData(null);
    a.setAnalysisData(aData);

    val aSchema =
        AnalysisSchema.builder()
            .schema(new ObjectMapper().createObjectNode())
            .id(33)
            .name("somethingVar")
            .version(4)
            .build();
    aSchema.associateAnalysis(a);

    val d =
        Donor.builder()
            .donorGender("Male")
            .donorId("DO1")
            .submitterDonorId("SUB_DO1")
            .studyId("ABC123")
            .build();

    val sp =
        Specimen.builder()
            .donorId("DO1")
            .specimenClass(SPECIMEN_CLASS.stream().findAny().get())
            .specimenType(TUMOUR_NORMAL_DESIGNATION.stream().findAny().get())
            .specimenId("SP1")
            .submitterSpecimenId("SUB_SP1")
            .build();

    val sa =
        Sample.builder()
            .sampleId("SA1")
            .sampleType(SAMPLE_TYPE.stream().findAny().get())
            .specimenId("SP1")
            .submitterSampleId("SUB_SA1")
            .build();

    val ce = CompositeEntity.create(sa);
    ce.setDonor(d);
    ce.setSpecimen(sp);

    val f1 =
        FileEntity.builder()
            .analysisId("AN1")
            .fileAccess("open")
            .fileMd5sum("sdfsdf")
            .fileSize(222L)
            .objectId("sfdsdfsdf")
            .studyId("ABC123")
            .fileName("something.bam")
            .fileType(BAM.toString())
            .build();
    val fileList = List.of(f1);
    val fff = new ObjectMapper().valueToTree(f1);

    val lce = List.of(ce);
    a.setSamples(lce);
    a.setFiles(fileList);

    // Assert no info fields when converting from object to tree
    val root = objectToTree(a);
    assertNoInfoFieldsInAnalysis(root);

    // Assert no info fields when converting from object to string to tree
    val root2 = readTree(toJson(a));
    assertNoInfoFieldsInAnalysis(root2);
  }

  @Test
  public void testAnalysisTypeId() {
    val j1 = JsonUtils.mapper().createObjectNode();
    j1.put("name", "something");
    j1.put("version", 33);
    val e1 = AnalysisTypeId.builder().name("something").version(33).build();
    assertEquals(fromJson(j1, AnalysisTypeId.class), e1);

    val j2 = JsonUtils.mapper().createObjectNode();
    j2.put("name", "something");
    val e2 = AnalysisTypeId.builder().name("something").build();
    assertEquals(fromJson(j2, AnalysisTypeId.class), e2);

    val j3 = JsonUtils.mapper().createObjectNode();
    j3.put("version", 33);
    val e3 = AnalysisTypeId.builder().version(33).build();
    assertEquals(fromJson(j3, AnalysisTypeId.class), e3);

    val j4 = JsonUtils.mapper().createObjectNode();
    val e4 = AnalysisTypeId.builder().build();
    assertEquals(fromJson(j4, AnalysisTypeId.class), e4);
  }

  @Test
  public void testVariantCallDeserialization() {
    val payload1 =
        fromJson(
            TestFiles.getJsonNodeFromClasspath(
                "documents/deserialization/variantcall-deserialize1.json"),
            Payload.class);
    val rootNode1 = toJsonNode(payload1.getData());
    val experimentNode1 = rootNode1.path("experiment");
    assertFalse(experimentNode1.hasNonNull("matchedNormalSampleSubmitterId"));
    assertFalse(experimentNode1.hasNonNull("variantCallingTool"));
    assertFalse(experimentNode1.hasNonNull("random"));

    val payload2 =
        fromJson(
            TestFiles.getJsonNodeFromClasspath(
                "documents/deserialization/variantcall-deserialize2.json"),
            Payload.class);

    val rootNode2 = toJsonNode(payload2.getData());
    val experimentNode2 = rootNode2.path("experiment");
    assertTrue(rootNode2.has("experiment"));
    assertFalse(experimentNode2.has("matchedNormalSampleSubmitterId"));
    assertFalse(experimentNode2.has("variantCallingTool"));
  }

  @Test
  public void testSequencingReadDeserialization() {
    val payload1 =
        fromJson(
            TestFiles.getJsonNodeFromClasspath(
                "documents/deserialization/sequencingread-deserialize1.json"),
            Payload.class);

    val rootNode1 = toJsonNode(payload1.getData());
    val experimentNode1 = rootNode1.path("experiment");
    assertFalse(experimentNode1.has("aligned"));
    assertFalse(experimentNode1.has("alignmentTool"));
    assertFalse(experimentNode1.has("insertSize"));
    assertEquals(experimentNode1.path("libraryStrategy").textValue(), "WXS");
    assertFalse(experimentNode1.hasNonNull("pairedEnd"));
    assertFalse(experimentNode1.hasNonNull("referenceGenome"));
    assertFalse(experimentNode1.path("info").hasNonNull("random"));

    val payload2 =
        fromJson(
            TestFiles.getJsonNodeFromClasspath(
                "documents/deserialization/sequencingread-deserialize2.json"),
            Payload.class);

    val rootNode2 = toJsonNode(payload2.getData());
    val experimentNode2 = rootNode2.path("experiment");
    assertFalse(experimentNode2.has("aligned"));
    assertFalse(experimentNode2.has("alignmentTool"));
    assertFalse(experimentNode2.hasNonNull("insertSize"));
    assertEquals(experimentNode2.path("libraryStrategy").textValue(), "WXS");
    assertTrue(experimentNode2.path("pairedEnd").booleanValue());
    assertFalse(experimentNode2.hasNonNull("referenceGenome"));
  }

  private static void assertNoInfoFieldsInAnalysis(JsonNode analysisAsJson) {
    assertNoInfoField(analysisAsJson);
    val jSample1 = analysisAsJson.path(ModelAttributeNames.SAMPLES).get(0);
    assertNoInfoField(jSample1);
    assertNoInfoField(jSample1.path(DONOR));
    assertNoInfoField(jSample1.path(SPECIMEN));
    assertNoInfoField(analysisAsJson.path(ModelAttributeNames.FILES).get(0));
  }

  private static void assertNoInfoField(JsonNode jsonPath) {
    assertFalse(jsonPath.has(INFO));
  }
}
