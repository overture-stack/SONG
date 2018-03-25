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
