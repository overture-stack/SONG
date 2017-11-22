package org.icgc.dcc.song.importer.convert;

import com.fasterxml.jackson.databind.JsonNode;
import org.icgc.dcc.common.core.util.stream.Streams;
import org.icgc.dcc.song.importer.model.DccMetadata;

import java.net.URL;
import java.util.List;

import static org.icgc.dcc.common.core.util.stream.Collectors.toImmutableList;
import static org.icgc.dcc.song.core.utils.JsonUtils.read;

public class DccMetadataUrlConverter implements Converter<URL, List<DccMetadata>> {

  private static final String CONTENT = "content";

  @Override
  public List<DccMetadata> convert(URL url) {
    return Streams.stream(getContents(read(url)))
        .map(DccMetadataConverter::convertToDccMetadata)
        .collect(toImmutableList());
  }

  private static JsonNode getContents(JsonNode jsonNode) {
    return jsonNode.get(CONTENT);
  }

  public static DccMetadataUrlConverter createDccMetadataUrlConverter() {
    return new DccMetadataUrlConverter();
  }

}
