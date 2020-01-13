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

package bio.overture.song.server;

import static bio.overture.song.core.model.FileUpdateRequest.createFileUpdateRequest;
import static bio.overture.song.core.utils.RandomGenerator.createRandomGenerator;
import static bio.overture.song.server.utils.JsonNodeBuilders.object;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import bio.overture.song.core.model.FileData;
import bio.overture.song.core.model.FileUpdateRequest;
import bio.overture.song.core.utils.RandomGenerator;
import bio.overture.song.server.config.ConverterConfig;
import bio.overture.song.server.converter.FileConverter;
import bio.overture.song.server.converter.LegacyEntityConverter;
import bio.overture.song.server.model.entity.FileEntity;
import bio.overture.song.server.model.legacy.LegacyEntity;
import java.util.function.Function;
import lombok.val;
import org.junit.BeforeClass;
import org.junit.Test;

public class ConverterTest {

  private static final ConverterConfig CONVERTER_CONFIG = new ConverterConfig();
  private LegacyEntityConverter legacyEntityConverter = LegacyEntityConverter.INSTANCE;
  private FileConverter fileConverter = FileConverter.INSTANCE;
  private static final int MAX_NUMBER_OF_REQUEST_PARAMS = 4;
  private static final RandomGenerator RANDOM_GENERATOR =
      createRandomGenerator(ConverterTest.class.getSimpleName());
  private static final String UNIQUE_MD5_1 = RANDOM_GENERATOR.generateRandomMD5();
  private static final String UNIQUE_MD5_2 = RANDOM_GENERATOR.generateRandomMD5();

  @BeforeClass
  public static void beforeClass() {
    assertNotEquals(UNIQUE_MD5_1, UNIQUE_MD5_2);
  }

  @Test
  public void testFileCopy() {
    val referenceFile = buildReferenceFile();
    val copiedFile = fileConverter.copyFile(referenceFile);
    assertFalse(copiedFile == referenceFile);
    assertEquals(copiedFile, referenceFile);
  }

  @Test
  public void testEntityToUpdateRequestConversion() {
    val referenceFile = buildReferenceFile();
    val referenceUpdateRequest =
        createFileUpdateRequest(
            referenceFile.getFileSize(),
            referenceFile.getFileMd5sum(),
            referenceFile.getFileAccess(),
            referenceFile.getInfo());
    val outputFileUpdateRequest = fileConverter.fileEntityToFileUpdateRequest(referenceFile);
    assertEquals(outputFileUpdateRequest, referenceUpdateRequest);
  }

  @Test
  public void testFileUpdate() {
    val referenceFile = buildReferenceFile();
    for (int i = 0; i < getNumberOfPermutations(); i++) {
      val fileUpdateRequest = generateFileUpdateRequest(i);
      val updatedFile = fileConverter.copyFile(referenceFile);
      fileConverter.updateEntityFromData(fileUpdateRequest, updatedFile);
      if (i == 0) {
        assertEquals(updatedFile, referenceFile);
      } else {
        assertConfigEqual(i, 0, FileData::getFileAccess, updatedFile, referenceFile);
        assertConfigEqual(i, 1, FileData::getFileMd5sum, updatedFile, referenceFile);
        assertConfigEqual(i, 2, FileData::getFileSize, updatedFile, referenceFile);
        assertConfigEqual(i, 3, FileData::getInfo, updatedFile, referenceFile);
        assertIsEqual(FileEntity::getAnalysisId, referenceFile, updatedFile);
        assertIsEqual(FileEntity::getFileName, referenceFile, updatedFile);
        assertIsEqual(FileEntity::getFileType, referenceFile, updatedFile);
        assertIsEqual(FileEntity::getStudyId, referenceFile, updatedFile);
        assertIsEqual(FileEntity::getObjectId, referenceFile, updatedFile);
      }
    }
  }

  @Test
  public void testLegacyEntityConversion() {
    val legacyEntity =
        LegacyEntity.builder()
            .access("controlled")
            .fileName("fn1")
            .gnosId("an1")
            .id("fi1")
            .projectCode("ABC123")
            .build();

    val legacyDto = legacyEntityConverter.convertToLegacyDto(legacyEntity);
    assertFalse(isObjectsEqual(legacyDto, legacyEntity));
    assertEquals(legacyEntity.getAccess(), legacyDto.getAccess());
    assertEquals(legacyEntity.getFileName(), legacyDto.getFileName());
    assertEquals(legacyEntity.getGnosId(), legacyDto.getGnosId());
    assertEquals(legacyEntity.getId(), legacyDto.getId());
    assertEquals(legacyEntity.getProjectCode(), legacyDto.getProjectCode());

    val legacyEntityCopy = legacyEntityConverter.convertToLegacyEntity(legacyEntity);
    assertEquals(legacyEntityCopy, legacyEntity);
    assertFalse(isObjectsEqual(legacyEntityCopy, legacyEntity));

    val legacyEntityCopy2 = legacyEntityConverter.convertToLegacyEntity(legacyDto);
    assertFalse(isObjectsEqual(legacyEntityCopy2, legacyDto));
    assertEquals(legacyEntityCopy2.getAccess(), legacyDto.getAccess());
    assertEquals(legacyEntityCopy2.getFileName(), legacyDto.getFileName());
    assertEquals(legacyEntityCopy2.getGnosId(), legacyDto.getGnosId());
    assertEquals(legacyEntityCopy2.getId(), legacyDto.getId());
    assertEquals(legacyEntityCopy2.getProjectCode(), legacyDto.getProjectCode());

    val legacyDtoCopy = legacyEntityConverter.convertToLegacyDto(legacyDto);
    assertEquals(legacyDtoCopy, legacyDto);
    assertFalse(isObjectsEqual(legacyDtoCopy, legacyDto));
  }

  private static FileEntity buildReferenceFile() {
    val referenceFile =
        FileEntity.builder()
            .analysisId("AN1")
            .objectId("FI1")
            .studyId("ABC123")
            .fileName("myFilename.bam")
            .fileAccess("open")
            .fileMd5sum(UNIQUE_MD5_1)
            .fileSize(777777L)
            .fileType("BAM")
            .build();
    referenceFile.setInfo(object().with("myInfoKey1", "myInfoValue1").end());
    return referenceFile;
  }

  private static <T> void assertIsEqual(Function<T, ?> getterCallback, T left, T right) {
    assertEquals(getterCallback.apply(left), getterCallback.apply(right));
  }

  private static void assertConfigEqual(
      int id,
      int parameterNumFrom0,
      Function<FileData, ?> getterCallback,
      FileEntity updatedFile,
      FileEntity referenceFile) {
    if (isConfigEnabled(id, parameterNumFrom0)) {
      assertNotEquals(getterCallback.apply(updatedFile), getterCallback.apply(referenceFile));
    } else {
      assertEquals(getterCallback.apply(updatedFile), getterCallback.apply(referenceFile));
    }
  }

  private static int getNumberOfPermutations() {
    return 1 << MAX_NUMBER_OF_REQUEST_PARAMS;
  }

  private static boolean isConfigEnabled(int id, int parameterNumFrom0) {
    return (id & (1 << parameterNumFrom0)) > 0;
  }

  private static FileUpdateRequest generateFileUpdateRequest(int id) {
    assertTrue(id < getNumberOfPermutations());
    val builder = FileUpdateRequest.builder();
    if (isConfigEnabled(id, 0)) {
      builder.fileAccess("controlled");
    }

    if (isConfigEnabled(id, 1)) {
      builder.fileMd5sum(UNIQUE_MD5_2);
    }

    if (isConfigEnabled(id, 2)) {
      builder.fileSize(999999L);
    }

    if (isConfigEnabled(id, 3)) {
      builder.info(object().with("myInfoKey2", "myInfoValue2").end());
    }

    return builder.build();
  }

  private static boolean isObjectsEqual(Object o1, Object o2) {
    return o1 == o2;
  }
}
