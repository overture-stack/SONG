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

package org.icgc.dcc.song.importer.download;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.net.URL;

import static org.icgc.dcc.song.core.utils.JsonUtils.read;

@Slf4j
@RequiredArgsConstructor
public class PortalDonorIdFetcher {

  private static final String REPOSITORY_DONORS_ENDPOINT = "/api/v1/donors";
  private static final String INCLUDE_PARAM = "include=specimen";

  private final String serverUrl;

  @SneakyThrows
  public URL getUrl(String donorId) {
    val url = new URL( serverUrl+ REPOSITORY_DONORS_ENDPOINT+"/"+donorId +"?"+INCLUDE_PARAM);
    return url;
  }

  public JsonNode getDonorMetadata(String donorId){
    return read(getUrl(donorId));
  }

  public static PortalDonorIdFetcher createPortalDonorIdFetcher(String serverUrl) {
    return new PortalDonorIdFetcher(serverUrl);
  }

}
