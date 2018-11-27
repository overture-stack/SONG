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
import bio.overture.song.server.model.entity.Donor;
import bio.overture.song.server.model.entity.Specimen;
import bio.overture.song.server.model.entity.composites.DonorWithSpecimens;
import bio.overture.song.server.utils.securestudy.impl.SecureDonorTester;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
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
import static bio.overture.song.core.exceptions.ServerErrors.DONOR_ALREADY_EXISTS;
import static bio.overture.song.core.exceptions.ServerErrors.DONOR_DOES_NOT_EXIST;
import static bio.overture.song.core.exceptions.ServerErrors.DONOR_ID_IS_CORRUPTED;
import static bio.overture.song.core.exceptions.ServerErrors.STUDY_ID_DOES_NOT_EXIST;
import static bio.overture.song.core.testing.SongErrorAssertions.assertSongError;
import static bio.overture.song.core.utils.RandomGenerator.createRandomGenerator;
import static bio.overture.song.server.model.enums.Constants.DONOR_GENDER;
import static bio.overture.song.server.utils.TestConstants.DEFAULT_DONOR_ID;
import static bio.overture.song.server.utils.TestConstants.DEFAULT_STUDY_ID;
import static bio.overture.song.server.utils.TestFiles.getInfoName;
import static bio.overture.song.server.utils.generator.StudyGenerator.createStudyGenerator;
import static bio.overture.song.server.utils.securestudy.impl.SecureDonorTester.createSecureDonorTester;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
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
  private SecureDonorTester secureDonorTester;

  @Before
  public void beforeTest(){
    assertThat(studyService.isStudyExist(DEFAULT_STUDY_ID)).isTrue();
    this.secureDonorTester = createSecureDonorTester(randomGenerator, studyService, service);
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
    return specimenService.unsecuredRead(specimen.getSpecimenId());
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
    Assertions.assertThat(d.getDonorId()).isNull();

    val status = service.create(d);
    val id = d.getDonorId();

    Assertions.assertThat(id).startsWith("DO");
    Assertions.assertThat(status).isEqualTo(id);

    DonorWithSpecimens check = service.readWithSpecimens(id);
    assertThat(d).isEqualToComparingFieldByField(check);

    service.securedDelete("XYZ234", id);
    assertThat(service.isDonorExist(id)).isFalse();

    val status2 = service.create(d);
    Assertions.assertThat(status2).isEqualTo(id);
    service.securedDelete("XYZ234", newArrayList(id));
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
    Assertions.assertThat(id).isEqualTo(d.getDonorId());


    val d2 = new Donor();
    d2.setDonorId(id);
    d2.setDonorSubmitterId("X21-Beta-17");
    d2.setStudyId(studyId);
    d2.setDonorGender("female");
    d2.setInfo(info);

    val response = service.update(d2);
    Assertions.assertThat(response).isEqualTo("OK");

    val d3 = service.securedRead(studyId, id);
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
    val initialDonor = service.securedRead(studyId, donorId);
    assertThat(initialDonor.getDonorGender()).isEqualTo("male");
    assertThat(service.isDonorExist(donorId)).isTrue();

    val dUpdate = new DonorWithSpecimens();
    dUpdate.setDonorSubmitterId(donorSubmitterId);
    dUpdate.setStudyId(studyId);
    dUpdate.setDonorGender("female");
    val donorId2 = service.save(studyId, dUpdate);
    assertThat(service.isDonorExist(donorId2)).isTrue();
    Assertions.assertThat(donorId2).isEqualTo(donorId);
    val updateDonor = service.securedRead(studyId, donorId2);
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
    SongErrorAssertions.assertSongError(() -> service.save(randomStudyId, d), STUDY_ID_DOES_NOT_EXIST);

    val d2 = new DonorWithSpecimens();
    d2.setDonorId("");
    d2.setDonorSubmitterId(randomGenerator.generateRandomUUIDAsString());
    d2.setStudyId(randomStudyId);
    d2.setDonorGender("female");
    SongErrorAssertions.assertSongError(() -> service.save(randomStudyId, d2), STUDY_ID_DOES_NOT_EXIST);
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
    Assertions.assertThat(actualDonorWithSpecimens).contains(d1, d2);
    val response = service.deleteByParentId(randomStudyId);
    Assertions.assertThat(response).isEqualTo("OK");
    val emptyDonorWithSpecimens = service.readByParentId(randomStudyId);
    Assertions.assertThat(emptyDonorWithSpecimens).isEmpty();
  }

  @Test
  public void testDeleteByParentIdStudyDNE(){
    val randomStudyId = randomGenerator.generateRandomUUIDAsString();
    SongErrorAssertions.assertSongError(() -> service.deleteByParentId(randomStudyId), STUDY_ID_DOES_NOT_EXIST);
  }

  @Test
  public void testDonorCheck(){
    val randomDonorId = randomGenerator.generateRandomUUIDAsString();
    val randomDonorSubmitterId = randomGenerator.generateRandomUUID().toString();
    val randomDonorGender = randomGenerator.randomElement(newArrayList(DONOR_GENDER));
    val expectedId = idService.generateDonorId(randomDonorSubmitterId, DEFAULT_STUDY_ID);
    assertThat(service.isDonorExist(expectedId)).isFalse();
    SongErrorAssertions.assertSongErrorRunnable(() -> service.checkDonorExists(randomDonorId), DONOR_DOES_NOT_EXIST);
    val donorId = service.save(DEFAULT_STUDY_ID,
        Donor.builder()
        .donorId(null)
        .donorSubmitterId(randomDonorSubmitterId)
        .studyId(DEFAULT_STUDY_ID)
        .donorGender(randomDonorGender)
        .build());
    Assertions.assertThat(donorId).isEqualTo(expectedId);
    assertThat(service.isDonorExist(donorId)).isTrue();
    service.checkDonorExists(donorId);
  }

  @Test
  public void testCreateStudyDNE(){
    val donor = createRandomDonor();
    val donorWithSpecimens = new DonorWithSpecimens();
    donorWithSpecimens.setDonor(donor);
    SongErrorAssertions.assertSongError(() -> service.create(donorWithSpecimens), STUDY_ID_DOES_NOT_EXIST);
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
    Assertions.assertThat(donors).hasSize(numDonors);
    assertThat(donors.stream().map(Donor::getDonorId).collect(toSet())).containsAll(donorIdSet);
  }

  @Test
  public void testReadByParentIdStudyDNE(){
    val randomStudyId = randomGenerator.generateRandomUUIDAsString();
    SongErrorAssertions.assertSongError(() -> service.readByParentId(randomStudyId), STUDY_ID_DOES_NOT_EXIST);
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
    Assertions.assertThat(donorId).isEqualTo(expectedId);

    donorWithSpecimens.setDonorId("DO123");
    SongErrorAssertions.assertSongError(() -> service.create(donorWithSpecimens), DONOR_ID_IS_CORRUPTED);

    donorWithSpecimens.setDonorId(expectedId);
    SongErrorAssertions.assertSongError(() -> service.create(donorWithSpecimens), DONOR_ALREADY_EXISTS);

    donorWithSpecimens.setDonorId("");
    SongErrorAssertions.assertSongError(() -> service.create(donorWithSpecimens), DONOR_ALREADY_EXISTS);

    donorWithSpecimens.setDonorId(null);
    SongErrorAssertions.assertSongError(() -> service.create(donorWithSpecimens), DONOR_ALREADY_EXISTS);
  }

  @Test
  public void testUpdateDonorDNE(){
    val randomDonor = createRandomDonor();
    SongErrorAssertions.assertSongError(() ->  service.update(randomDonor), DONOR_DOES_NOT_EXIST);
  }

  @Test
  public void testReadDonorDNE(){
    val data = secureDonorTester.generateData();
    SongErrorAssertions.assertSongError(() -> service.unsecuredRead(data.getNonExistingId()), DONOR_DOES_NOT_EXIST);
    SongErrorAssertions.assertSongError(() -> service.readWithSpecimens(data.getNonExistingId()), DONOR_DOES_NOT_EXIST);
  }

  @Test
  @Transactional
  public void testCheckDonorUnRelatedToStudy(){
    secureDonorTester.runSecureAnalysisTest((s,d) -> service.checkDonorAndStudyRelated(s, d));
    secureDonorTester.runSecureAnalysisTest((s,d) -> service.securedRead(s, d));
    secureDonorTester.runSecureAnalysisTest((s,d) -> service.securedDelete(s, d));
    secureDonorTester.runSecureAnalysisTest((s,d) -> service.securedDelete(s, newArrayList(d)));
  }

  private Donor createRandomDonor(){
    val randomStudyId = randomGenerator.generateRandomUUIDAsString();
    val randomDonorSubmitterId = randomGenerator.generateRandomUUIDAsString();
    val randomDonorId = randomGenerator.generateRandomUUIDAsString();
    return Donor.builder()
        .donorId(randomDonorId)
        .donorSubmitterId(randomDonorSubmitterId)
        .studyId(randomStudyId)
        .donorGender("male")
        .build();
  }

}
