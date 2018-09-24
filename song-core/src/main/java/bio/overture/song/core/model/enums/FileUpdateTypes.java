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

package bio.overture.song.core.model.enums;

import lombok.val;
import bio.overture.song.core.model.file.FileContent;
import bio.overture.song.core.model.file.FileData;
import bio.overture.song.core.model.file.FileMetadata;

import java.util.function.Function;

import static java.util.Objects.isNull;

public enum FileUpdateTypes {
  NO_UPDATE,
  CONTENT_UPDATE,
  METADATA_UPDATE;

  public static FileUpdateTypes resolveFileUpdateType(FileData originalFile, FileData fileUpdateData){
    if (isChangedContent(originalFile, fileUpdateData)){
      return CONTENT_UPDATE;
    } else if(isChangedMetadata(originalFile, fileUpdateData)){
      return METADATA_UPDATE;
    } else {
      return NO_UPDATE;
    }
  }

  private static <T> boolean isUnchanged(Function<T, ?> getterFunction, T originalFile, T fileUpdateData ){
    val value = getterFunction.apply(fileUpdateData);
    return isNull(value) || getterFunction.apply(originalFile).equals(value);
  }

  private static boolean isUnchangedMd5(FileContent originalFile, FileContent fileUpdataData){
    return isUnchanged(FileContent::getFileMd5sum, originalFile, fileUpdataData);
  }

  private static boolean isUnchangedSize(FileContent originalFile, FileContent fileUpdataData){
    return isUnchanged(FileContent::getFileSize, originalFile, fileUpdataData);
  }

  private static boolean isUnchangedAccess(FileMetadata originalFile, FileMetadata fileUpdataData){
    return isUnchanged(FileMetadata::getFileAccess, originalFile, fileUpdataData);
  }

  private static boolean isUnchangedInfo(FileMetadata originalFile, FileMetadata fileUpdataData){
    return isUnchanged(FileMetadata::getInfo, originalFile, fileUpdataData);
  }

  private static boolean isChangedContent(FileContent originalFile, FileContent fileUpdataData){
    return !(isUnchangedMd5(originalFile, fileUpdataData) && isUnchangedSize(originalFile, fileUpdataData));
  }

  private static boolean isChangedMetadata(FileMetadata originalFile, FileMetadata fileUpdateData){
    return !(isUnchangedAccess(originalFile, fileUpdateData) && isUnchangedInfo(originalFile, fileUpdateData));
  }

}
