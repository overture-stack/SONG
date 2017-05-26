package org.icgc.dcc.sodalite.server.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.flywaydb.test.annotation.FlywayTest;
import org.flywaydb.test.junit.FlywayTestExecutionListener;
import org.icgc.dcc.sodalite.server.model.entity.Donor.DonorGender;
import org.icgc.dcc.sodalite.server.model.entity.File.FileType;
import org.icgc.dcc.sodalite.server.model.entity.Sample.SampleType;
import org.icgc.dcc.sodalite.server.model.entity.Specimen.SpecimenClass;
import org.icgc.dcc.sodalite.server.model.entity.Specimen.SpecimenType;
import org.icgc.dcc.sodalite.server.utils.JsonUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

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

    String json = "{";
    json += JsonUtils.jsonPair("donorSubmitterId", "donor_abc123") + ",";
    json += JsonUtils.jsonPair("donorGender", "female");
    json += "}";

    val donor = JsonUtils.getTree(json);
    assertThat(donor != null);

    // This should create a new record, because there is no entry
    // in the existing test data for "donor_abc123"
    val id = service.saveDonor(studyId, donor);

    val d = donorService.getById(studyId, id);
    assertThat(d.getDonorGender().equals(DonorGender.FEMALE));
    assertThat(d.getDonorSubmitterId().equals("donor_abc123"));
    assertThat(d.getDonorId().equals(id));
  }

  @SneakyThrows
  @Test
  public void testSaveDonor_Update() {
    val studyId = "ABC123";

    String json = "{";
    json += JsonUtils.jsonPair("donorSubmitterId", "Subject-X23Alpha7") + ",";
    json += JsonUtils.jsonPair("donorGender", "female");
    json += "}";

    val donor = JsonUtils.getTree(json);
    assertThat(donor != null);

    // this submitterId and study already exists in our test data,
    // so we should update the existing donor record, not create a new one...
    val id = service.saveDonor(studyId, donor);

    // check that what we saved is what's in the database...
    val d = donorService.getById(studyId, id);
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

    String json = "{";
    json += JsonUtils.jsonPair("specimenSubmitterId", "specimen_abc123") + ",";
    json += JsonUtils.jsonPair("specimenClass", "Normal") + ",";
    json += JsonUtils.jsonPair("specimenType", "Normal - solid tissue");
    json += "}";

    val specimen = JsonUtils.getTree(json);
    assertThat(specimen != null);

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

    String json = "{";
    json += JsonUtils.jsonPair("specimenSubmitterId", "Tissue-Culture 284 Gamma 3") + ",";
    json += JsonUtils.jsonPair("specimenClass", "Tumour") + ",";
    json += JsonUtils.jsonPair("specimenType", "Recurrent tumour - other");
    json += "}";

    val specimen = JsonUtils.getTree(json);
    assertThat(specimen != null);

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

    String json = "{";
    json += JsonUtils.jsonPair("sampleSubmitterId", "sample_abc123") + ",";
    json += JsonUtils.jsonPair("sampleType", "Total RNA");
    json += "}";

    val sample = JsonUtils.getTree(json);
    assertThat(sample != null);

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

    String json = "{";
    json += JsonUtils.jsonPair("sampleSubmitterId", "T285-G7-B9") + ",";
    json += JsonUtils.jsonPair("sampleType", "Total RNA");
    json += "}";

    val sample = JsonUtils.getTree(json);
    assertThat(sample != null);

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
    String json = "{";
    json += JsonUtils.jsonPair("fileName", name) + ",";
    json += JsonUtils.jsonPair("fileSize", 12345L) + ",";
    json += JsonUtils.jsonPair("fileMd5", md5) + ",";
    json += JsonUtils.jsonPair("fileType", "IDX");
    json += "}";

    val file = JsonUtils.getTree(json);
    assertThat(file != null);

    // This should create a new record
    val sampleId = "SA1";

    val id = service.saveFile(studyId, sampleId, file);
    val f = fileService.getById(id);
    assertThat(f.getObjectId().equals(id));
    assertThat(f.getFileName().equals(name));
    assertThat(f.getFileSize() == 12345L);
    assertThat(f.getFileMd5().equals(md5));
    assertThat(f.getFileType().equals(FileType.IDX));
  }

  @SneakyThrows
  @Test
  public void testSaveFile_Update() {
    val studyId = "ABC123";

    val name = "ABC-TC285-G7-B9-kthx12345.bai";
    val md5 = "mmmmdddd5555";
    String json = "{";
    json += JsonUtils.jsonPair("fileName", name) + ",";
    json += JsonUtils.jsonPair("fileSize", 12345L) + ",";
    json += JsonUtils.jsonPair("fileMd5", md5) + ",";
    json += JsonUtils.jsonPair("fileType", "IDX");
    json += "}";

    val file = JsonUtils.getTree(json);
    assertThat(file != null);

    // This should update the row with fileId "FI3".
    val sampleId = "SA11";

    val id = service.saveFile(studyId, sampleId, file);
    val f = fileService.getById(id);
    assertThat(id.equals("FI3"));
    assertThat(f.getObjectId().equals(id));
    assertThat(f.getFileName().equals(name));
    assertThat(f.getFileSize() == 12345L);
    assertThat(f.getFileMd5().equals(md5));
    assertThat(f.getFileType().equals(FileType.IDX));
  }

}
