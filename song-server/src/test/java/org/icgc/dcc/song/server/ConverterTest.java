package org.icgc.dcc.song.server;

import com.googlecode.protobuf.format.JsonFormat;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.icgc.dcc.song.schema.FileOuterClass;
import org.icgc.dcc.song.server.config.ConverterConfig;
import org.icgc.dcc.song.server.converter.LegacyEntityConverter;
import org.icgc.dcc.song.server.model.legacy.LegacyEntity;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class ConverterTest {

  private static final ConverterConfig CONVERTER_CONFIG = new ConverterConfig();
  private LegacyEntityConverter legacyEntityConverter = CONVERTER_CONFIG.legacyEntityConverter();

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

  @Test
  public void testPro(){
    val mym = FileOuterClass.File.newBuilder()
        .setAnalysisId("sdf")
        .setFileAccess("controlled")
        .build();
    val f = new JsonFormat();
    val out = f.printToString(mym);
    log.info("file: {}", out );

  }

  private boolean isObjectsEqual(Object o1, Object o2){
    return o1 == o2;
  }

}
