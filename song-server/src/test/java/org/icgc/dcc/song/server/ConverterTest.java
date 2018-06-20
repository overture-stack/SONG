package org.icgc.dcc.song.server;

import lombok.val;
import org.icgc.dcc.song.core.utils.RandomGenerator;
import org.icgc.dcc.song.server.config.ConverterConfig;
import org.icgc.dcc.song.server.converter.FileConverter;
import org.icgc.dcc.song.server.converter.LegacyEntityConverter;
import org.icgc.dcc.song.server.model.entity.file.File;
import org.icgc.dcc.song.server.model.entity.file.FileData;
import org.icgc.dcc.song.server.model.entity.file.FileUpdateRequest;
import org.icgc.dcc.song.server.model.legacy.LegacyEntity;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.icgc.dcc.common.core.json.JsonNodeBuilders.object;
import static org.icgc.dcc.song.core.utils.RandomGenerator.createRandomGenerator;
import static org.icgc.dcc.song.server.model.entity.file.FileUpdateRequest.createFileUpdateRequest;

public class ConverterTest {

  private static final ConverterConfig CONVERTER_CONFIG = new ConverterConfig();
  private LegacyEntityConverter legacyEntityConverter = CONVERTER_CONFIG.legacyEntityConverter();
  private FileConverter fileConverter = CONVERTER_CONFIG.fileConverter();
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
        assertIsEqual(File::getAnalysisId, referenceFile, updatedFile);
        assertIsEqual(File::getFileName, referenceFile, updatedFile);
        assertIsEqual(File::getFileType, referenceFile, updatedFile);
        assertIsEqual(File::getStudyId, referenceFile, updatedFile);
        assertIsEqual(File::getObjectId, referenceFile, updatedFile);
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

  private static File buildReferenceFile(){
    val referenceFile = File.builder()
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
      File updatedFile, File referenceFile){
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
