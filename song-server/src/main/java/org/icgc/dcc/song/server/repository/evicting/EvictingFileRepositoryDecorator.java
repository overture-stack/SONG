package org.icgc.dcc.song.server.repository.evicting;

import lombok.NonNull;
import org.icgc.dcc.song.server.model.entity.file.File;
import org.icgc.dcc.song.server.repository.FileRepository;
import org.icgc.dcc.song.server.utils.EvictingRepositoryDecorator;
import org.icgc.dcc.song.server.utils.Evictor;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.List;

@NoRepositoryBean
public class EvictingFileRepositoryDecorator extends EvictingRepositoryDecorator<File, String> implements
    FileRepository {

  private final Evictor evictor;
  private final FileRepository fileRepository;

  public EvictingFileRepositoryDecorator(
      @NonNull Evictor evictor,
     @NonNull FileRepository fileRepository) {
    super(evictor, fileRepository);
    this.fileRepository = fileRepository;
    this.evictor = evictor;
  }

  @Override public List<File> findAllByAnalysisIdAndFileName(String analysisId, String fileName) {
    return evictor.evictList(fileRepository.findAllByAnalysisIdAndFileName(analysisId, fileName));
  }

  @Override public List<File> findAllByAnalysisId(String analysisId) {
    return evictor.evictList(fileRepository.findAllByAnalysisId(analysisId));
  }

  @Override public void deleteAllByAnalysisId(String analysisId) {
    fileRepository.deleteAllByAnalysisId(analysisId);
  }

  @Override public long countAllByStudyIdAndObjectId(String studyId, String objectId) {
    return fileRepository.countAllByStudyIdAndObjectId(studyId, objectId);
  }

}
