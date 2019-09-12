package bio.overture.song.server;

import bio.overture.song.server.converter.AnalysisTypeIdConverter;
import bio.overture.song.server.model.analysis.AnalysisTypeId;
import bio.overture.song.server.model.entity.AnalysisSchema;
import com.google.common.collect.ImmutableSet;
import lombok.val;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mapstruct.factory.Mappers.getMapper;

public class AnalysisTypeIdConverterTest {

  private static final AnalysisTypeIdConverter CONVERTER = getMapper(AnalysisTypeIdConverter.class);
  private static final String NAME = "something";
  private static final Integer VERSION = 9938;

  @Test
  public void convertToString_analysisTypeId_success(){
    val inputAid = AnalysisTypeId.builder()
        .name(NAME)
        .version(VERSION)
        .build();
    val expected = NAME+":"+VERSION;
    val actual = CONVERTER.convertToString(inputAid);
    assertEquals(actual, expected);
  }

  @Test
  public void convertToString_analysisSchema_success(){
    val inputASchema = AnalysisSchema.builder()
        .analyses(ImmutableSet.of())
        .schema(null)
        .name(NAME)
        .version(VERSION)
        .build();
    val expected = NAME+":"+VERSION;
    val actual = CONVERTER.convertToString(inputASchema);
    assertEquals(actual, expected);
  }

  @Test
  public void convertToAnalysisTypeId_analysisSchema_success(){
    val inputASchema = AnalysisSchema.builder()
        .analyses(ImmutableSet.of())
        .schema(null)
        .name(NAME)
        .version(VERSION)
        .build();
    val expected = AnalysisTypeId.builder()
        .name(NAME)
        .version(VERSION)
        .build();
    val actual = CONVERTER.convertToAnalysisTypeId(inputASchema);
    assertEquals(actual, expected);
  }

  @Test
  public void convertFromString_string_success(){
    val input = NAME+":"+VERSION;
    val expected = AnalysisTypeId.builder()
        .name(NAME)
        .version(VERSION)
        .build();
    val actual = CONVERTER.convertFromString(input);
    assertEquals(actual, expected);
  }

  // checkName name missing error
  // convertToAnalysisTypeId analysisTypeID missing version error
  // convertToAnalysisTypeId analysisTypeID missing name error
  // convertFromString null or empty error
  // convertFromString name only error
  // convertFromString name only error

}
