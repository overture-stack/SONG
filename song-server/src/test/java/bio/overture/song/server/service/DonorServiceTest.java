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

import static bio.overture.song.core.exceptions.ServerErrors.DONOR_ALREADY_EXISTS;
import static bio.overture.song.core.exceptions.ServerErrors.DONOR_DOES_NOT_EXIST;
import static bio.overture.song.core.exceptions.ServerErrors.DONOR_ID_IS_CORRUPTED;
import static bio.overture.song.core.exceptions.ServerErrors.STUDY_ID_DOES_NOT_EXIST;
import static bio.overture.song.core.utils.RandomGenerator.createRandomGenerator;
import static bio.overture.song.server.model.enums.Constants.DONOR_GENDER;
import static bio.overture.song.server.utils.TestConstants.DEFAULT_DONOR_ID;
import static bio.overture.song.server.utils.TestConstants.DEFAULT_STUDY_ID;
import static bio.overture.song.server.utils.TestFiles.getInfoName;
import static bio.overture.song.server.utils.generator.StudyGenerator.createStudyGenerator;
import static bio.overture.song.server.utils.securestudy.impl.SecureDonorTester.createSecureDonorTester;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.stream.Collectors.toSet;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import bio.overture.song.core.testing.SongErrorAssertions;
import bio.overture.song.core.utils.JsonUtils;
import bio.overture.song.core.utils.RandomGenerator;
import bio.overture.song.server.model.entity.Donor;
import bio.overture.song.server.model.entity.Specimen;
import bio.overture.song.server.model.entity.composites.DonorWithSpecimens;
import bio.overture.song.server.utils.securestudy.impl.SecureDonorTester;
import com.google.common.collect.Sets;
import javax.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
public class DonorServiceTest {

  @Autowired DonorService service;
  @Autowired SpecimenService specimenService;
  @Autowired IdServiceOLD idServiceOLD;
  @Autowired StudyService studyService;

  private final RandomGenerator randomGenerator =
      createRandomGenerator(DonorServiceTest.class.getSimpleName());
  private SecureDonorTester secureDonorTester;

  @Before
  public void beforeTest() {
    assertTrue(studyService.isStudyExist(DEFAULT_STUDY_ID));
    this.secureDonorTester = createSecureDonorTester(randomGenerator, studyService, service);
  }

  @Test
  public void testReadDonor() {
    // check for data that we know exists in the H2 database already
    val d = service.readWithSpecimens(DEFAULT_DONOR_ID);
    assertNotNull(d);
    assertEquals(d.getDonorId(), DEFAULT_DONOR_ID);
    assertEquals(d.getDonorGender(), "male");
    assertEquals(d.getDonorSubmitterId(), "Subject-X23Alpha7");
    assertEquals(d.getSpecimens().size(), 2);
    assertEquals(getInfoName(d), "donor1");

    // Just check that each specimen object that we get is the same as the one we get from the
    // specimen service. Let the specimen service tests verify that the contents are right.
    d.getSpecimens()
        .forEach(specimen -> assertEqualSpecimen(specimen, getMatchingSpecimen(specimen)));
  }

  private static void assertEqualSpecimen(Specimen s1, Specimen s2) {
    assertEquals(s1.getDonorId(), s2.getDonorId());
    assertEquals(s1.getSpecimenClass(), s2.getSpecimenClass());
    assertEquals(s1.getSpecimenId(), s2.getSpecimenId());
    assertEquals(s1.getSpecimenSubmitterId(), s2.getSpecimenSubmitterId());
    assertEquals(s1.getSpecimenType(), s2.getSpecimenType());
    assertEquals(s1.getInfoAsString(), s2.getInfoAsString());
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
    assertNull(d.getDonorId());

    val status = service.create(d);
    val id = d.getDonorId();

    assertTrue(id.startsWith("DO"));
    assertEquals(status, id);

    DonorWithSpecimens check = service.readWithSpecimens(id);
    assertEquals(d, check);

    service.securedDelete("XYZ234", id);
    assertFalse(service.isDonorExist(id));

    val status2 = service.create(d);
    assertEquals(status2, id);
    service.securedDelete("XYZ234", newArrayList(id));
    assertFalse(service.isDonorExist(id));
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
    val id = service.create(d);
    assertEquals(id, d.getDonorId());

    val d2 = new Donor();
    d2.setDonorId(id);
    d2.setDonorSubmitterId("X21-Beta-17");
    d2.setStudyId(studyId);
    d2.setDonorGender("female");
    d2.setInfo(info);

    val response = service.update(d2);
    assertEquals(response, "OK");

    val d3 = service.securedRead(studyId, id);
    assertEquals(d3, d2);
  }

  @Test
  public void testSave() {
    val studyId = DEFAULT_STUDY_ID;
    val donorSubmitterId = randomGenerator.generateRandomUUIDAsString();
    val d = new DonorWithSpecimens();
    d.setDonorId("");
    d.setDonorSubmitterId(donorSubmitterId);
    d.setStudyId(studyId);
    d.setDonorGender("male");
    val donorId = service.save(studyId, d);
    val initialDonor = service.securedRead(studyId, donorId);
    assertEquals(initialDonor.getDonorGender(), "male");
    assertTrue(service.isDonorExist(donorId));

    val dUpdate = new DonorWithSpecimens();
    dUpdate.setDonorSubmitterId(donorSubmitterId);
    dUpdate.setStudyId(studyId);
    dUpdate.setDonorGender("female");
    val donorId2 = service.save(studyId, dUpdate);
    assertTrue(service.isDonorExist(donorId2));
    assertEquals(donorId2, donorId);
    val updateDonor = service.securedRead(studyId, donorId2);
    assertEquals(updateDonor.getDonorGender(), "female");
  }

  @Test
  public void testSaveStudyDNE() {
    val studyId = DEFAULT_STUDY_ID;
    val randomStudyId = randomGenerator.generateRandomUUIDAsString();
    assertFalse(studyService.isStudyExist(randomStudyId));
    val donorSubmitterId = randomGenerator.generateRandomUUIDAsString();
    val d = new DonorWithSpecimens();
    d.setDonorId("");
    d.setDonorSubmitterId(donorSubmitterId);
    d.setStudyId(studyId);
    d.setDonorGender("male");
    val donorId = service.create(d);
    assertTrue(service.isDonorExist(donorId));
    SongErrorAssertions.assertSongError(
        () -> service.save(randomStudyId, d), STUDY_ID_DOES_NOT_EXIST);

    val d2 = new DonorWithSpecimens();
    d2.setDonorId("");
    d2.setDonorSubmitterId(randomGenerator.generateRandomUUIDAsString());
    d2.setStudyId(randomStudyId);
    d2.setDonorGender("female");
    SongErrorAssertions.assertSongError(
        () -> service.save(randomStudyId, d2), STUDY_ID_DOES_NOT_EXIST);
  }

  @Test
  public void testDeleteByParentId() {
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
    assertThat(actualDonorWithSpecimens, contains(d1, d2));
    val response = service.deleteByParentId(randomStudyId);
    assertEquals(response, "OK");
    val emptyDonorWithSpecimens = service.readByParentId(randomStudyId);
    assertTrue(emptyDonorWithSpecimens.isEmpty());
  }

  @Test
  public void testDeleteByParentIdStudyDNE() {
    val randomStudyId = randomGenerator.generateRandomUUIDAsString();
    SongErrorAssertions.assertSongError(
        () -> service.deleteByParentId(randomStudyId), STUDY_ID_DOES_NOT_EXIST);
  }

  @Test
  public void testDonorCheck() {
    val randomDonorId = randomGenerator.generateRandomUUIDAsString();
    val randomDonorSubmitterId = randomGenerator.generateRandomUUID().toString();
    val randomDonorGender = randomGenerator.randomElement(newArrayList(DONOR_GENDER));
    val expectedId = idServiceOLD.generateDonorId(randomDonorSubmitterId, DEFAULT_STUDY_ID);
    assertFalse(service.isDonorExist(expectedId));
    SongErrorAssertions.assertSongErrorRunnable(
        () -> service.checkDonorExists(randomDonorId), DONOR_DOES_NOT_EXIST);
    val donorId =
        service.save(
            DEFAULT_STUDY_ID,
            Donor.builder()
                .donorId(null)
                .donorSubmitterId(randomDonorSubmitterId)
                .studyId(DEFAULT_STUDY_ID)
                .donorGender(randomDonorGender)
                .build());
    assertEquals(donorId, expectedId);
    assertTrue(service.isDonorExist(donorId));
    service.checkDonorExists(donorId);
  }

  @Test
  public void testCreateStudyDNE() {
    val donor = createRandomDonor();
    val donorWithSpecimens = new DonorWithSpecimens();
    donorWithSpecimens.setDonor(donor);
    SongErrorAssertions.assertSongError(
        () -> service.create(donorWithSpecimens), STUDY_ID_DOES_NOT_EXIST);
  }

  @Test
  public void testReadByParentId() {
    val studyGenerator = createStudyGenerator(studyService, randomGenerator);
    val studyId = studyGenerator.createRandomStudy();
    val numDonors = 7;
    val donorIdSet = Sets.<String>newHashSet();
    for (int i = 0; i < numDonors; i++) {
      val d = new DonorWithSpecimens();
      d.setDonorGender("male");
      d.setStudyId(studyId);
      d.setDonorSubmitterId(randomGenerator.generateRandomUUIDAsString());
      val donorId = service.create(d);
      donorIdSet.add(donorId);
    }
    val donors = service.readByParentId(studyId);
    assertEquals(donors.size(), numDonors);
    assertTrue(donors.stream().map(Donor::getDonorId).collect(toSet()).containsAll(donorIdSet));
  }

  @Test
  public void testReadByParentIdStudyDNE() {
    val randomStudyId = randomGenerator.generateRandomUUIDAsString();
    SongErrorAssertions.assertSongError(
        () -> service.readByParentId(randomStudyId), STUDY_ID_DOES_NOT_EXIST);
  }

  @Test
  public void testCreateDonorAlreadyExists() {
    val studyId = DEFAULT_STUDY_ID;
    val randomGender = randomGenerator.randomElement(newArrayList(DONOR_GENDER));
    val randomDonorSubmitterId = randomGenerator.generateRandomUUIDAsString();
    val expectedId = idServiceOLD.generateDonorId(randomDonorSubmitterId, studyId);

    val donorWithSpecimens = new DonorWithSpecimens();
    donorWithSpecimens.setStudyId(studyId);
    donorWithSpecimens.setDonorSubmitterId(randomDonorSubmitterId);
    donorWithSpecimens.setDonorGender(randomGender);
    donorWithSpecimens.setInfo("someKey", "someValue");
    val donorId = service.create(donorWithSpecimens);
    assertEquals(donorId, expectedId);

    donorWithSpecimens.setDonorId("DO123");
    SongErrorAssertions.assertSongError(
        () -> service.create(donorWithSpecimens), DONOR_ID_IS_CORRUPTED);

    donorWithSpecimens.setDonorId(expectedId);
    SongErrorAssertions.assertSongError(
        () -> service.create(donorWithSpecimens), DONOR_ALREADY_EXISTS);

    donorWithSpecimens.setDonorId("");
    SongErrorAssertions.assertSongError(
        () -> service.create(donorWithSpecimens), DONOR_ALREADY_EXISTS);

    donorWithSpecimens.setDonorId(null);
    SongErrorAssertions.assertSongError(
        () -> service.create(donorWithSpecimens), DONOR_ALREADY_EXISTS);
  }

  @Test
  public void testUpdateDonorDNE() {
    val randomDonor = createRandomDonor();
    SongErrorAssertions.assertSongError(() -> service.update(randomDonor), DONOR_DOES_NOT_EXIST);
  }

  @Test
  public void testReadDonorDNE() {
    val data = secureDonorTester.generateData();
    SongErrorAssertions.assertSongError(
        () -> service.unsecuredRead(data.getNonExistingId()), DONOR_DOES_NOT_EXIST);
    SongErrorAssertions.assertSongError(
        () -> service.readWithSpecimens(data.getNonExistingId()), DONOR_DOES_NOT_EXIST);
  }

  @Test
  @Transactional
  public void testCheckDonorUnRelatedToStudy() {
    secureDonorTester.runSecureAnalysisTest((s, d) -> service.checkDonorAndStudyRelated(s, d));
    secureDonorTester.runSecureAnalysisTest((s, d) -> service.securedRead(s, d));
    secureDonorTester.runSecureAnalysisTest((s, d) -> service.securedDelete(s, d));
    secureDonorTester.runSecureAnalysisTest((s, d) -> service.securedDelete(s, newArrayList(d)));
  }

  private Donor createRandomDonor() {
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
