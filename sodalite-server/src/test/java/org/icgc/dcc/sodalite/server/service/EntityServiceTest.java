package org.icgc.dcc.sodalite.server.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.flywaydb.test.annotation.FlywayTest;
import org.flywaydb.test.junit.FlywayTestExecutionListener;
import org.icgc.dcc.sodalite.server.model.enums.DonorGender;
import org.icgc.dcc.sodalite.server.model.enums.FileType;
import org.icgc.dcc.sodalite.server.model.enums.SampleType;
import org.icgc.dcc.sodalite.server.model.enums.SpecimenClass;
import org.icgc.dcc.sodalite.server.model.enums.SpecimenType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;

import lombok.SneakyThrows;
import lombok.val;

@SpringBootTest
@RunWith(SpringRunner.class)
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, FlywayTestExecutionListener.class })
@FlywayTest
@ActiveProfiles("dev")
public class EntityServiceTest {

  // class under test
  @Autowired
  EntityService service;

  // trusted services used to verify class under test
  @Autowired
  DonorService donorService;
  @Autowired
  SpecimenService specimenService;
  @Autowired
  SampleService sampleService;
  @Autowired
  FileService fileService;

  @SneakyThrows
  @Test
  public void testSaveDonor_Create() {
    val studyId = "ABC123";
    val donor =
        JsonNodeFactory.instance.objectNode().put("donorSubmitterId", "donor_abc123").put("donorGender", "female");

    // This should create a new record, because there is no entry
    // in the existing test data for "donor_abc123"
    val id = service.saveDonor(studyId, donor);

    val d = donorService.read(id);
    assertThat(d.getDonorGender().equals(DonorGender.FEMALE));
    assertThat(d.getDonorSubmitterId().equals("donor_abc123"));
    assertThat(d.getDonorId().equals(id));
  }

  @SneakyThrows
  @Test
  public void testSaveDonor_Update() {
    val studyId = "ABC123";

    val donor =
        JsonNodeFactory.instance.objectNode().put("donorSubmitterId", "Subject-X23Alpha7").put("donorGender", "female");

    // this submitterId and study already exists in our test data,
    // so we should update the existing donor record, not create a new one...
    val id = service.saveDonor(studyId, donor);

    // check that what we saved is what's in the database...
    val d = donorService.read(id);
    assertThat(d.getDonorGender().equals(DonorGender.FEMALE));
    assertThat(d.getDonorSubmitterId().equals("Subject-X23Alpha7"));
    assertThat(d.getDonorId().equals(id));

    // make sure we're updating the donorID of the existing record in our test data ("DO1")
    assertThat(id.equals("DO1"));
  }

  @SneakyThrows
  @Test
  public void testSaveSpecimen_Create() {
    val studyId = "ABC123";

    val specimen = JsonNodeFactory.instance.objectNode().put("specimenSubmitterId", "specimen_abc123")
        .put("specimenClass", "Normal").put("specimenType", "Normal - solid tissue");

    // This should create a new record
    val donorId = "DO1";

    val id = service.saveSpecimen(studyId, donorId, specimen);
    val sp = specimenService.getById(id);
    assertThat(sp.getSpecimenId().equals(id));
    assertThat(sp.getSpecimenClass().equals(SpecimenClass.NORMAL));
    assertThat(sp.getSpecimenType().equals(SpecimenType.NORMAL_SOLID_TISSUE));
  }

  @SneakyThrows
  @Test
  public void testSaveSpecimen_Update() {
    val studyId = "ABC123";

    val specimen = JsonNodeFactory.instance.objectNode().put("specimenSubmitterId", "Tissue-Culture 284 Gamma 3")
        .put("specimenClass", "Tumour").put("specimenType", "Recurrent tumour - other");

    // This should update the record for specimen "SP1"
    val donorId = "DO1";

    val id = service.saveSpecimen(studyId, donorId, specimen);
    val sp = specimenService.getById(id);
    assertThat(id.equals("SP1"));
    assertThat(sp.getSpecimenId().equals(id));
    assertThat(sp.getSpecimenSubmitterId().equals("Tissue-Culture 284 Gamma 3"));
    assertThat(sp.getSpecimenClass().equals(SpecimenClass.TUMOUR));
    assertThat(sp.getSpecimenType().equals(SpecimenType.RECURRENT_TUMOUR_OTHER));
  }

  @SneakyThrows
  @Test
  public void testSaveSample_Create() {
    val studyId = "ABC123";

    val sample =
        JsonNodeFactory.instance.objectNode().put("sampleSubmitterId", "sample_abc123").put("sampleType", "Total RNA");

    // This should create a new record
    val specimenId = "SP1";

    val id = service.saveSample(studyId, specimenId, sample);
    val sa = sampleService.getById(id);
    assertThat(sa.getSampleId().equals(id));
    assertThat(sa.getSampleType().equals(SampleType.TOTAL_RNA));
    assertThat(sa.getSampleSubmitterId().equals("sample_abc123"));
  }

  @SneakyThrows
  @Test
  public void testSaveSample_Update() {
    val studyId = "ABC123";

    val sample =
        JsonNodeFactory.instance.objectNode().put("sampleSubmitterId", "T285-G7-B9").put("sampleType", "Total RNA");

    // This should update sample "SA11"
    val specimenId = "SP1";

    val id = service.saveSample(studyId, specimenId, sample);
    val sa = sampleService.getById(id);
    assertThat(id.equals("SA11"));
    assertThat(sa.getSampleId().equals(id));
    assertThat(sa.getSampleType().equals(SampleType.TOTAL_RNA));
    assertThat(sa.getSampleSubmitterId().equals("sample_abc123"));
  }

  @SneakyThrows
  @Test
  public void testSaveFile_Create() {
    val studyId = "ABC123";

    val name = "file_abc123.idx.gz";
    val md5 = "mmmmdddd5555";
    val file = JsonNodeFactory.instance.objectNode().put("fileName", name).put("fileSize", 12345L).put("fileMd5", md5)
        .put("type", "IDX");

    // This should create a new record
    val sampleId = "SA1";

    val id = service.saveFile(studyId, sampleId, file);
    val f = fileService.getById(id);
    assertThat(f.getId().equals(id));
    assertThat(f.getName().equals(name));
    assertThat(f.getFileSize() == 12345L);
    assertThat(f.getMd5().equals(md5));
    assertThat(f.getType().equals(FileType.IDX));
  }

  @SneakyThrows
  @Test
  public void testSaveFile_Update() {
    val studyId = "ABC123";

    val name = "ABC-TC285-G7-B9-kthx12345.bai";
    val md5 = "mmmmdddd5555";
    val file = JsonNodeFactory.instance.objectNode().put("fileName", name).put("fileSize", 12345L).put("fileMd5", md5)
        .put("fileType", "IDX");

    // This should update the row with fileId "FI3".
    val sampleId = "SA11";

    val id = service.saveFile(studyId, sampleId, file);
    val f = fileService.getById(id);
    assertThat(id.equals("FI3"));
    assertThat(f.getId().equals(id));
    assertThat(f.getName().equals(name));
    assertThat(f.getFileSize() == 12345L);
    assertThat(f.getMd5().equals(md5));
    assertThat(f.getType().equals(FileType.IDX));
  }

}
