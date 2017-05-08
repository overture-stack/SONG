package org.icgc.dcc.sodalite.server.service;

import org.icgc.dcc.sodalite.server.model.File;
import org.icgc.dcc.sodalite.server.model.FileType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import lombok.val;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@RunWith(SpringRunner.class)
public class FileServiceTest {

  @Autowired
  FileService fileService;

  @Test
  public void testReadFile() {
    String id = "FI1";
    File file = fileService.getById(id);
    assertThat(file.getObjectId()).isEqualTo(id);
    assertThat(file.getFileName()).isEqualTo("ABC-TC285G7-A5-ae3458712345.bam");
    assertThat(file.getFileType()).isEqualTo(FileType.BAM);
    assertThat(file.getFileSize()).isEqualTo(122333444455555L);
  }

  @Test
  public void testCreateAndDeleteFile() {
    File f = new File()
        .withFileName("ABC-TC285G87-A5-sqrl.bai")
        .withFileSize(0)
        .withFileType(FileType.FAI);

    String status = fileService.create("SA1", f);
    val id = f.getObjectId();

    assertThat(id).startsWith("FI");
    assertThat(status).isEqualTo("ok:" + id);

    File check = fileService.getById(id);
    assertThat(f).isEqualToComparingFieldByField(check);

    fileService.delete(id);
    File check2 = fileService.getById(id);
    assertThat(check2).isNull();
  }

  @Test
  public void testUpdateFile() {
    File s = new File()
        .withFileName("file123.fasta")
        .withFileType(FileType.FASTA)
        .withFileSize(12345);

    fileService.create("SA11", s);

    val id = s.getObjectId();

    File s2 = new File()
        .withObjectId(id)
        .withFileName("File 102.fai")
        .withFileType(FileType.FAI)
        .withFileSize(123456789);

    fileService.update(s2);

    File s3 = fileService.getById(id);
    assertThat(s3).isEqualToComparingFieldByField(s2);
  }

}
