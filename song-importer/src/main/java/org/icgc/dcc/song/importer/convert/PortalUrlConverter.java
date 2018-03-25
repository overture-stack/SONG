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
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.icgc.dcc.common.core.util.stream.Streams;
import org.icgc.dcc.song.importer.model.PortalFileMetadata;

import java.net.URL;
import java.util.List;

import static org.icgc.dcc.common.core.util.stream.Collectors.toImmutableList;
import static org.icgc.dcc.song.core.utils.JsonUtils.read;
import static org.icgc.dcc.song.importer.convert.PortalFileMetadataConverter.convertToPortalFileMetadata;

@RequiredArgsConstructor
public class PortalUrlConverter implements Converter<URL, List<PortalFileMetadata>> {

  private static final String HITS = "hits";

  @NonNull private final String repoName;

  @Override
  public List<PortalFileMetadata> convert(URL url) {
    return Streams.stream(getHits(read(url)))
        .map(x -> convertToPortalFileMetadata(x, repoName))
        .collect(toImmutableList());
  }

  private synchronized static JsonNode getHits(JsonNode result) {
    return result.get(HITS);
  }

  public static PortalUrlConverter createPortalUrlConverter(String repoName) {
    return new PortalUrlConverter(repoName);
  }

}
