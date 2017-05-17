package org.icgc.dcc.sodalite.server.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.flywaydb.test.annotation.FlywayTest;
import org.flywaydb.test.junit.FlywayTestExecutionListener;
import org.icgc.dcc.sodalite.server.model.File;
import org.icgc.dcc.sodalite.server.model.FileType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import lombok.val;

@SpringBootTest
@RunWith(SpringRunner.class)
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class, FlywayTestExecutionListener.class })
@FlywayTest
@ActiveProfiles("dev")
public class FileServiceTest {

  @Autowired
  FileService fileService;

  @Test
  public void testReadFile() {
    val id = "FI1";
    val file = fileService.getById(id);
    assertThat(file.getObjectId()).isEqualTo(id);
    assertThat(file.getFileName()).isEqualTo("ABC-TC285G7-A5-ae3458712345.bam");
    assertThat(file.getFileType()).isEqualTo(FileType.BAM);
    assertThat(file.getFileSize()).isEqualTo(122333444455555L);
  }

  @Test
  public void testCreateAndDeleteFile() {
    val f = new File()
        .withStudyId("ABC123")
        .withSampleId("SA1")
        .withFileName("ABC-TC285G87-A5-sqrl.bai")
        .withFileSize(0)
        .withFileType(FileType.FAI);

    val status = fileService.create(f);
    val id = f.getObjectId();

    assertThat(status).isEqualTo(id);

    File check = fileService.getById(id);
    assertThat(f).isEqualToComparingFieldByField(check);

    fileService.delete(id);
    val check2 = fileService.getById(id);
    assertThat(check2).isNull();
  }

  @Test
  public void testUpdateFile() {
    val s = new File()
        .withStudyId("ABC123")
        .withSampleId("SA11")
        .withFileName("file123.fasta")
        .withFileType(FileType.FASTA)
        .withFileSize(12345);

    fileService.create(s);

    val id = s.getObjectId();

    val s2 = new File()
        .withStudyId("ABC123")
        .withSampleId("SA11")
        .withObjectId(id)
        .withFileName("File 102.fai")
        .withFileType(FileType.FAI)
        .withFileSize(123456789);

    fileService.update(s2);

    val s3 = fileService.getById(id);
    assertThat(s3).isEqualToComparingFieldByField(s2);
  }

}
