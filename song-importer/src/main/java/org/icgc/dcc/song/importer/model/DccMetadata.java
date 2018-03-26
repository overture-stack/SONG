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

package org.icgc.dcc.song.importer.model;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.icgc.dcc.song.server.model.enums.AccessTypes;

import java.io.Serializable;
import java.util.Optional;

@Value
@RequiredArgsConstructor
public class DccMetadata implements Serializable{

  @NonNull private final String id;
  @NonNull private final AccessTypes accessType;
  private final long createdTime;
  @NonNull private final String fileName;
  @NonNull private final String gnosId;
  private String projectCode;

  public Optional<String> getProjectCode() {
    return Optional.ofNullable(projectCode);
  }

  public static DccMetadata createDccMetadata(String id, AccessTypes accessType, long createdTime,
      String fileName,
      String gnosId, String projectCode) {
    return new DccMetadata(id, accessType, createdTime, fileName, gnosId, projectCode);
  }

}
