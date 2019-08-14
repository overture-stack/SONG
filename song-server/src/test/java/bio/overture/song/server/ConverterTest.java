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

package bio.overture.song.server;

import bio.overture.song.core.model.file.FileData;
import bio.overture.song.core.model.file.FileUpdateRequest;
import bio.overture.song.core.utils.RandomGenerator;
import bio.overture.song.server.config.ConverterConfig;
import bio.overture.song.server.converter.FileConverter;
import bio.overture.song.server.converter.LegacyEntityConverter;
import bio.overture.song.server.model.entity.FileEntity;
import bio.overture.song.server.model.legacy.LegacyEntity;
import lombok.val;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.icgc.dcc.common.core.json.JsonNodeBuilders.object;
import static bio.overture.song.core.model.file.FileUpdateRequest.createFileUpdateRequest;
import static bio.overture.song.core.utils.RandomGenerator.createRandomGenerator;

public class ConverterTest {

  private static final ConverterConfig CONVERTER_CONFIG = new ConverterConfig();
  private LegacyEntityConverter legacyEntityConverter = LegacyEntityConverter.INSTANCE;
  private FileConverter fileConverter = FileConverter.INSTANCE;
  private static final int MAX_NUMBER_OF_REQUEST_PARAMS = 4;
  private static final RandomGenerator RANDOM_GENERATOR = createRandomGenerator(ConverterTest.class.getSimpleName());
  private static final String UNIQUE_MD5_1 = RANDOM_GENERATOR.generateRandomMD5();
  private static final String UNIQUE_MD5_2 = RANDOM_GENERATOR.generateRandomMD5();

  @BeforeClass
  public static void beforeClass(){
    assertThat(UNIQUE_MD5_1).isNotEqualTo(UNIQUE_MD5_2);
  }

  @Test
  public void testFileCopy(){
    val referenceFile = buildReferenceFile();
    val copiedFile = fileConverter.copyFile(referenceFile);
    assertThat(copiedFile == referenceFile).isFalse();
    assertThat(copiedFile).isEqualTo(referenceFile);
  }

  @Test
  public void testEntityToUpdateRequestConversion(){
    val referenceFile = buildReferenceFile();
    val referenceUpdateRequest = createFileUpdateRequest(
        referenceFile.getFileSize(),
        referenceFile.getFileMd5sum(),
        referenceFile.getFileAccess(),
        referenceFile.getInfo());
    val outputFileUpdateRequest = fileConverter.fileEntityToFileUpdateRequest(referenceFile);
    assertThat(outputFileUpdateRequest).isEqualTo(referenceUpdateRequest);
  }

  @Test
  public void testFileUpdate(){
    val referenceFile = buildReferenceFile();
    for(int i=0; i< getNumberOfPermutations(); i++){
      val fileUpdateRequest = generateFileUpdateRequest(i);
      val updatedFile = fileConverter.copyFile(referenceFile);
      fileConverter.updateEntityFromData(fileUpdateRequest, updatedFile);
      if (i == 0) {
        assertThat(updatedFile).isEqualTo(referenceFile);
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
  public void testLegacyEntityConversion(){
    val legacyEntity = LegacyEntity.builder()
        .access("controlled")
        .fileName("fn1")
        .gnosId("an1")
        .id("fi1")
        .projectCode("ABC123")
        .build();

    val legacyDto = legacyEntityConverter.convertToLegacyDto(legacyEntity);
    assertThat(legacyDto).isEqualToComparingFieldByField(legacyEntity);
    assertThat(isObjectsEqual(legacyDto, legacyEntity)).isFalse();

    val legacyEntityCopy = legacyEntityConverter.convertToLegacyEntity(legacyEntity);
    assertThat(legacyEntityCopy).isEqualToComparingFieldByField(legacyEntity);
    assertThat(isObjectsEqual(legacyEntityCopy, legacyEntity)).isFalse();

    val legacyEntityCopy2 = legacyEntityConverter.convertToLegacyEntity(legacyDto);
    assertThat(legacyEntityCopy2).isEqualToComparingFieldByField(legacyDto);
    assertThat(isObjectsEqual(legacyEntityCopy2, legacyDto)).isFalse();

    val legacyDtoCopy = legacyEntityConverter.convertToLegacyDto(legacyDto);
    assertThat(legacyDtoCopy).isEqualToComparingFieldByField(legacyDto);
    assertThat(isObjectsEqual(legacyDtoCopy, legacyDto)).isFalse();
  }

  private static FileEntity buildReferenceFile(){
    val referenceFile = FileEntity.builder()
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

  private static <T> void assertIsEqual(Function<T, ?> getterCallback, T left, T right ){
    assertThat(getterCallback.apply(left)).isEqualTo(getterCallback.apply(right));
  }

  private static void assertConfigEqual(int id, int parameterNumFrom0, Function<FileData, ?> getterCallback,
      FileEntity updatedFile, FileEntity referenceFile){
    if (isConfigEnabled(id, parameterNumFrom0)){
      assertThat(getterCallback.apply(updatedFile)).isNotEqualTo(getterCallback.apply(referenceFile));
    } else {
      assertThat(getterCallback.apply(updatedFile)).isEqualTo(getterCallback.apply(referenceFile));
    }
  }

  private static int getNumberOfPermutations(){
    return 1 << MAX_NUMBER_OF_REQUEST_PARAMS;
  }

  private static boolean isConfigEnabled(int id, int parameterNumFrom0){
    return (id & (1<<parameterNumFrom0)) > 0;
  }

  private static FileUpdateRequest generateFileUpdateRequest(int id){
    assertThat(id).isLessThan(getNumberOfPermutations());
    val builder  =FileUpdateRequest.builder();
    if (isConfigEnabled(id, 0)){
      builder.fileAccess("controlled");
    }

    if (isConfigEnabled(id, 1)){
      builder.fileMd5sum(UNIQUE_MD5_2);
    }

    if (isConfigEnabled(id, 2)){
      builder.fileSize(999999L);
    }

    if (isConfigEnabled(id, 3)) {
      builder.info(object()
          .with( "myInfoKey2", "myInfoValue2" )
          .end());
    }

    return builder.build();
  }

  private static boolean isObjectsEqual(Object o1, Object o2){
    return o1 == o2;
  }

}
