package org.icgc.dcc.sodalite.server.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;

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
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class, FlywayTestExecutionListener.class })
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
    Specimen specimen = specimenService.getById(id);
    assertThat(specimen.getSpecimenId()).isEqualTo(id);
    assertThat(specimen.getSpecimenSubmitterId()).isEqualTo("Tissue-Culture 284 Gamma 3");
    assertThat(specimen.getSpecimenClass()).isEqualTo(Specimen.SpecimenClass.TUMOUR);
    assertThat(specimen.getSpecimenType()).isEqualTo(Specimen.SpecimenType.RECURRENT_TUMOUR_SOLID_TISSUE);
    assertThat(specimen.getSamples().size()).isEqualTo(2);

    // Verify that we got the same samples as the sample service says we should.
    specimen.getSamples().forEach(sample -> assertThat(sample.equals(getSample(sample.getSampleId()))));
  }

  private Sample getSample(String id) {
    return sampleService.getById(id);
  }

  @Test
  public void testCreateAndDeleteSpecimen() {
    Specimen s = new Specimen()
        .withSpecimenSubmitterId("Specimen 101 Ipsilon Prime")
        .withSpecimenType(Specimen.SpecimenType.CELL_LINE_DERIVED_FROM_TUMOUR)
        .withSpecimenClass(Specimen.SpecimenClass.TUMOUR)
        .withSamples(new ArrayList<Sample>());

    val status = specimenService.create("DO2", s);
    val id = s.getSpecimenId();

    assertThat(id).startsWith("SP");
    assertThat(status).isEqualTo("ok:" + id);

    val check = specimenService.getById(id);
    assertThat(s).isEqualToComparingFieldByField(check);

    specimenService.delete(id);
    Specimen check2 = specimenService.getById(id);
    assertThat(check2).isNull();
  }

  @Test
  public void testUpdateSpecimen() {
    val s = new Specimen()
        .withSpecimenSubmitterId("Specimen 102 Chiron-Beta Prime")
        .withSpecimenType(Specimen.SpecimenType.METASTATIC_TUMOUR_ADDITIONAL_METASTATIC)
        .withSpecimenClass(Specimen.SpecimenClass.TUMOUR)
        .withSamples(new ArrayList<Sample>());

    specimenService.create("DO2", s);

    val id = s.getSpecimenId();

    val s2 = new Specimen()
        .withSpecimenId(id)
        .withSpecimenSubmitterId("Specimen 102")
        .withSpecimenType(Specimen.SpecimenType.NORMAL_OTHER)
        .withSpecimenClass(Specimen.SpecimenClass.NORMAL)
        .withSamples(new ArrayList<Sample>());

    specimenService.update(s2);

    val s3 = specimenService.getById(id);
    assertThat(s3).isEqualToComparingFieldByField(s2);
  }

}
