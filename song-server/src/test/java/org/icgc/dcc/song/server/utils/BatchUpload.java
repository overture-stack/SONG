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

package org.icgc.dcc.song.server.utils;

import lombok.Lombok;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.val;
import org.icgc.dcc.song.server.model.Upload;
import org.icgc.dcc.song.server.model.enums.UploadStates;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Maps.newHashMap;
import static java.nio.file.Files.newInputStream;
import static lombok.AccessLevel.PRIVATE;
import static org.icgc.dcc.common.core.util.Joiners.NEWLINE;
import static org.icgc.dcc.common.core.util.stream.Collectors.toImmutableList;
import static org.icgc.dcc.song.core.utils.JsonUtils.readTree;
import static org.icgc.dcc.song.core.utils.JsonUtils.toJson;
import static org.icgc.dcc.song.server.model.enums.UploadStates.SAVED;
import static org.icgc.dcc.song.server.model.enums.UploadStates.VALIDATED;
import static org.icgc.dcc.song.server.model.enums.UploadStates.VALIDATION_ERROR;
import static org.icgc.dcc.song.server.model.enums.UploadStates.resolveState;

@Value
@RequiredArgsConstructor(access = PRIVATE)
public class BatchUpload {

  /**
   * Config
   */
  @NonNull private final String studyId;
  @NonNull private final List<String> payloads;

  /**
   * State
   */

  @NonNull private final Map<String, Upload> uploadMap = newHashMap();

  private List<Upload> filter(Predicate<UploadStates> ... predicates){
    return uploadMap.values().stream()
        .filter(x -> decide(x, predicates))
        .collect(toImmutableList());
  }

  private static boolean decide(Upload upload, Predicate<UploadStates>...predicates){
    val u = resolveState(upload.getState());
    boolean result = true;
    for (val predicate : predicates){
      result |= predicate.test(u);
    }
    return result;
  }

  public List<Upload> getValidatedUploads(){
    return filter(state -> state == VALIDATED);
  }

  public List<Upload> getErroredUploads(){
    return filter(state -> state == VALIDATION_ERROR);
  }

  public List<Upload> getSavedUplouds(){
    return filter(state -> state == SAVED);
  }

  public List<Upload> getPendingUploads(){
    return filter(state -> state != VALIDATION_ERROR,
        state -> state != VALIDATED,
        state -> state != SAVED);
  }

  public boolean isDone(){
    return getPendingUploads().isEmpty();
  }

  public void addUpload(@NonNull Upload upload){
    checkArgument(!uploadMap.containsKey(upload.getUploadId()),
        "The upload '%s' was already added", upload.getUploadId());
    uploadMap.put(upload.getUploadId(), upload);
  }

  public void updateUpload(@NonNull Upload upload){
    checkArgument(uploadMap.containsKey(upload.getUploadId()),
        "The upload '%s' does not exist in this batch", upload.getUploadId());
    uploadMap.put(upload.getUploadId(), upload);
  }

  public Upload getUpload(@NonNull String uploadId){
    checkArgument(uploadMap.containsKey(uploadId),
        "The uploadId '%s' does not exist", uploadId);
    return uploadMap.get(uploadId);
  }


  public static BatchUpload createBatchUpload(String studyId, List<String> filePaths){
    checkState(!filePaths.isEmpty(), "There must be atleast one filepath");
    return new BatchUpload(studyId, filePaths);
  }

  public static BatchUpload createBatchUploadFromPathNames(String studyId, List<String> filePathNames){
    val filePaths = filePathNames.stream().map(x -> Paths.get(x)).collect(toImmutableList());
    return createBatchUploadFromPaths(studyId, filePaths);

  }
  public static BatchUpload createBatchUploadFromPaths(String studyId, List<Path> filePaths){
    val badFileNames = filePaths.stream()
        .map(Path::toFile)
        .filter(f -> !f.exists() || !f.isFile())
        .map(File::getName)
        .collect(toImmutableList());

    checkState(badFileNames.size() == 0, "Some files could not be processed: \n%s",
        NEWLINE.join(badFileNames));

    val payloads = filePaths.stream()
        .map(f -> {
          try{
            return toJson(readTree(newInputStream(f)));
          } catch(IOException e){
            throw Lombok.sneakyThrow(e);
          }
        })
        .collect(toImmutableList());

    return createBatchUpload(studyId, payloads);
  }

}
