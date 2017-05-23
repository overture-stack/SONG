package org.icgc.dcc.sodalite.server.service;

import java.util.List;

import org.icgc.dcc.sodalite.server.model.File;
import org.icgc.dcc.sodalite.server.repository.FileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.NoArgsConstructor;
import lombok.val;

@Service
@NoArgsConstructor
public class FileService {

  @Autowired
  FileRepository repository;
  @Autowired
  IdService idService;

  public String create(File f) {
    val id = idService.generateFileId();
    f.setObjectId(id);
    val status = repository.save(id, f.getStudyId(), f.getSampleId(), f.getFileName(), f.getFileSize(),
        f.getFileType().toString(), f.getFileMd5(), f.getMetadataDoc());

    if (status != 1) {
      return "error: Can't create" + f.toString();
    }

    return id;
  }

  public void update(File f) {
    repository.update(f.getObjectId(), f.getStudyId(), f.getSampleId(), f.getFileName(), f.getFileSize(),
        f.getFileType().toString(), f.getFileMd5(), f.getMetadataDoc());
  }

  public void delete(String id) {
    repository.delete(id);
  }

  public File getById(String id) {
    return repository.getById(id);
  }

  public void deleteByParentId(String sampleId) {
    repository.deleteBySampleId(sampleId);
  }

  public File findByBusinessKey(String studyId, String submitterFileName) {
    return repository.getByBusinessKey(studyId, submitterFileName);
  }

  public List<File> findByParentId(String sampleId) {
    return repository.findByParentId(sampleId);
  }
}
