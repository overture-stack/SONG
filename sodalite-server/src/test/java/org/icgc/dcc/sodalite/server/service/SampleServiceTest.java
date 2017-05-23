package org.icgc.dcc.sodalite.server.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;

import org.flywaydb.test.annotation.FlywayTest;
import org.flywaydb.test.junit.FlywayTestExecutionListener;
import org.icgc.dcc.sodalite.server.model.File;
import org.icgc.dcc.sodalite.server.model.Sample;
import org.icgc.dcc.sodalite.server.model.SampleType;
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
public class SampleServiceTest {

  @Autowired
  SampleService sampleService;
  @Autowired
  FileService fileService;

  @Test
  public void testReadSample() {
    val id = "SA1";
    val sample = sampleService.getById(id);
    assertThat(sample.getSampleId()).isEqualTo(id);
    assertThat(sample.getSampleSubmitterId()).isEqualTo("T285-G7-A5");
    assertThat(sample.getSampleType()).isEqualTo(SampleType.DNA);
  }

  private File getFile(String id) {
    return fileService.getById(id);
  }

  @Test
  public void testCreateAndDeleteSample() {
    val s = new Sample()
        .withStudyId("ABC123")
        .withSampleId("SP2")
        .withSampleSubmitterId("101-IP-A")
        .withSampleType(SampleType.AMPLIFIED_DNA)
        .withFiles(new ArrayList<File>());

    val status = sampleService.create(s);
    val id = s.getSampleId();

    assertThat(id).startsWith("SA");
    assertThat(status).isEqualTo(id);

    Sample check = sampleService.getById(id);
    assertThat(s).isEqualToComparingFieldByField(check);

    sampleService.delete(id);
    Sample check2 = sampleService.getById(id);
    assertThat(check2).isNull();
  }

  @Test
  public void testUpdateSample() {
    val s = new Sample()
        .withStudyId("ABC123")
        .withSpecimenId("SP2")
        .withSampleSubmitterId("102-CBP-A")
        .withSampleType(SampleType.RNA)
        .withFiles(new ArrayList<File>());

    sampleService.create(s);

    val id = s.getSampleId();

    val s2 = new Sample()
        .withSampleId(id)
        .withStudyId("ABC123")
        .withSpecimenId("SP2")
        .withSampleSubmitterId("Sample 102")
        .withSampleType(SampleType.FFPE_DNA)
        .withFiles(new ArrayList<File>());

    sampleService.update(s2);

    val s3 = sampleService.getById(id);
    assertThat(s3).isEqualToComparingFieldByField(s2);
  }

}
