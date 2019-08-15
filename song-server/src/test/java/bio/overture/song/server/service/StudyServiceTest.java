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

import static bio.overture.song.core.exceptions.ServerErrors.STUDY_ALREADY_EXISTS;
import static bio.overture.song.core.exceptions.ServerErrors.STUDY_ID_DOES_NOT_EXIST;
import static bio.overture.song.core.utils.RandomGenerator.createRandomGenerator;
import static bio.overture.song.server.utils.TestConstants.DEFAULT_STUDY_ID;
import static bio.overture.song.server.utils.TestFiles.getInfoName;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import bio.overture.song.core.testing.SongErrorAssertions;
import bio.overture.song.core.utils.RandomGenerator;
import bio.overture.song.server.model.entity.Study;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
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
public class StudyServiceTest {

  @Autowired StudyService service;

  private final RandomGenerator randomGenerator =
      createRandomGenerator(StudyServiceTest.class.getSimpleName());

  @Test
  public void testReadStudy() {
    // check for data that we know exists in the database already
    val study = service.read(DEFAULT_STUDY_ID);
    assertNotNull(study);
    assertEquals(study.getStudyId(), "ABC123");
    assertEquals(study.getName(), "X1-CA");
    assertEquals(study.getDescription(), "A fictional study");
    assertEquals(study.getOrganization(), "Sample Data Research Institute");
    assertEquals(getInfoName(study), "study1");
  }

  @Test
  public void testSave() {
    val studyId = randomGenerator.generateRandomUUID().toString();
    val organization = randomGenerator.generateRandomUUID().toString();
    val name = randomGenerator.generateRandomAsciiString(10);
    val description = randomGenerator.generateRandomUUID().toString();
    val study =
        Study.builder()
            .studyId(studyId)
            .name(name)
            .description(description)
            .organization(organization)
            .build();
    assertFalse(service.isStudyExist(studyId));
    service.saveStudy(study);
    val readStudy = service.read(studyId);
    assertEquals(readStudy, study);
  }

  @Test
  public void testFindAllStudies() {
    val studyIds = service.findAllStudies();
    assertThat(studyIds, hasItems(DEFAULT_STUDY_ID, "XYZ234"));
    val study =
        Study.builder()
            .studyId(randomGenerator.generateRandomUUIDAsString())
            .name(randomGenerator.generateRandomUUIDAsString())
            .organization(randomGenerator.generateRandomUUIDAsString())
            .description(randomGenerator.generateRandomUUIDAsString())
            .build();

    service.saveStudy(study);
    val studyIds2 = service.findAllStudies();
    assertThat(studyIds2, hasItems(DEFAULT_STUDY_ID, "XYZ234", study.getStudyId()));
  }

  @Test
  public void testDuplicateSaveStudyError() {
    val existentStudyId = DEFAULT_STUDY_ID;
    assertTrue(service.isStudyExist(existentStudyId));
    val study = service.read(existentStudyId);
    SongErrorAssertions.assertSongError(() -> service.saveStudy(study), STUDY_ALREADY_EXISTS);
  }

  @Test
  public void testReadStudyError() {
    val nonExistentStudyId = genStudyId();
    SongErrorAssertions.assertSongError(
        () -> service.read(nonExistentStudyId), STUDY_ID_DOES_NOT_EXIST);
  }

  @Test
  public void testStudyCheck() {
    val existentStudyId = DEFAULT_STUDY_ID;
    assertTrue(service.isStudyExist(existentStudyId));
    val nonExistentStudyId = genStudyId();
    assertFalse(service.isStudyExist(nonExistentStudyId));
  }

  private String genStudyId() {
    return randomGenerator.generateRandomAsciiString(15);
  }
}
