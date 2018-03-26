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

package org.icgc.dcc.song.server.model.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;
import lombok.val;
import org.icgc.dcc.song.server.model.Metadata;
import org.icgc.dcc.song.server.model.enums.AccessTypes;
import org.icgc.dcc.song.server.model.enums.Constants;

import java.io.Serializable;

import static org.icgc.dcc.song.server.model.enums.AccessTypes.resolveAccessType;

@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_ABSENT)
@ToString(callSuper = true)
@Data
public class File extends Metadata implements Serializable {

  private String objectId = "";
  private String analysisId = "";
  private String fileName = "";
  private String studyId = "";
  private Long fileSize = -1L;
  private String fileType = "";
  private String fileMd5sum = "";
  private String fileAccess= "";

  public static File create(String id, String analysisId, String name, String study, Long size,
                            String type, String md5, AccessTypes access) {
    val f = new File();
    f.setObjectId(id);
    f.setAnalysisId(analysisId);
    f.setFileName(name);
    f.setStudyId(study);
    f.setFileSize(size);
    f.setFileType(type);
    f.setFileMd5sum(md5);
    f.setFileAccess(access);
    return f;
  }

  public void setFileType(String type) {
    Constants.validate(Constants.FILE_TYPE, type);
    fileType = type;
  }

  public void setFileAccess(@NonNull AccessTypes access){
    this.fileAccess = access.toString();
  }

  public void setFileAccess(@NonNull String access){
    setFileAccess(resolveAccessType(access));
  }

}
