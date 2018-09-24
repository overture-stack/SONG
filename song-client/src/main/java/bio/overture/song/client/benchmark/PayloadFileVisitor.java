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

package bio.overture.song.client.benchmark;

import bio.overture.song.client.benchmark.model.UploadData;
import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.Set;

import static java.util.Objects.isNull;
import static org.icgc.dcc.common.core.util.stream.Collectors.toImmutableList;
import static org.icgc.dcc.common.core.util.stream.Collectors.toImmutableSet;

@RequiredArgsConstructor
public class PayloadFileVisitor extends SimpleFileVisitor<Path> {

  @NonNull private final Path rootDir;
  @NonNull private final Set<String> excludeStudies;
  @NonNull private final Set<String> includeStudies;


  /**
   * State
   */
  private String studyId;
  @Getter private List<UploadData> datas = Lists.newArrayList();

  @Override public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
    if (dir.equals(rootDir)) {
      return FileVisitResult.CONTINUE;
    }
    studyId = dir.toAbsolutePath().getFileName().toString();
    return resolveVisitResult(studyId);
  }

  private FileVisitResult resolveVisitResult(String studyId){
    if (excludeStudies.isEmpty() || !excludeStudies.contains(studyId)) {
      if (includeStudies.isEmpty() || includeStudies.contains(studyId)) {
        return FileVisitResult.CONTINUE;
      }
    }
    return FileVisitResult.SKIP_SUBTREE;
  }

  @Override public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {

    if (!isNull(studyId)) {
      if (file.getFileName().toString().endsWith(".json")) {
        val uploadFile = UploadData.builder()
            .file(file)
            .studyId(studyId)
            .build();
        datas.add(uploadFile);
      }
    }
    return FileVisitResult.CONTINUE;
  }

  public List<UploadData> getDataForStudy(@NonNull String studyId) {
    return datas.stream()
        .filter(x -> x.getStudyId().equals(studyId))
        .collect(toImmutableList());
  }

  public Set<String> getStudies(){
    return datas.stream().map(UploadData::getStudyId).collect(toImmutableSet());
  }

}
