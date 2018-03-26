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
package org.icgc.dcc.song.server.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonRawValue;
import lombok.Data;
import lombok.NonNull;
import lombok.val;
import org.icgc.dcc.song.server.model.enums.UploadStates;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Objects.isNull;
import static org.icgc.dcc.song.server.model.enums.UploadStates.resolveState;

@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonPropertyOrder({ "analysisId", "uploadId", "studyId", "state", "createdAt", "updatedAt", "errors", "payload"
})

@Data
public class Upload {

  private String uploadId = "";
  private String studyId = "";
  private String state = "";
  private String analysisId = "";
  private List<String> errors = new ArrayList<>();
  private String payload = "";
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  public void setState(@NonNull UploadStates state){
    this.state = state.getText();
  }

  public void setState(@NonNull String state){
    setState(resolveState(state));
  }

  public static Upload create(String id, String study, String analysisId, UploadStates state, String errors,
      String payload, LocalDateTime created, LocalDateTime updated) {
    val u = new Upload();

    u.setUploadId(id);
    u.setStudyId(study);
    u.setAnalysisId(analysisId);
    u.setState(state);
    u.setErrors(errors);
    u.setPayload(payload);
    u.setCreatedAt(created);
    u.setUpdatedAt(updated);
    return u;
  }

  @JsonRawValue
  public String getPayload() {
    return payload;
  }

  public void setErrors(String errorString) {
    if (isNull(errorString)) {
      errorString = "";
    }

    this.errors.clear();
    this.errors.addAll(asList(errorString.split("\\|")));
  }

  public void addErrors(Collection<String> errors) {
    this.errors.addAll(errors);
  }

}
