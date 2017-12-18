package org.icgc.dcc.song.client.benchmark.model;

import lombok.Builder;
import lombok.Data;

import java.nio.file.Path;
import java.util.List;

@Data
@Builder
public class UploadData {

  private String studyId;
  private Path file;
  private String uploadId;
  private String analysisId;
  private String submittedAnalysisId;
  private String uploadState;
  private List<String> uploadErrors;
  private long fileSize = -1;

}
