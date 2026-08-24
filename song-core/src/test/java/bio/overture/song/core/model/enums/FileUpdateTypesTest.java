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
package bio.overture.song.core.model.enums;

import static bio.overture.song.core.model.enums.FileUpdateTypes.CONTENT_UPDATE;
import static bio.overture.song.core.model.enums.FileUpdateTypes.METADATA_UPDATE;
import static bio.overture.song.core.model.enums.FileUpdateTypes.NO_UPDATE;
import static bio.overture.song.core.model.enums.FileUpdateTypes.resolveFileUpdateType;
import static org.junit.Assert.assertEquals;

import bio.overture.song.core.model.FileData;
import bio.overture.song.core.model.FileUpdateRequest;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;
import lombok.Data;
import lombok.val;
import org.junit.Test;

public class FileUpdateTypesTest {

  @Data
  @Builder
  private static class TestFileData implements FileData {
    private String fileMd5sum;
    private Long fileSize;
    private String fileAccess;
    private String dataType;
    private JsonNode info;
  }

  // ---- null original field tests (currently NPE before fix) ----

  @Test
  public void nullOriginalMd5_nonNullUpdate_isContentUpdate() {
    val original = TestFileData.builder().fileMd5sum(null).fileSize(100L).build();
    val update = FileUpdateRequest.builder().fileMd5sum("aabbccddeeff00112233445566778899").build();
    assertEquals(CONTENT_UPDATE, resolveFileUpdateType(original, update));
  }

  @Test
  public void nullOriginalSize_nonNullUpdate_isContentUpdate() {
    val original =
        TestFileData.builder()
            .fileMd5sum("aabbccddeeff00112233445566778899")
            .fileSize(null)
            .build();
    val update = FileUpdateRequest.builder().fileSize(500L).build();
    assertEquals(CONTENT_UPDATE, resolveFileUpdateType(original, update));
  }

  @Test
  public void nullOriginalAccess_nonNullUpdate_isMetadataUpdate() {
    val original = TestFileData.builder().fileAccess(null).build();
    val update = FileUpdateRequest.builder().fileAccess("open").build();
    assertEquals(METADATA_UPDATE, resolveFileUpdateType(original, update));
  }

  @Test
  public void nullOriginalDataType_nonNullUpdate_isMetadataUpdate() {
    val original = TestFileData.builder().dataType(null).build();
    val update = FileUpdateRequest.builder().dataType("someType").build();
    assertEquals(METADATA_UPDATE, resolveFileUpdateType(original, update));
  }

  // ---- both null: no change ----

  @Test
  public void bothNullMd5_isNoUpdate() {
    val original = TestFileData.builder().fileMd5sum(null).fileSize(100L).build();
    val update = FileUpdateRequest.builder().fileMd5sum(null).build();
    assertEquals(NO_UPDATE, resolveFileUpdateType(original, update));
  }

  @Test
  public void bothNullSize_isNoUpdate() {
    val original =
        TestFileData.builder()
            .fileSize(null)
            .fileMd5sum("aabbccddeeff00112233445566778899")
            .build();
    val update = FileUpdateRequest.builder().fileSize(null).build();
    assertEquals(NO_UPDATE, resolveFileUpdateType(original, update));
  }

  // ---- non-null original, null update: no change (regression guard) ----

  @Test
  public void nonNullOriginalMd5_nullUpdate_isNoUpdate() {
    val original =
        TestFileData.builder()
            .fileMd5sum("aabbccddeeff00112233445566778899")
            .fileSize(100L)
            .build();
    val update = FileUpdateRequest.builder().fileMd5sum(null).build();
    assertEquals(NO_UPDATE, resolveFileUpdateType(original, update));
  }

  @Test
  public void nonNullOriginalSize_nullUpdate_isNoUpdate() {
    val original =
        TestFileData.builder()
            .fileSize(100L)
            .fileMd5sum("aabbccddeeff00112233445566778899")
            .build();
    val update = FileUpdateRequest.builder().fileSize(null).build();
    assertEquals(NO_UPDATE, resolveFileUpdateType(original, update));
  }

  // ---- non-null original, matching update: no change (regression guard) ----

  @Test
  public void matchingMd5_isNoUpdate() {
    val md5 = "aabbccddeeff00112233445566778899";
    val original = TestFileData.builder().fileMd5sum(md5).fileSize(100L).build();
    val update = FileUpdateRequest.builder().fileMd5sum(md5).build();
    assertEquals(NO_UPDATE, resolveFileUpdateType(original, update));
  }

  @Test
  public void matchingSize_isNoUpdate() {
    val original =
        TestFileData.builder()
            .fileMd5sum("aabbccddeeff00112233445566778899")
            .fileSize(100L)
            .build();
    val update = FileUpdateRequest.builder().fileSize(100L).build();
    assertEquals(NO_UPDATE, resolveFileUpdateType(original, update));
  }

  // ---- non-null original, different update: correct update type (regression guard) ----

  @Test
  public void differentMd5_isContentUpdate() {
    val original =
        TestFileData.builder()
            .fileMd5sum("aabbccddeeff00112233445566778899")
            .fileSize(100L)
            .build();
    val update = FileUpdateRequest.builder().fileMd5sum("00112233445566778899aabbccddeeff").build();
    assertEquals(CONTENT_UPDATE, resolveFileUpdateType(original, update));
  }

  @Test
  public void differentSize_isContentUpdate() {
    val original =
        TestFileData.builder()
            .fileMd5sum("aabbccddeeff00112233445566778899")
            .fileSize(100L)
            .build();
    val update = FileUpdateRequest.builder().fileSize(999L).build();
    assertEquals(CONTENT_UPDATE, resolveFileUpdateType(original, update));
  }

  @Test
  public void differentAccess_isMetadataUpdate() {
    val original = TestFileData.builder().fileAccess("open").build();
    val update = FileUpdateRequest.builder().fileAccess("controlled").build();
    assertEquals(METADATA_UPDATE, resolveFileUpdateType(original, update));
  }

  @Test
  public void differentDataType_isMetadataUpdate() {
    val original = TestFileData.builder().dataType("BAM").build();
    val update = FileUpdateRequest.builder().dataType("CRAM").build();
    assertEquals(METADATA_UPDATE, resolveFileUpdateType(original, update));
  }
}
