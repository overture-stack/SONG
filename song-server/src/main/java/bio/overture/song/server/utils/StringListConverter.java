package bio.overture.song.server.utils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter
public class StringListConverter implements AttributeConverter<List<String>, String> {

  @Override
  public String convertToDatabaseColumn(List<String> attribute) {
    if (attribute == null || attribute.isEmpty()) {
      return "{}";
    }
    return "{"
        + String.join(
            ",", attribute.stream().map(s -> "\"" + s + "\"").collect(Collectors.toList()))
        + "}";
  }

  @Override
  public List<String> convertToEntityAttribute(String dbData) {
    if (dbData == null || dbData.equals("{}")) {
      return Arrays.asList();
    }
    return Arrays.stream(dbData.substring(1, dbData.length() - 1).split(","))
        .map(s -> s.replace("\"", ""))
        .collect(Collectors.toList());
  }
}
