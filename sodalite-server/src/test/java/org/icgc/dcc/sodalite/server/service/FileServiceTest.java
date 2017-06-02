package org.icgc.dcc.sodalite.server.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.flywaydb.test.annotation.FlywayTest;
import org.flywaydb.test.junit.FlywayTestExecutionListener;
import org.icgc.dcc.sodalite.server.model.entity.File;
import org.icgc.dcc.sodalite.server.model.enums.FileType;
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
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, FlywayTestExecutionListener.class })
@FlywayTest
@ActiveProfiles("dev")
public class FileServiceTest {

  @Autowired
  FileService fileService;

  @Test
  public void testReadFile() {
    val id = "FI1";
    val file = fileService.getById(id);
    assertThat(file.getId()).isEqualTo(id);
    assertThat(file.getName()).isEqualTo("ABC-TC285G7-A5-ae3458712345.bam");
    assertThat(file.getType()).isEqualTo(FileType.BAM.value());
    assertThat(file.getFileSize()).isEqualTo(122333444455555L);
    assertThat(file.getMd5()).isEqualTo("20de2982390c60e33452bf8736c3a9f1");
  }

  @Test
  public void testCreateAndDeleteFile() {
    val sampleId = "";
    val f = new File();
    f.setId("");
    f.setName("ABC-TC285G87-A5-sqrl.bai");

    f.setSampleId(sampleId);

    f.setFileSize(0L);
    f.setType(FileType.FAI.value());
    f.setMd5("md5abcdefg");

    val status = fileService.create("SA1", f);
    val id = f.getId();

    assertThat(id).startsWith("FI");
    assertThat(status).isEqualTo("ok:" + id);

    File check = fileService.getById(id);
    assertThat(f).isEqualToComparingFieldByField(check);

    fileService.delete(id);
    val check2 = fileService.getById(id);
    assertThat(check2).isNull();
  }

  @Test
  public void testUpdateFile() {
    val sampleId = "";
    val s = new File();
    s.setId("");
    s.setName("file123.fasta");
    s.setSampleId(sampleId);
    s.setFileSize(12345L);
    s.setType(FileType.FASTA.value());
    s.setMd5("md5sumaebcefghadwa");

    fileService.create("SA11", s);

    val id = s.getId();

    val s2 = new File();
    s2.setId(id);
    s2.setName("File 102.fai");
    s2.setSampleId(s.getSampleId());
    s2.setFileSize(1234356789L);
    s2.setType(FileType.FAI.value());
    s2.setMd5("md5magical");

    fileService.update(s2);

    val s3 = fileService.getById(id);
    assertThat(s3).isEqualToComparingFieldByField(s2);
  }

}
