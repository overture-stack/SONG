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

import org.icgc.dcc.song.server.model.entity.file.File;
import org.icgc.dcc.song.server.model.entity.file.FileData;

import java.util.function.Function;

public enum FileUpdateTypes {
  NO_UPDATE,
  CONTENT_UPDATE,
  METADATA_UPDATE;

  public static FileUpdateTypes resolveFileUpdateType(File originalFile, FileData fileUpdateData){
    if (isDifferentContent(originalFile, fileUpdateData)){
      return CONTENT_UPDATE;
    } else if(isDifferentMetadata(originalFile, fileUpdateData)){
      return METADATA_UPDATE;
    } else {
      return NO_UPDATE;
    }
  }

  private static boolean isEqual(Function<FileData, ?> getterFunction, File originalFile, FileData fileUpdateData ){
    return getterFunction.apply(originalFile).equals(getterFunction.apply(fileUpdateData));
  }

  private static boolean isSameMd5(File originalFile, FileData fileUpdataData){
    return isEqual(FileData::getFileMd5sum, originalFile, fileUpdataData);
  }

  private static boolean isSameSize(File originalFile, FileData fileUpdataData){
    return isEqual(FileData::getFileSize, originalFile, fileUpdataData);
  }

  private static boolean isSameAccess(File originalFile, FileData fileUpdataData){
    return isEqual(FileData::getFileAccess, originalFile, fileUpdataData);
  }

  private static boolean isSameInfo(File originalFile, FileData fileUpdataData){
    return isEqual(FileData::getInfo, originalFile, fileUpdataData);
  }

  private static boolean isDifferentContent(File originalFile, FileData fileUpdataData){
    return !(isSameMd5(originalFile, fileUpdataData) && isSameSize(originalFile, fileUpdataData));
  }

  private static boolean isDifferentMetadata(File originalFile, FileData fileUpdateData){
    return !(isSameAccess(originalFile, fileUpdateData) && isSameInfo(originalFile, fileUpdateData));
  }

}
