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
package org.icgc.dcc.song.server.service;

import com.google.common.collect.Sets;
import lombok.val;
import org.icgc.dcc.song.core.utils.JsonUtils;
import org.icgc.dcc.song.core.utils.RandomGenerator;
import org.icgc.dcc.song.server.model.entity.Donor;
import org.icgc.dcc.song.server.model.entity.Specimen;
import org.icgc.dcc.song.server.model.entity.composites.DonorWithSpecimens;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.DONOR_ALREADY_EXISTS;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.DONOR_DOES_NOT_EXIST;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.DONOR_ID_IS_CORRUPTED;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.STUDY_ID_DOES_NOT_EXIST;
import static org.icgc.dcc.song.core.testing.SongErrorAssertions.assertSongError;
import static org.icgc.dcc.song.core.utils.RandomGenerator.createRandomGenerator;
import static org.icgc.dcc.song.server.model.enums.Constants.DONOR_GENDER;
import static org.icgc.dcc.song.server.utils.StudyGenerator.createStudyGenerator;
import static org.icgc.dcc.song.server.utils.TestConstants.DEFAULT_DONOR_ID;
import static org.icgc.dcc.song.server.utils.TestConstants.DEFAULT_STUDY_ID;
import static org.icgc.dcc.song.server.utils.TestFiles.getInfoName;

@SpringBootTest
@RunWith(SpringRunner.class)
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class})
@ActiveProfiles("dev")
public class DonorServiceTest {

  @Autowired
  DonorService service;
  @Autowired
  SpecimenService specimenService;
  @Autowired
  IdService idService;
  @Autowired
  StudyService studyService;


  private final RandomGenerator randomGenerator = createRandomGenerator(DonorServiceTest.class.getSimpleName());

  @Before
  public void beforeTest(){
    assertThat(studyService.isStudyExist(DEFAULT_STUDY_ID)).isTrue();
  }

  @Test
  public void testReadDonor() {
    // check for data that we know exists in the H2 database already
    val d = service.readWithSpecimens(DEFAULT_DONOR_ID);
    assertThat(d).isNotNull();
    assertThat(d.getDonorId()).isEqualTo(DEFAULT_DONOR_ID);
    assertThat(d.getDonorGender()).isEqualTo("male");
    assertThat(d.getDonorSubmitterId()).isEqualTo("Subject-X23Alpha7");
    assertThat(d.getSpecimens().size()).isEqualTo(2);
    assertThat(getInfoName(d)).isEqualTo("donor1");

    // Just check that each specimen object that we get is the same as the one we get from the
    // specimen service. Let the specimen service tests verify that the contents are right.
    d.getSpecimens().forEach(specimen -> assertThat(specimen.equals(getMatchingSpecimen(specimen))));

  }

  Specimen getMatchingSpecimen(Specimen specimen) {
    return specimenService.read(specimen.getSpecimenId());
  }

  @Test
  public void testCreateAndDeleteDonor() {
    val json = JsonUtils.mapper().createObjectNode();
    val studyId = "XYZ234";
    json.put("donorId", "");
    json.put("donorSubmitterId", "Subject X21-Alpha");
    json.put("studyId", studyId);
    json.put("donorGender", "unspecified");
    json.put("species", "human");

    DonorWithSpecimens d = JsonUtils.mapper().convertValue(json, DonorWithSpecimens.class);
    assertThat(d.getDonorId()).isNull();

    val status = service.create(d);
    val id = d.getDonorId();

    assertThat(id).startsWith("DO");
    assertThat(status).isEqualTo(id);

    DonorWithSpecimens check = service.readWithSpecimens(id);
    assertThat(d).isEqualToComparingFieldByField(check);

    service.delete("XYZ234", id);
    assertThat(service.isDonorExist(id)).isFalse();

    val status2 = service.create(d);
    assertThat(status2).isEqualTo(id);
    service.delete("XYZ234", newArrayList(id));
    assertThat(service.isDonorExist(id)).isFalse();
  }

  @Test
  public void testUpdateDonor() {
    val studyId = DEFAULT_STUDY_ID;
    val info = JsonUtils.fromSingleQuoted("{'test': 'new json'}");

    val d = new DonorWithSpecimens();
    d.setDonorId("");
    d.setDonorSubmitterId("Triangle-Arrow-S");
    d.setStudyId(studyId);
    d.setDonorGender("male");
    val id= service.create(d);
    assertThat(id).isEqualTo(d.getDonorId());


    val d2 = new Donor();
    d2.setDonorId(id);
    d2.setDonorSubmitterId("X21-Beta-17");
    d2.setStudyId(studyId);
    d2.setDonorGender("female");
    d2.setInfo(info);

    val response = service.update(d2);
    assertThat(response).isEqualTo("OK");

    val d3 = service.read(id);
    assertThat(d3).isEqualToComparingFieldByField(d2);
  }

  @Test
  public void testSave(){
    val studyId = DEFAULT_STUDY_ID;
    val donorSubmitterId = randomGenerator.generateRandomUUIDAsString();
    val d = new DonorWithSpecimens();
    d.setDonorId("");
    d.setDonorSubmitterId(donorSubmitterId);
    d.setStudyId(studyId);
    d.setDonorGender("male");
    val donorId = service.save(studyId, d);
    val initialDonor = service.read(donorId);
    assertThat(initialDonor.getDonorGender()).isEqualTo("male");
    assertThat(service.isDonorExist(donorId)).isTrue();

    val dUpdate = new DonorWithSpecimens();
    dUpdate.setDonorSubmitterId(donorSubmitterId);
    dUpdate.setStudyId(studyId);
    dUpdate.setDonorGender("female");
    val donorId2 = service.save(studyId, dUpdate);
    assertThat(service.isDonorExist(donorId2)).isTrue();
    assertThat(donorId2).isEqualTo(donorId);
    val updateDonor = service.read(donorId2);
    assertThat(updateDonor.getDonorGender()).isEqualTo("female");
  }

  @Test
  public void testSaveStudyDNE(){
    val studyId = DEFAULT_STUDY_ID;
    val randomStudyId = randomGenerator.generateRandomUUIDAsString();
    assertThat(studyService.isStudyExist(randomStudyId)).isFalse();
    val donorSubmitterId = randomGenerator.generateRandomUUIDAsString();
    val d = new DonorWithSpecimens();
    d.setDonorId("");
    d.setDonorSubmitterId(donorSubmitterId);
    d.setStudyId(studyId);
    d.setDonorGender("male");
    val donorId = service.create(d);
    assertThat(service.isDonorExist(donorId)).isTrue();
    assertSongError(() -> service.save(randomStudyId, d), STUDY_ID_DOES_NOT_EXIST);

    val d2 = new DonorWithSpecimens();
    d2.setDonorId("");
    d2.setDonorSubmitterId(randomGenerator.generateRandomUUIDAsString());
    d2.setStudyId(randomStudyId);
    d2.setDonorGender("female");
    assertSongError(() -> service.save(randomStudyId, d2), STUDY_ID_DOES_NOT_EXIST);
  }

  @Test
  public void testDeleteByParentId(){
    val studyGenerator = createStudyGenerator(studyService, randomGenerator);
    val randomStudyId = studyGenerator.createRandomStudy();

    val d1 = new DonorWithSpecimens();
    d1.setDonorId("");
    d1.setDonorSubmitterId(randomGenerator.generateRandomUUIDAsString());
    d1.setStudyId(randomStudyId);
    d1.setDonorGender("female");
    val id1 = service.create(d1);
    d1.setDonorId(id1);

    val d2 = new DonorWithSpecimens();
    d2.setDonorId("");
    d2.setDonorSubmitterId(randomGenerator.generateRandomUUIDAsString());
    d2.setStudyId(randomStudyId);
    d2.setDonorGender("male");
    val id2 = service.create(d2);
    d2.setDonorId(id2);

    val actualDonorWithSpecimens = service.readByParentId(randomStudyId);
    assertThat(actualDonorWithSpecimens).contains(d1, d2);
    val response = service.deleteByParentId(randomStudyId);
    assertThat(response).isEqualTo("OK");
    val emptyDonorWithSpecimens = service.readByParentId(randomStudyId);
    assertThat(emptyDonorWithSpecimens).isEmpty();
  }

  @Test
  public void testDeleteByParentIdStudyDNE(){
    val randomStudyId = randomGenerator.generateRandomUUIDAsString();
    assertSongError(() -> service.deleteByParentId(randomStudyId), STUDY_ID_DOES_NOT_EXIST);
  }

  @Test
  public void testDonorCheck(){
    val randomDonorId = randomGenerator.generateRandomUUIDAsString();
    val randomDonorSubmitterId = randomGenerator.generateRandomUUID().toString();
    val randomDonorGender = randomGenerator.randomElement(newArrayList(DONOR_GENDER));
    assertThat(service.isDonorExist(randomDonorId)).isFalse();
    assertSongError(() -> service.checkDonorExists(randomDonorId), DONOR_DOES_NOT_EXIST);
    val donorId = service.save(DEFAULT_STUDY_ID, Donor.create(randomDonorId, randomDonorSubmitterId,
        DEFAULT_STUDY_ID, randomDonorGender));

    assertThat(service.isDonorExist(donorId)).isTrue();
    service.checkDonorExists(donorId);
  }

  @Test
  public void testCreateStudyDNE(){
    val donor = createRandomDonor();
    val donorWithSpecimens = new DonorWithSpecimens();
    donorWithSpecimens.setDonor(donor);
    assertSongError(() -> service.create(donorWithSpecimens), STUDY_ID_DOES_NOT_EXIST);
  }

  @Test
  public void testReadByParentId(){
    val studyGenerator = createStudyGenerator(studyService, randomGenerator);
    val studyId = studyGenerator.createRandomStudy();
    val numDonors = 7;
    val donorIdSet = Sets.<String>newHashSet();
    for (int i =0; i<numDonors; i++){
      val d = new DonorWithSpecimens();
      d.setDonorGender("male");
      d.setStudyId(studyId);
      d.setDonorSubmitterId(randomGenerator.generateRandomUUIDAsString());
      val donorId = service.create(d);
      donorIdSet.add(donorId);
    }
    val donors = service.readByParentId(studyId);
    assertThat(donors).hasSize(numDonors);
    assertThat(donors.stream().map(Donor::getDonorId).collect(toSet())).containsAll(donorIdSet);
  }

  @Test
  public void testReadByParentIdStudyDNE(){
    val randomStudyId = randomGenerator.generateRandomUUIDAsString();
    assertSongError(() -> service.readByParentId(randomStudyId), STUDY_ID_DOES_NOT_EXIST);
  }

  @Test
  public void testCreateDonorAlreadyExists(){
    val studyId = DEFAULT_STUDY_ID;
    val randomGender = randomGenerator.randomElement(newArrayList(DONOR_GENDER));
    val randomDonorSubmitterId = randomGenerator.generateRandomUUIDAsString();
    val expectedId = idService.generateDonorId(randomDonorSubmitterId, studyId);

    val donorWithSpecimens = new DonorWithSpecimens();
    donorWithSpecimens.setStudyId(studyId);
    donorWithSpecimens.setDonorSubmitterId(randomDonorSubmitterId);
    donorWithSpecimens.setDonorGender(randomGender);
    donorWithSpecimens.setInfo("someKey", "someValue");
    val donorId = service.create(donorWithSpecimens);
    assertThat(donorId).isEqualTo(expectedId);

    donorWithSpecimens.setDonorId("DO123");
    assertSongError(() -> service.create(donorWithSpecimens), DONOR_ID_IS_CORRUPTED);

    donorWithSpecimens.setDonorId(expectedId);
    assertSongError(() -> service.create(donorWithSpecimens), DONOR_ALREADY_EXISTS);

    donorWithSpecimens.setDonorId("");
    assertSongError(() -> service.create(donorWithSpecimens), DONOR_ALREADY_EXISTS);

    donorWithSpecimens.setDonorId(null);
    assertSongError(() -> service.create(donorWithSpecimens), DONOR_ALREADY_EXISTS);
  }

  @Test
  public void testUpdateDonorDNE(){
    val randomDonor = createRandomDonor();
    assertSongError(() ->  service.update(randomDonor), DONOR_DOES_NOT_EXIST);
  }

  @Test
  public void testDeleteStudyDNE(){
    val randomDonorId =  randomGenerator.generateRandomUUIDAsString();
    val randomStudyId=  randomGenerator.generateRandomUUIDAsString();
    assertSongError(() -> service.delete(randomStudyId, randomDonorId), STUDY_ID_DOES_NOT_EXIST);
    assertSongError(() -> service.delete(randomStudyId, newArrayList(randomDonorId)), STUDY_ID_DOES_NOT_EXIST);
  }

  @Test
  public void testDeleteDonorDNE(){
    val studyId = DEFAULT_STUDY_ID;
    val randomDonorId =  randomGenerator.generateRandomUUIDAsString();
    assertSongError(() -> service.delete(studyId, randomDonorId), DONOR_DOES_NOT_EXIST);
    assertSongError(() -> service.delete(studyId, newArrayList(randomDonorId, DEFAULT_DONOR_ID)),
        DONOR_DOES_NOT_EXIST);
  }

  @Test
  public void testReadDonorDNE(){
    val randomDonorId =  randomGenerator.generateRandomUUIDAsString();
    assertThat(service.isDonorExist(randomDonorId)).isFalse();
    assertSongError(() -> service.read(randomDonorId), DONOR_DOES_NOT_EXIST);
    assertSongError(() -> service.readWithSpecimens(randomDonorId), DONOR_DOES_NOT_EXIST);
  }

  private Donor createRandomDonor(){
    val randomStudyId = randomGenerator.generateRandomUUIDAsString();
    val randomDonorSubmitterId = randomGenerator.generateRandomUUIDAsString();
    val randomDonorId = randomGenerator.generateRandomUUIDAsString();
    return Donor.create(randomDonorId, randomDonorSubmitterId, randomStudyId,
        "male" );
  }

}
