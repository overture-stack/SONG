package org.icgc.dcc.sodalite.server.service;

import static org.icgc.dcc.sodalite.server.model.enums.IdPrefix.File;

import java.util.List;

import org.icgc.dcc.sodalite.server.model.entity.File;
import org.icgc.dcc.sodalite.server.model.enums.IdPrefix;
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
    val id = idService.generate(File);
    f.setId(id);
    f.setSampleId(parentId);

    int status = repository.create(f);

    if (status != 1) {
      return "error: Can't create" + f.toString();
    }

    return "ok:" + id;
  }

  public String update(File f) {
    repository.update(f);
    return "ok";
  }

  public String delete(String id) {
    repository.delete(id);
    return "ok";
  }

  public String save(String studyId, File f) {
    String fileId = repository.findByBusinessKey(studyId, f.getName());
    if (fileId == null) {
      fileId = idService.generate(IdPrefix.File);
      f.setId(fileId);
      repository.create(f);
    } else {
      repository.update(f);
    }
    return fileId;
  }

  public File getById(String id) {
    return repository.read(id);
  }

  public List<File> findByParentId(String parentId) {
    return repository.readByParentId(parentId);
  }
}
