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

package org.icgc.dcc.song.importer.download.urlgenerator.impl;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import org.icgc.dcc.song.importer.download.urlgenerator.UrlGenerator;

import java.net.URL;

import static java.lang.String.format;

@RequiredArgsConstructor
public class DccMetadataUrlGenerator implements UrlGenerator {

  @NonNull private final String serverUrl;

  @Override
  @SneakyThrows
  public URL getUrl(int size, int from) {
    val page = calcPage(size, from);
    val urlString = format("%s/entities?size=%s&page=%s", serverUrl, size, page);
    return new URL(urlString);
  }

  private int calcPage(int size, int from){
    return from/size;
  }

  public static DccMetadataUrlGenerator createDccMetadataUrlGenerator(String serverUrl) {
    return new DccMetadataUrlGenerator(serverUrl);
  }

}
