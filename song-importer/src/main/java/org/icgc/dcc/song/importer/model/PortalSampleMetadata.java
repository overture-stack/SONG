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

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.io.Serializable;
import java.util.Optional;

@Value
@Builder
public class PortalSampleMetadata implements Serializable{

  public static final long serialVersionUID = 1499437058L;

  @NonNull private final String id;
  @NonNull private final String analyzedId;
  @NonNull private final String study;
  private final String libraryStrategy;

  public Optional<String> getLibraryStrategy(){
    return Optional.of(libraryStrategy);
  }


}
