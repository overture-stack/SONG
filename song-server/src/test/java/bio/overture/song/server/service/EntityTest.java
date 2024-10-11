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

import static bio.overture.song.core.model.enums.AccessTypes.CONTROLLED;
import static bio.overture.song.core.model.enums.AnalysisStates.*;
import static bio.overture.song.core.testing.SongErrorAssertions.assertExceptionThrownBy;
import static bio.overture.song.server.utils.TestFiles.assertInfoKVPair;
import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.*;

import bio.overture.song.core.model.Metadata;
import bio.overture.song.server.model.Upload;
import bio.overture.song.server.model.entity.FileEntity;
import bio.overture.song.server.model.entity.Study;
import bio.overture.song.server.model.enums.UploadStates;
import bio.overture.song.server.model.legacy.LegacyEntity;
import com.fasterxml.jackson.databind.JsonNode;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import lombok.val;
import org.junit.Test;

public class EntityTest {
  private static final String DEFAULT_STUDY_ID = "ABC123";
  private static final List<String> FILE_TYPES = Arrays.asList("mp3", "mp4", "BAM");

  @Test
  public void testNullMetadata() {
    val m = new Metadata();
    m.setInfo((String) null);
    m.setInfo((JsonNode) null);
    m.addInfo(null);
  }

  @Test
  public void testFile() {
    val file1 = new FileEntity();
    file1.setAnalysisId("a1");
    file1.setFileAccess(CONTROLLED);
    file1.setFileMd5sum("b1");
    file1.setFileName("c1");
    file1.setFileSize(13L);
    file1.setFileType("mp4");
    file1.setObjectId("d1");
    file1.setStudyId("e1");

    val file1_same =
        FileEntity.builder()
            .objectId("d1")
            .analysisId("a1")
            .fileName("c1")
            .studyId("e1")
            .fileSize(13L)
            .fileType(FILE_TYPES.get(0))
            .fileMd5sum("b1")
            .fileAccess(CONTROLLED.toString())
            .build();
    assertEntitiesEqual(file1, file1_same, true);

    val file2 =
        FileEntity.builder()
            .objectId("d2")
            .analysisId("a2")
            .fileName("c2")
            .studyId("e2")
            .fileSize(14L)
            .fileType(FILE_TYPES.get(1))
            .fileMd5sum("b2")
            .fileAccess(CONTROLLED.toString())
            .build();

    assertEntitiesNotEqual(file1, file2);

    file1.setInfo("key1", "f5c9381090a53c54358feb2ba5b7a3d7");
    file1_same.setInfo("key2", "6329334b-dcd5-53c8-98fd-9812ac386d30");
    assertEntitiesNotEqual(file1, file1_same);

    // Test getters
    assertEquals(file1.getAnalysisId(), "a1");
    assertEquals(file1.getFileAccess(), CONTROLLED.toString());
    assertEquals(file1.getFileMd5sum(), "b1");
    assertEquals(file1.getFileName(), "c1");
    assertEquals(file1.getFileType(), FILE_TYPES.get(0));
    assertEquals(file1.getObjectId(), "d1");
    assertEquals(file1.getStudyId(), "e1");
    assertInfoKVPair(file1, "key1", "f5c9381090a53c54358feb2ba5b7a3d7");
  }

  @Test
  public void testMetadata() {
    val metadata1 = new Metadata();
    metadata1.setInfo("key1", "f5c9381090a53c54358feb2ba5b7a3d7");

    val metadata1_same = new Metadata();
    metadata1_same.setInfo("key1", "f5c9381090a53c54358feb2ba5b7a3d7");

    assertEntitiesEqual(metadata1, metadata1_same, true);

    val metadata2 = new Metadata();
    metadata2.setInfo("key2", "6329334b-dcd5-53c8-98fd-9812ac386d30");

    assertEntitiesNotEqual(metadata1, metadata2);

    metadata2.addInfo(metadata1.getInfoAsString());
    assertTrue(metadata2.getInfo().has("key1"));
    assertEquals(metadata2.getInfo().path("key1").textValue(), "f5c9381090a53c54358feb2ba5b7a3d7");

    metadata2.setInfo("something that is not json");
    assertTrue(metadata2.getInfo().has("info"));
    assertEquals(metadata2.getInfo().path("info").textValue(), "something that is not json");
  }

  @Test
  public void testUpload() {
    val u1 = new Upload();
    u1.setAnalysisId("an1");
    u1.setCreatedAt(LocalDateTime.MAX);
    u1.setErrors("error1");
    u1.setPayload("payload1");
    u1.setState(UploadStates.CREATED);
    u1.setState(UploadStates.CREATED.getText());
    u1.setStudyId(DEFAULT_STUDY_ID);
    u1.setUpdatedAt(LocalDateTime.MIN);
    u1.setUploadId("uploadId1");

    val u1_same =
        Upload.builder()
            .uploadId("uploadId1")
            .studyId(DEFAULT_STUDY_ID)
            .analysisId("an1")
            .state(UploadStates.CREATED.toString())
            .errors("error1")
            .payload("payload1")
            .updatedAt(LocalDateTime.MIN)
            .createdAt(LocalDateTime.MAX)
            .build();
    assertEntitiesEqual(u1, u1_same, true);

    val u2 =
        Upload.builder()
            .uploadId("uploadId2")
            .studyId("study333")
            .analysisId("an2")
            .state(UploadStates.VALIDATION_ERROR.toString())
            .errors("error2")
            .payload("payload2")
            .updatedAt(LocalDateTime.MIN)
            .createdAt(LocalDateTime.MAX)
            .build();
    assertEntitiesNotEqual(u1, u2);

    // Test getters
    assertEquals(u1.getAnalysisId(), "an1");
    assertEquals(u1.getCreatedAt(), LocalDateTime.MAX);
    assertEquals(u1.getErrors(), newArrayList("error1"));
    assertEquals(u1.getPayload(), "payload1");
    assertEquals(u1.getState(), UploadStates.CREATED.getText());
    assertEquals(u1.getStudyId(), DEFAULT_STUDY_ID);
    assertEquals(u1.getUpdatedAt(), LocalDateTime.MIN);
    assertEquals(u1.getUploadId(), "uploadId1");

    u1.setErrors("error1|error2|error3");
    assertThat(u1.getErrors(), containsInAnyOrder("error1", "error2", "error3"));
    assertEquals(u1.getErrors().size(), 3);

    u1.addErrors(newArrayList("error4", "error5"));
    assertThat(
        u1.getErrors(), containsInAnyOrder("error1", "error2", "error3", "error4", "error5"));
    assertEquals(u1.getErrors().size(), 5);
  }

  @Test
  public void testLegacyEntity() {
    val e1 =
        LegacyEntity.builder()
            .access("open")
            .fileName("f1")
            .gnosId("g1")
            .id("i1")
            .projectCode("p1")
            .build();

    val e1_same =
        LegacyEntity.builder()
            .access("open")
            .fileName("f1")
            .gnosId("g1")
            .id("i1")
            .projectCode("p1")
            .build();

    assertEntitiesEqual(e1, e1_same, true);

    val e2 =
        LegacyEntity.builder()
            .access("open")
            .fileName("f2")
            .gnosId("g2")
            .id("i2")
            .projectCode("p2")
            .build();
    assertEntitiesNotEqual(e1, e2);

    // Test getters
    assertEquals(e1.getAccess(), "open");
    assertEquals(e1.getFileName(), "f1");
    assertEquals(e1.getGnosId(), "g1");
    assertEquals(e1.getId(), "i1");
    assertEquals(e1.getProjectCode(), "p1");
  }

  @Test
  public void testStudy() {
    val study1 = new Study();
    study1.setDescription("a");
    study1.setName("b");
    study1.setOrganization("c");
    study1.setStudyId("d");

    val study1_same =
        Study.builder().studyId("d").name("b").organization("c").description("a").build();

    assertEntitiesEqual(study1, study1_same, true);

    val study2 =
        Study.builder().studyId("d1").name("b1").organization("c1").description("a1").build();
    assertEntitiesNotEqual(study1, study2);

    study1.setInfo("key1", "f5c9381090a53c54358feb2ba5b7a3d7");
    study1_same.setInfo("key2", "6329334b-dcd5-53c8-98fd-9812ac386d30");
    assertEntitiesNotEqual(study1, study1_same);

    // Test getters
    assertEquals(study1.getDescription(), "a");
    assertEquals(study1.getName(), "b");
    assertEquals(study1.getOrganization(), "c");
    assertEquals(study1.getStudyId(), "d");
    assertInfoKVPair(study1, "key1", "f5c9381090a53c54358feb2ba5b7a3d7");
  }

  @Test
  public void testAnalysisStates() {
    assertEquals(resolveAnalysisState("PUBLISHED"), PUBLISHED);
    assertEquals(resolveAnalysisState("UNPUBLISHED"), UNPUBLISHED);
    assertEquals(resolveAnalysisState("SUPPRESSED"), SUPPRESSED);
    val erroredStates = newArrayList("published", "unpublished", "suppressed", "anything");
    for (val state : erroredStates) {
      assertExceptionThrownBy(IllegalStateException.class, () -> resolveAnalysisState(state));
    }
  }

  private static void assertEntitiesEqual(
      Object actual, Object expected, boolean checkFieldByField) {
    if (checkFieldByField) {
      assertEquals(actual, expected);
    }
    assertEquals(actual, expected);
    assertEquals(actual.hashCode(), expected.hashCode());
  }

  private static void assertEntitiesNotEqual(Object actual, Object expected) {
    assertNotEquals(actual, expected);
    assertNotEquals(actual.hashCode(), expected.hashCode());
  }
}
