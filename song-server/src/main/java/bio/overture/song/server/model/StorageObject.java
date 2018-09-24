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

package bio.overture.song.server.model;

import bio.overture.song.core.model.file.FileContent;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import static com.google.common.base.Strings.isNullOrEmpty;

@Value
@Builder
public class StorageObject implements FileContent {

  @NonNull private final String objectId;
  private final String fileMd5sum;
  @NonNull private final Long fileSize;

  public boolean isMd5Defined(){
    return !isNullOrEmpty(fileMd5sum);
  }

}
