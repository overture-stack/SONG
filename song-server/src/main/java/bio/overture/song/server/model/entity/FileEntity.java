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

package bio.overture.song.server.model.entity;

import bio.overture.song.core.model.File;
import bio.overture.song.core.model.FileData;
import bio.overture.song.core.model.Metadata;
import bio.overture.song.core.model.enums.AccessTypes;
import bio.overture.song.core.model.enums.FileTypes;
import bio.overture.song.server.model.enums.TableAttributeNames;
import bio.overture.song.server.model.enums.TableNames;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;

@Entity
@Table(name = TableNames.FILE)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_ABSENT)
public class FileEntity extends Metadata implements Serializable, FileData, File {

  @Id
  @Column(name = TableAttributeNames.ID, updatable = false, unique = true, nullable = false)
  private String objectId;

  @Column(name = TableAttributeNames.STUDY_ID, nullable = false)
  private String studyId;

  @Column(name = TableAttributeNames.ANALYSIS_ID, nullable = false)
  private String analysisId;

  @Column(name = TableAttributeNames.NAME, nullable = false)
  private String fileName;

  @Column(name = TableAttributeNames.SIZE, nullable = false)
  private Long fileSize;

  @Column(name = TableAttributeNames.TYPE, nullable = false)
  private String fileType;

  @Column(name = TableAttributeNames.MD5, nullable = false)
  private String fileMd5sum;

  @Column(name = TableAttributeNames.ACCESS, nullable = false)
  private String fileAccess;

  public void setFileType(FileTypes type) {
    this.fileType = type.toString();
  }

  public void setFileAccess(@NonNull AccessTypes access) {
    this.fileAccess = access.toString();
  }
}
