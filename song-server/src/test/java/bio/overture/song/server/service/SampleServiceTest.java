/*
 * Copyright (c) 2018. Ontario Institute for Cancer Research
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

import bio.overture.song.core.testing.SongErrorAssertions;
import bio.overture.song.core.utils.JsonUtils;
import bio.overture.song.core.utils.RandomGenerator;
import bio.overture.song.server.model.entity.Sample;
import bio.overture.song.server.model.entity.Specimen;
import bio.overture.song.server.model.enums.Constants;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.val;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.assertThat;
import static bio.overture.song.core.exceptions.ServerErrors.SAMPLE_ALREADY_EXISTS;
import static bio.overture.song.core.exceptions.ServerErrors.SAMPLE_DOES_NOT_EXIST;
import static bio.overture.song.core.exceptions.ServerErrors.SAMPLE_ID_IS_CORRUPTED;
import static bio.overture.song.core.exceptions.ServerErrors.STUDY_ID_DOES_NOT_EXIST;
import static bio.overture.song.core.testing.SongErrorAssertions.assertSongError;
import static bio.overture.song.core.utils.RandomGenerator.createRandomGenerator;
import static bio.overture.song.server.utils.TestConstants.DEFAULT_DONOR_ID;
import static bio.overture.song.server.utils.TestConstants.DEFAULT_SAMPLE_ID;
import static bio.overture.song.server.utils.TestConstants.DEFAULT_SPECIMEN_ID;
import static bio.overture.song.server.utils.TestConstants.DEFAULT_STUDY_ID;
import static bio.overture.song.server.utils.TestFiles.getInfoName;
import static bio.overture.song.server.utils.securestudy.impl.SecureSampleTester.createSecureSampleTester;

@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles("dev")
public class SampleServiceTest {

  @Autowired
  SampleService sampleService;
  @Autowired
  StudyService studyService;
  @Autowired
  SpecimenService specimenService;

  private final RandomGenerator randomGenerator = createRandomGenerator(SampleServiceTest.class.getSimpleName());

  @Before
  public void beforeTest(){
    assertThat(studyService.isStudyExist(DEFAULT_STUDY_ID)).isTrue();
    assertThat(specimenService.isSpecimenExist(DEFAULT_SPECIMEN_ID)).isTrue();
  }

  @Test
  public void testReadSample() {
    val id = "SA1";
    val sample = sampleService.securedRead(DEFAULT_STUDY_ID,id);
    assertThat(sample.getSampleId()).isEqualTo(id);
    assertThat(sample.getSampleSubmitterId()).isEqualTo("T285-G7-A5");
    assertThat(sample.getSampleType()).isEqualTo("DNA");
    assertThat(getInfoName(sample)).isEqualTo("sample1");
  }

  @Test
  public void testCreateAndDeleteSample() {
    val specimenId = "SP2";
    val metadata = JsonUtils.fromSingleQuoted("{'ageCategory': 3, 'species': 'human'}");
    val s = Sample.builder()
        .sampleId("")
        .sampleSubmitterId( "101-IP-A")
        .specimenId(specimenId)
        .sampleType("Amplified DNA")
        .build();
    s.setInfo(metadata);

    val status = sampleService.create(DEFAULT_STUDY_ID, s);
    val id = s.getSampleId();
    assertThat(sampleService.isSampleExist(id)).isTrue();

    assertThat(id).startsWith("SA");
    Assertions.assertThat(status).isEqualTo(id);

    Sample check = sampleService.securedRead(DEFAULT_STUDY_ID, id);
    assertThat(check).isEqualToComparingFieldByField(s);

    sampleService.securedDelete(DEFAULT_STUDY_ID, newArrayList(id));
    assertThat(sampleService.isSampleExist(id)).isFalse();
  }

  @Test
  public void testUpdateSample() {

    val specimenId = "SP2";
    val s = Sample.builder()
        .sampleId("")
        .specimenId(specimenId)
        .sampleSubmitterId("102-CBP-A")
        .sampleType("RNA")
        .build();

    sampleService.create(DEFAULT_STUDY_ID, s);

    val id = s.getSampleId();

    val metadata = JsonUtils.fromSingleQuoted("{'species': 'Canadian Beaver'}");
    val s2 = Sample.builder()
        .sampleId(id)
        .sampleSubmitterId("Sample 102")
        .specimenId(s.getSpecimenId())
        .sampleType( "FFPE RNA")
        .build();
    s2.setInfo(metadata);
    sampleService.update(s2);

    val s3 = sampleService.securedRead(DEFAULT_STUDY_ID,id);
    assertThat(s3).isEqualToComparingFieldByField(s2);
  }

  @Test
  public void testCreateStudyDNE(){
    val randomStudyId = randomGenerator.generateRandomUUIDAsString();
    val sample = new Sample();
    SongErrorAssertions.assertSongError(() -> sampleService.create(randomStudyId, sample), STUDY_ID_DOES_NOT_EXIST);
  }

  @Test
  public void testCreateCorruptedAndAlreadyExists(){
    val specimenId = DEFAULT_SPECIMEN_ID;
    val existingStudyId = DEFAULT_STUDY_ID;

    val sample = new Sample();
    sample.setSampleSubmitterId(randomGenerator.generateRandomUUIDAsString());
    sample.setSampleType(randomGenerator.randomElement(Lists.newArrayList(Constants.SAMPLE_TYPE)));
    sample.setSpecimenId(specimenId);

    // Create a sample
    val sampleId = sampleService.create(existingStudyId, sample);
    assertThat(sampleService.isSampleExist(sampleId)).isTrue();

    // Try to create the sample again, and assert that the right exception is thrown
    SongErrorAssertions.assertSongError(() -> sampleService.create(existingStudyId, sample), SAMPLE_ALREADY_EXISTS);

    // 'Accidentally' set the sampleId to something not generated by the idService, and try to create. Should
    // detected the corrupted id field, indicating user might have accidentally set the id, thinking it would be
    // persisted
    val sample2 = new Sample();
    sample2.setSpecimenId(specimenId);
    sample2.setSampleType(randomGenerator.randomElement(Lists.newArrayList(Constants.SAMPLE_TYPE)));
    sample2.setSampleSubmitterId(randomGenerator.generateRandomUUIDAsString());
    sample2.setSampleId(randomGenerator.generateRandomUUIDAsString());
    assertThat(sampleService.isSampleExist(sample2.getSampleId())).isFalse();
    SongErrorAssertions.assertSongError(() -> sampleService.create(existingStudyId, sample2), SAMPLE_ID_IS_CORRUPTED);
  }

  @Test
  public void testSampleExists(){
    val existingSampleId= DEFAULT_SAMPLE_ID;
    assertThat(sampleService.isSampleExist(existingSampleId)).isTrue();
    sampleService.checkSampleExists(existingSampleId);
    val nonExistingSampleId = randomGenerator.generateRandomUUIDAsString();
    assertThat(sampleService.isSampleExist(nonExistingSampleId)).isFalse();
    sampleService.checkSampleExists(existingSampleId);
    sampleService.checkSampleDoesNotExist(nonExistingSampleId);

    assertSongError(() -> sampleService.checkSampleExists(nonExistingSampleId), SAMPLE_DOES_NOT_EXIST);
    assertSongError(() -> sampleService.checkSampleDoesNotExist(existingSampleId), SAMPLE_ALREADY_EXISTS);
  }

  @Test
  public void testReadSampleDNE(){
    val randomSampleId = randomGenerator.generateRandomUUIDAsString();
    assertThat(sampleService.isSampleExist(randomSampleId)).isFalse();
    SongErrorAssertions.assertSongError(() -> sampleService.unsecuredRead(randomSampleId), SAMPLE_DOES_NOT_EXIST);
  }

  @Test
  public void testReadAndDeleteByParentId(){
    val studyId = DEFAULT_STUDY_ID;
    val donorId = DEFAULT_DONOR_ID;
    val specimen = new Specimen();
    specimen.setDonorId(donorId);
    specimen.setSpecimenClass(randomGenerator.randomElement(Lists.newArrayList(Constants.SPECIMEN_CLASS)));
    specimen.setSpecimenType(randomGenerator.randomElement(Lists.newArrayList(Constants.SPECIMEN_TYPE)));
    specimen.setSpecimenSubmitterId(randomGenerator.generateRandomUUIDAsString());

    // Create specimen
    val specimenId = specimenService.create(studyId, specimen);
    specimen.setSpecimenId(specimenId);

    // Create samples
    val numSamples = 5;
    val expectedSampleIds = Sets.<String>newHashSet();
    for (int i =0; i< numSamples; i++){
      val sample = new Sample();
      sample.setSpecimenId(specimenId);
      sample.setSampleType(randomGenerator.randomElement(Lists.newArrayList(Constants.SAMPLE_TYPE)));
      sample.setSampleSubmitterId(randomGenerator.generateRandomUUIDAsString());
      val sampleId = sampleService.create(studyId, sample);
      expectedSampleIds.add(sampleId);
    }

    // Read the samples by parent Id (specimenId)
    val actualSamples = sampleService.readByParentId(specimenId);
    Assertions.assertThat(actualSamples).hasSize(numSamples);
    assertThat(actualSamples.stream().map(Sample::getSampleId).collect(toSet())).containsAll(expectedSampleIds);

    // Assert that reading by a non-existent specimenId returns something empty
    val randomSpecimenId = randomGenerator.generateRandomUUIDAsString();
    assertThat(specimenService.isSpecimenExist(randomSpecimenId)).isFalse();
    val emptySampleList = sampleService.readByParentId(randomSpecimenId);
    Assertions.assertThat(emptySampleList).isEmpty();

    // Delete by parent id
    val response = sampleService.deleteByParentId(specimenId);
    Assertions.assertThat(response).isEqualTo("OK");
    val emptySampleList2 = sampleService.readByParentId(specimenId);
    Assertions.assertThat(emptySampleList2).isEmpty();
  }

  @Test
  @Transactional
  public void testCheckSampleUnrelatedToStudy(){
    val existingSpecimenId = DEFAULT_SPECIMEN_ID;
    val secureSampleTester = createSecureSampleTester(randomGenerator,studyService, specimenService, sampleService);

    secureSampleTester.runSecureTest((studyId, id) -> sampleService.checkSampleRelatedToStudy(studyId, id),
        existingSpecimenId);

    secureSampleTester.runSecureTest((studyId, id) -> sampleService.securedRead(studyId, id),
        existingSpecimenId);

    secureSampleTester.runSecureTest((studyId, id) -> sampleService.securedDelete(studyId, id),
        existingSpecimenId);

    secureSampleTester.runSecureTest((studyId, id) -> sampleService.securedDelete(studyId, newArrayList(id)),
        existingSpecimenId);

  }


  @Test
  public void testUpdateSpecimenDNE(){
    val randomSampleId = randomGenerator.generateRandomUUIDAsString();
    val sample = new Sample();
    sample.setSampleSubmitterId(randomGenerator.generateRandomUUIDAsString());
    sample.setSampleId(randomSampleId);
    sample.setSampleType(randomGenerator.randomElement(Lists.newArrayList(Constants.SAMPLE_TYPE)));
    sample.setSpecimenId(DEFAULT_SPECIMEN_ID);
    SongErrorAssertions.assertSongError(() -> sampleService.update(sample), SAMPLE_DOES_NOT_EXIST);
  }

}
