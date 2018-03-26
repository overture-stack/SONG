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
package org.icgc.dcc.song.importer.parser;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public final class DonorPortalJsonParser {

  public static String getProjectName(@NonNull JsonNode donor){
    return donor.path(FieldNames.PROJECT_NAME).textValue();
  }

  public static String getDonorId(@NonNull JsonNode donor){
    return donor.path(FieldNames.ID).textValue();
  }

  public static String getGender(@NonNull JsonNode donor){
    return donor.path(FieldNames.GENDER).textValue();
  }

  public static int getNumSpecimens(@NonNull JsonNode donor){
    return donor.path(FieldNames.SPECIMEN).size();
  }

  public static JsonNode getSpecimen(@NonNull JsonNode donor, int specimenIdx){
    return donor.path(FieldNames.SPECIMEN).get(specimenIdx);
  }

  public static String getProjectId(@NonNull JsonNode donor){
    return donor.path(FieldNames.PROJECT_ID).textValue();
  }

  public static String getSubmittedDonorId(@NonNull JsonNode donor){
    return donor.path(FieldNames.SUBMITTED_DONOR_ID).textValue();
  }

}
