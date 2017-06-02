package org.icgc.dcc.sodalite.server.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.flywaydb.test.annotation.FlywayTest;
import org.flywaydb.test.junit.FlywayTestExecutionListener;
import org.icgc.dcc.sodalite.server.model.entity.File;
import org.icgc.dcc.sodalite.server.model.enums.FileType;
import org.icgc.dcc.sodalite.server.utils.JsonUtils;
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
    val name = "ABC-TC285G7-A5-ae3458712345.bam";
    val sample = "SA1";
    val type = FileType.BAM.value();
    val size = 122333444455555L;
    val md5 = "20de2982390c60e33452bf8736c3a9f1";
    val metadata = JsonUtils.fromSingleQuoted("{'metadata':'<XML>Not even well-formed <XML></XML>'}");
    val file = fileService.read(id);

    val expected = File.create(id, name, sample, size, type, md5, metadata);
    assertThat(file).isEqualToComparingFieldByField(expected);
  }

  @Test
  public void testCreateAndDeleteFile() {
    val sampleId = "";
    val f = new File();
    f.setObjectId("");
    f.setFileName("ABC-TC285G87-A5-sqrl.bai");

    f.setSampleId(sampleId);

    f.setFileSize(0L);
    f.setFileType(FileType.FAI.value());
    f.setFileMd5("md5abcdefg");

    val status = fileService.create("SA1", f);
    val id = f.getObjectId();

    assertThat(id).startsWith("FI");
    assertThat(status).isEqualTo("ok:" + id);

    File check = fileService.read(id);
    assertThat(f).isEqualToComparingFieldByField(check);

    fileService.delete(id);
    val check2 = fileService.read(id);
    assertThat(check2).isNull();
  }

  @Test
  public void testUpdateFile() {

    val id = "";
    val name = "file123.fasta";
    val sampleId = "";
    val size = 12345L;
    val type = FileType.FASTA.value();
    val md5 = "md5sumaebcefghadwa";
    val metadata = "";

    val s = File.create(id, name, sampleId, size, type, md5, metadata);

    fileService.create("SA11", s);
    val id2 = s.getObjectId();

    val s2 = File.create(id2, "File 102.fai", s.getSampleId(), 123456789L, FileType.FAI.value(), "md5magical", "");
    fileService.update(s2);

    val s3 = fileService.read(id2);
    assertThat(s3).isEqualToComparingFieldByField(s2);
  }

}
