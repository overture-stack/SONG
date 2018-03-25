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
public class PortalDonorMetadata implements Serializable{

  @NonNull private final String donorId;
  @NonNull private final String projectId;
  @NonNull private final String submittedDonorId;
  private final String projectName;
  private final String gender;

  public Optional<String> getGender() {
    return Optional.of(gender);
  }

  public Optional<String> getProjectName(){
    return Optional.of(projectName);
  }

}
