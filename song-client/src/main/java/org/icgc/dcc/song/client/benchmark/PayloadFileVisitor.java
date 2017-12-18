package org.icgc.dcc.song.client.benchmark;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.icgc.dcc.song.client.benchmark.model.UploadData;

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
