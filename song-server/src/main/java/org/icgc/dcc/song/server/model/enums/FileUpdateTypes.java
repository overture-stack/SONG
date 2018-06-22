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

package org.icgc.dcc.song.server.model.enums;

import lombok.val;
import org.icgc.dcc.song.server.model.entity.file.File;
import org.icgc.dcc.song.server.model.entity.file.FileData;

import java.util.function.Function;

import static java.util.Objects.isNull;

public enum FileUpdateTypes {
  NO_UPDATE,
  CONTENT_UPDATE,
  METADATA_UPDATE;

  public static FileUpdateTypes resolveFileUpdateType(File originalFile, FileData fileUpdateData){
    if (isChangedContent(originalFile, fileUpdateData)){
      return CONTENT_UPDATE;
    } else if(isChangedMetadata(originalFile, fileUpdateData)){
      return METADATA_UPDATE;
    } else {
      return NO_UPDATE;
    }
  }

  private static boolean isUnchanged(Function<FileData, ?> getterFunction, File originalFile, FileData fileUpdateData ){
    val value = getterFunction.apply(fileUpdateData);
    return isNull(value) || getterFunction.apply(originalFile).equals(value);
  }

  private static boolean isUnchangedMd5(File originalFile, FileData fileUpdataData){
    return isUnchanged(FileData::getFileMd5sum, originalFile, fileUpdataData);
  }

  private static boolean isUnchangedSize(File originalFile, FileData fileUpdataData){
    return isUnchanged(FileData::getFileSize, originalFile, fileUpdataData);
  }

  private static boolean isUnchangedAccess(File originalFile, FileData fileUpdataData){
    return isUnchanged(FileData::getFileAccess, originalFile, fileUpdataData);
  }

  private static boolean isUnchangedInfo(File originalFile, FileData fileUpdataData){
    return isUnchanged(FileData::getInfo, originalFile, fileUpdataData);
  }

  private static boolean isChangedContent(File originalFile, FileData fileUpdataData){
    return !(isUnchangedMd5(originalFile, fileUpdataData) && isUnchangedSize(originalFile, fileUpdataData));
  }

  private static boolean isChangedMetadata(File originalFile, FileData fileUpdateData){
    return !(isUnchangedAccess(originalFile, fileUpdateData) && isUnchangedInfo(originalFile, fileUpdateData));
  }

}
