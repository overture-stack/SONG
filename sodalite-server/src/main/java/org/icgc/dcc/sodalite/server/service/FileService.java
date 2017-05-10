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

  public String create(String parentId, File f) {
    val id = idService.generateFileId();
    f.setObjectId(id);
    int status = repository.save(id, parentId, f.getFileName(), f.getFileSize(), f.getFileType().toString());

    if (status != 1) {
      return "error: Can't create" + f.toString();
    }

    return "ok:" + id;
  }

  public String update(File f) {
    repository.set(f.getObjectId(), f.getFileName(), f.getFileSize(), f.getFileType().toString());
    return "ok";
  }

  public String delete(String id) {
    repository.delete(id);
    return "ok";
  }

  public File getById(String id) {
    return repository.getById(id);
  }

  public String deleteByParentId(String parentId) {
    repository.deleteBySampleId(parentId);
    return "ok";
  }

  public List<File> findByParentId(String parentId) {
    return repository.findByParentId(parentId);
  }
}
