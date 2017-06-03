package org.icgc.dcc.sodalite.server.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.flywaydb.test.annotation.FlywayTest;
import org.flywaydb.test.junit.FlywayTestExecutionListener;
import org.icgc.dcc.sodalite.server.model.entity.Sample;
import org.icgc.dcc.sodalite.server.model.entity.Specimen;
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
public class SpecimenServiceTest {

  @Autowired
  SpecimenService specimenService;
  @Autowired
  SampleService sampleService;

  @Test
  public void testReadSpecimen() {
    val id = "SP1";
    Specimen specimen = specimenService.read(id);
    assertThat(specimen.getSpecimenId()).isEqualTo(id);
    assertThat(specimen.getSpecimenSubmitterId()).isEqualTo("Tissue-Culture 284 Gamma 3");
    assertThat(specimen.getSpecimenClass()).isEqualTo("Tumour");
    assertThat(specimen.getSpecimenType()).isEqualTo("Recurrent tumour - solid tissue");
    assertThat(specimen.getSamples().size()).isEqualTo(2);

    // Verify that we got the same samples as the sample service says we should.
    specimen.getSamples().forEach(sample -> assertThat(sample.equals(getSample(sample.getSampleId()))));
  }

  private Sample getSample(String id) {
    return sampleService.read(id);
  }

  private Specimen createSpecimen(String id, String submitterId, String donorId, String specimenClass, String type) {
    val sp = new Specimen();
    sp.setSpecimenId(id);
    sp.setSpecimenSubmitterId(submitterId);
    sp.setDonorId(donorId);
    sp.setSpecimenClass(specimenClass);
    sp.setSpecimenType(type);
    return sp;

  }

  @Test
  public void testCreateAndDeleteSpecimen() {
    val donorId = "";
    Specimen s = createSpecimen("", "Specimen 101 Ipsilon Prime", donorId, "Tumour", "Cell line - derived from tumour");

    val status = specimenService.create("DO2", s);
    val id = s.getSpecimenId();

    assertThat(id).startsWith("SP");
    assertThat(status).isEqualTo("ok:" + id);

    val check = specimenService.read(id);
    assertThat(s).isEqualToComparingFieldByField(check);

    specimenService.delete(id);
    Specimen check2 = specimenService.read(id);
    assertThat(check2).isNull();
  }

  @Test
  public void testUpdateSpecimen() {
    val donorId = "";
    val s = createSpecimen("", "Specimen 102 Chiron-Beta Prime", donorId, "Tumour",
        "Metastatic tumour - additional metastatic");

    specimenService.create("DO2", s);

    val id = s.getSpecimenId();

    val s2 = createSpecimen(id, "Specimen 102", s.getDonorId(), "Normal", "Normal - other");

    specimenService.update(s2);

    val s3 = specimenService.read(id);
    assertThat(s3).isEqualToComparingFieldByField(s2);
  }

}
