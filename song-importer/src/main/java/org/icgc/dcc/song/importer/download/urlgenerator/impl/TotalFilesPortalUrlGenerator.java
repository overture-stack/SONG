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

import com.google.common.base.Joiner;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.icgc.dcc.song.importer.download.queries.PortalQuery;
import org.icgc.dcc.song.importer.download.urlgenerator.UrlGenerator;

import java.net.URL;

import static java.net.URLEncoder.encode;
import static java.nio.charset.StandardCharsets.UTF_8;

@RequiredArgsConstructor
public class TotalFilesPortalUrlGenerator implements UrlGenerator {

  private static final String REPOSITORY_FILES_ENDPOINT = "/api/v1/repository/files/summary";
  private static final Joiner AMPERSAND_JOINER = Joiner.on("&");

  @NonNull private final String serverUrl;
  @NonNull private final PortalQuery portalQuery;

  @Override
  @SneakyThrows
  public URL getUrl(int size, int from) {
    return new URL(
        AMPERSAND_JOINER.join(
            serverUrl+ REPOSITORY_FILES_ENDPOINT +"?",
            getFiltersParam()
        ));
  }

  private String getFiltersParam(){
    return "filters="+encodeFilter();
  }

  @SneakyThrows
  private String encodeFilter(){
    return encode(portalQuery.toString(), UTF_8.name());
  }

  public static TotalFilesPortalUrlGenerator createTotalFilesPortalUrlGenerator(String serverUrl,
      PortalQuery portalQuery) {
    return new TotalFilesPortalUrlGenerator(serverUrl, portalQuery);
  }

}
