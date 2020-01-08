/*
 * Copyright (c) 2019. Ontario Institute for Cancer Research
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package bio.overture.song.server.service;

import static bio.overture.song.core.exceptions.ServerErrors.SAMPLE_ALREADY_EXISTS;
import static bio.overture.song.core.exceptions.ServerErrors.SAMPLE_DOES_NOT_EXIST;
import static bio.overture.song.core.exceptions.ServerErrors.SAMPLE_ID_IS_CORRUPTED;
import static bio.overture.song.core.exceptions.ServerErrors.STUDY_ID_DOES_NOT_EXIST;
import static bio.overture.song.core.utils.RandomGenerator.createRandomGenerator;
import static bio.overture.song.server.utils.TestConstants.DEFAULT_DONOR_ID;
import static bio.overture.song.server.utils.TestConstants.DEFAULT_SAMPLE_ID;
import static bio.overture.song.server.utils.TestConstants.DEFAULT_SPECIMEN_ID;
import static bio.overture.song.server.utils.TestConstants.DEFAULT_STUDY_ID;
import static bio.overture.song.server.utils.TestConstants.SPECIMEN_TISSUE_SOURCE;
import static bio.overture.song.server.utils.TestConstants.SPECIMEN_TYPE;
import static bio.overture.song.server.utils.TestConstants.TUMOUR_NORMAL_DESIGNATION;
import static bio.overture.song.server.utils.TestFiles.getInfoName;
import static bio.overture.song.server.utils.securestudy.impl.SecureSampleTester.createSecureSampleTester;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.stream.Collectors.toSet;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import bio.overture.song.core.testing.SongErrorAssertions;
import bio.overture.song.core.utils.JsonUtils;
import bio.overture.song.core.utils.RandomGenerator;
import bio.overture.song.server.model.entity.Sample;
import bio.overture.song.server.model.entity.Specimen;
import bio.overture.song.server.utils.TestConstants;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import javax.transaction.Transactional;
import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
public class SampleServiceTest {

  @Autowired SampleService sampleService;
  @Autowired StudyService studyService;
  @Autowired SpecimenService specimenService;

  private final RandomGenerator randomGenerator =
      createRandomGenerator(SampleServiceTest.class.getSimpleName());

  @Before
  public void beforeTest() {
    assertTrue(studyService.isStudyExist(DEFAULT_STUDY_ID));
    assertTrue(specimenService.isSpecimenExist(DEFAULT_SPECIMEN_ID));
  }

  @Test
  public void testReadSample() {
    val id = "SA1";
    val sample = sampleService.securedRead(DEFAULT_STUDY_ID, id);
    assertEquals(sample.getSampleId(), id);
    assertEquals(sample.getSubmitterSampleId(), "T285-G7-A5");
    assertEquals(sample.getSampleType(), "Total DNA");
    assertEquals(getInfoName(sample), "sample1");
  }

  @Test
  public void testCreateAndDeleteSample() {
    val specimenId = "SP2";
    val metadata = JsonUtils.fromSingleQuoted("{'ageCategory': 3, 'species': 'human'}");
    val s =
        Sample.builder()
            .sampleId("")
            .submitterSampleId("101-IP-A")
            .specimenId(specimenId)
            .sampleType("Amplified DNA")
            .build();
    s.setInfo(metadata);

    val status = sampleService.create(DEFAULT_STUDY_ID, s);
    val id = s.getSampleId();
    assertTrue(sampleService.isSampleExist(id));

    assertEquals(status, id);

    Sample check = sampleService.securedRead(DEFAULT_STUDY_ID, id);
    assertEquals(check, s);

    sampleService.securedDelete(DEFAULT_STUDY_ID, newArrayList(id));
    assertFalse(sampleService.isSampleExist(id));
  }

  @Test
  public void testUpdateSample() {

    val specimenId = "SP2";
    val s =
        Sample.builder()
            .sampleId("")
            .specimenId(specimenId)
            .submitterSampleId("102-CBP-A")
            .sampleType("Total RNA")
            .build();

    sampleService.create(DEFAULT_STUDY_ID, s);

    val id = s.getSampleId();

    val metadata = JsonUtils.fromSingleQuoted("{'species': 'Canadian Beaver'}");
    val s2 =
        Sample.builder()
            .sampleId(id)
            .submitterSampleId("Sample 102")
            .specimenId(s.getSpecimenId())
            .sampleType("Total RNA")
            .build();
    s2.setInfo(metadata);
    sampleService.update(s2);

    val s3 = sampleService.securedRead(DEFAULT_STUDY_ID, id);
    assertEquals(s3, s2);
  }

  @Test
  public void testCreateStudyDNE() {
    val randomStudyId = randomGenerator.generateRandomUUIDAsString();
    val sample = new Sample();
    SongErrorAssertions.assertSongError(
        () -> sampleService.create(randomStudyId, sample), STUDY_ID_DOES_NOT_EXIST);
  }

  @Test
  public void testCreateCorruptedAndAlreadyExists() {
    val specimenId = DEFAULT_SPECIMEN_ID;
    val existingStudyId = DEFAULT_STUDY_ID;

    val sample = new Sample();
    sample.setSubmitterSampleId(randomGenerator.generateRandomUUIDAsString());
    sample.setSampleType(
        randomGenerator.randomElement(Lists.newArrayList(TestConstants.SAMPLE_TYPE)));
    sample.setSpecimenId(specimenId);

    // Create a sample
    val sampleId = sampleService.create(existingStudyId, sample);
    assertTrue(sampleService.isSampleExist(sampleId));

    // Try to create the sample again, and assert that the right exception is thrown
    SongErrorAssertions.assertSongError(
        () -> sampleService.create(existingStudyId, sample), SAMPLE_ALREADY_EXISTS);

    // 'Accidentally' set the sampleId to something not generated by the idService, and try to
    // create. Should
    // detected the corrupted id field, indicating user might have accidentally set the id, thinking
    // it would be
    // persisted
    val sample2 = new Sample();
    sample2.setSpecimenId(specimenId);
    sample2.setSampleType(
        randomGenerator.randomElement(Lists.newArrayList(TestConstants.SAMPLE_TYPE)));
    sample2.setSubmitterSampleId(randomGenerator.generateRandomUUIDAsString());
    sample2.setSampleId(randomGenerator.generateRandomUUIDAsString());
    assertFalse(sampleService.isSampleExist(sample2.getSampleId()));
    SongErrorAssertions.assertSongError(
        () -> sampleService.create(existingStudyId, sample2), SAMPLE_ID_IS_CORRUPTED);
  }

  @Test
  public void testSampleExists() {
    val existingSampleId = DEFAULT_SAMPLE_ID;
    assertTrue(sampleService.isSampleExist(existingSampleId));
    sampleService.checkSampleExists(existingSampleId);
    val nonExistingSampleId = randomGenerator.generateRandomUUIDAsString();
    assertFalse(sampleService.isSampleExist(nonExistingSampleId));
    sampleService.checkSampleExists(existingSampleId);
    sampleService.checkSampleDoesNotExist(nonExistingSampleId);

    SongErrorAssertions.assertSongErrorRunnable(
        () -> sampleService.checkSampleExists(nonExistingSampleId), SAMPLE_DOES_NOT_EXIST);
    SongErrorAssertions.assertSongErrorRunnable(
        () -> sampleService.checkSampleDoesNotExist(existingSampleId), SAMPLE_ALREADY_EXISTS);
  }

  @Test
  public void testReadSampleDNE() {
    val randomSampleId = randomGenerator.generateRandomUUIDAsString();
    assertFalse(sampleService.isSampleExist(randomSampleId));
    SongErrorAssertions.assertSongError(
        () -> sampleService.unsecuredRead(randomSampleId), SAMPLE_DOES_NOT_EXIST);
  }

  @Test
  public void testReadAndDeleteByParentId() {
    val studyId = DEFAULT_STUDY_ID;
    val donorId = DEFAULT_DONOR_ID;
    val specimen = new Specimen();
    specimen.setDonorId(donorId);
    specimen.setSpecimenTissueSource(randomGenerator.randomElement(SPECIMEN_TISSUE_SOURCE));
    specimen.setTumourNormalDesignation(randomGenerator.randomElement(TUMOUR_NORMAL_DESIGNATION));
    specimen.setSpecimenType(randomGenerator.randomElement(SPECIMEN_TYPE));
    specimen.setSubmitterSpecimenId(randomGenerator.generateRandomUUIDAsString());

    // Create specimen
    val specimenId = specimenService.create(studyId, specimen);
    specimen.setSpecimenId(specimenId);

    // Create samples
    val numSamples = 5;
    val expectedSampleIds = Sets.<String>newHashSet();
    for (int i = 0; i < numSamples; i++) {
      val sample = new Sample();
      sample.setSpecimenId(specimenId);
      sample.setSampleType(
          randomGenerator.randomElement(Lists.newArrayList(TestConstants.SAMPLE_TYPE)));
      sample.setSubmitterSampleId(randomGenerator.generateRandomUUIDAsString());
      val sampleId = sampleService.create(studyId, sample);
      expectedSampleIds.add(sampleId);
    }

    // Read the samples by parent Id (specimenId)
    val actualSamples = sampleService.readByParentId(specimenId);
    assertEquals(actualSamples.size(), numSamples);
    assertTrue(
        actualSamples.stream()
            .map(Sample::getSampleId)
            .collect(toSet())
            .containsAll(expectedSampleIds));

    // Assert that reading by a non-existent specimenId returns something empty
    val randomSpecimenId = randomGenerator.generateRandomUUIDAsString();
    assertFalse(specimenService.isSpecimenExist(randomSpecimenId));
    val emptySampleList = sampleService.readByParentId(randomSpecimenId);
    assertTrue(emptySampleList.isEmpty());

    // Delete by parent id
    val response = sampleService.deleteByParentId(specimenId);
    assertEquals(response, "OK");
    val emptySampleList2 = sampleService.readByParentId(specimenId);
    assertTrue(emptySampleList2.isEmpty());
  }

  @Test
  @Transactional
  public void testCheckSampleUnrelatedToStudy() {
    val existingSpecimenId = DEFAULT_SPECIMEN_ID;
    val secureSampleTester =
        createSecureSampleTester(randomGenerator, studyService, specimenService, sampleService);

    secureSampleTester.runSecureTest(
        (studyId, id) -> sampleService.checkSampleRelatedToStudy(studyId, id), existingSpecimenId);

    secureSampleTester.runSecureTest(
        (studyId, id) -> sampleService.securedRead(studyId, id), existingSpecimenId);

    secureSampleTester.runSecureTest(
        (studyId, id) -> sampleService.securedDelete(studyId, id), existingSpecimenId);

    secureSampleTester.runSecureTest(
        (studyId, id) -> sampleService.securedDelete(studyId, newArrayList(id)),
        existingSpecimenId);
  }

  @Test
  public void testUpdateSpecimenDNE() {
    val randomSampleId = randomGenerator.generateRandomUUIDAsString();
    val sample = new Sample();
    sample.setSubmitterSampleId(randomGenerator.generateRandomUUIDAsString());
    sample.setSampleId(randomSampleId);
    sample.setSampleType(
        randomGenerator.randomElement(Lists.newArrayList(TestConstants.SAMPLE_TYPE)));
    sample.setSpecimenId(DEFAULT_SPECIMEN_ID);
    SongErrorAssertions.assertSongError(() -> sampleService.update(sample), SAMPLE_DOES_NOT_EXIST);
  }
}
