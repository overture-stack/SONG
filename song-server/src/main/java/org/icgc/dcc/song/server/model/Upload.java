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
import com.google.common.base.Joiner;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.icgc.dcc.song.server.model.enums.ModelAttributeNames;
import org.icgc.dcc.song.server.model.enums.TableAttributeNames;
import org.icgc.dcc.song.server.model.enums.TableNames;
import org.icgc.dcc.song.server.model.enums.UploadStates;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Objects.isNull;
import static org.icgc.dcc.song.server.model.enums.UploadStates.resolveState;

@Entity
@Table(name = TableNames.UPLOAD)
@Data
@Builder
@RequiredArgsConstructor
@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonPropertyOrder({
    ModelAttributeNames.ANALYSIS_ID,
    ModelAttributeNames.UPLOAD_ID,
    ModelAttributeNames.STUDY_ID,
    ModelAttributeNames.STATE,
    ModelAttributeNames.CREATED_AT,
    ModelAttributeNames.UPDATED_AT,
    ModelAttributeNames.ERRORS,
    ModelAttributeNames.PAYLOAD
})
public class Upload {

  private static final String INPUT_ERROR_DELIM = "\\|";
  private static final String OUTPUT_ERROR_DELIM = "|";

  @Id
  @Column(name = TableAttributeNames.ID, updatable = false, unique = true, nullable = false)
  private String uploadId;

  @Column(name = TableAttributeNames.ANALYSIS_ID, nullable = false)
  private String analysisId;

  @Column(name = TableAttributeNames.STUDY_ID, nullable = false)
  private String studyId;

  @Column(name = TableAttributeNames.STATE, nullable = false)
  private String state;

  @Column(name = TableAttributeNames.ERRORS, nullable = false)
  private String errors;

  @Column(name = TableAttributeNames.PAYLOAD, nullable = false)
  private String payload;

  @CreationTimestamp
  @Column(name = TableAttributeNames.CREATED_AT, updatable= false, nullable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = TableAttributeNames.UPDATED_AT, nullable = false)
  private LocalDateTime updatedAt;

  public Upload(String uploadId, String analysisId, String studyId, String state, String errors, String payload,
      LocalDateTime createdAt, LocalDateTime updatedAt) {
    this.uploadId = uploadId;
    this.analysisId = analysisId;
    this.studyId = studyId;
    setState(state);
    this.errors = errors;
    this.payload = payload;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public void setState(@NonNull UploadStates state){
    this.state = state.getText();
  }

  public void setState(@NonNull String state){
    setState(resolveState(state));
  }

  @JsonRawValue
  public String getPayload() {
    return payload;
  }

  public void setErrors(String errorString) {
    this.errors = errorString;
    if (isNull(errorString)) {
      this.errors = "";
    }
  }

  public List<String> getErrors(){
    if(isNull(errors)){
      return newArrayList();
    } else {
      return newArrayList(errors.split(INPUT_ERROR_DELIM));
    }
  }

  public void addErrors(Collection<String> errors) {
    val e = getErrors();
    e.addAll(errors);
    this.errors = Joiner.on(OUTPUT_ERROR_DELIM).join(e);
  }

}
