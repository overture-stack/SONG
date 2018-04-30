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

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.icgc.dcc.song.core.utils.RandomGenerator;
import org.icgc.dcc.song.server.model.entity.Study;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.STUDY_ALREADY_EXISTS;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.STUDY_ID_DOES_NOT_EXIST;
import static org.icgc.dcc.song.core.testing.SongErrorAssertions.assertSongError;
import static org.icgc.dcc.song.core.utils.RandomGenerator.createRandomGenerator;
import static org.icgc.dcc.song.server.utils.TestConstants.DEFAULT_STUDY_ID;
import static org.icgc.dcc.song.server.utils.TestFiles.getInfoName;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
//RTISMA_HACK @TestExecutionListeners({ DependencyInjectionTestExecutionListener.class})
@ActiveProfiles("dev")
public class StudyServiceTest {

  @Autowired
  StudyService service;

  private final RandomGenerator randomGenerator = createRandomGenerator(StudyServiceTest.class.getSimpleName());

  @Test
  public void testReadStudy() {
    // check for data that we know exists in the database already
    val study = service.read(DEFAULT_STUDY_ID);
    assertThat(study).isNotNull();
    assertThat(study.getStudyId()).isEqualTo("ABC123");
    assertThat(study.getName()).isEqualTo("X1-CA");
    assertThat(study.getDescription()).isEqualTo("A fictional study");
    assertThat(study.getOrganization()).isEqualTo("Sample Data Research Institute");
    assertThat(getInfoName(study)).isEqualTo("study1");
  }

  @Test
  public void testSave(){
    val studyId = randomGenerator.generateRandomUUID().toString();
    val organization = randomGenerator.generateRandomUUID().toString();
    val name  = randomGenerator.generateRandomAsciiString(10);
    val description = randomGenerator.generateRandomUUID().toString();
    val study = Study.builder()
        .studyId(studyId)
        .name(name)
        .description(description)
        .organization(organization)
        .build();
    assertThat(service.isStudyExist(studyId)).isFalse();
    service.saveStudy(study);
    val readStudy = service.read(studyId);
    assertThat(readStudy).isEqualToComparingFieldByFieldRecursively(study);
  }

  @Test
  public void testFindAllStudies(){
    val studyIds = service.findAllStudies();
    assertThat(studyIds).contains(DEFAULT_STUDY_ID, "XYZ234");
    val study = Study.builder()
        .studyId(randomGenerator.generateRandomUUIDAsString())
        .name( randomGenerator.generateRandomUUIDAsString())
        .organization(randomGenerator.generateRandomUUIDAsString())
        .description(randomGenerator.generateRandomUUIDAsString())
        .build();

    service.saveStudy(study);
    val studyIds2 = service.findAllStudies();
    assertThat(studyIds2).contains(DEFAULT_STUDY_ID, "XYZ234", study.getStudyId());
  }

  @Test
  public void testDuplicateSaveStudyError(){
    val existentStudyId = DEFAULT_STUDY_ID;
    assertThat(service.isStudyExist(existentStudyId)).isTrue();
    val study = service.read(existentStudyId);
    assertSongError(() -> service.saveStudy(study), STUDY_ALREADY_EXISTS);
  }

  @Test
  public void testReadStudyError(){
    val nonExistentStudyId = genStudyId();
    assertSongError(() -> service.read(nonExistentStudyId), STUDY_ID_DOES_NOT_EXIST);
  }

  @Test
  public void testStudyCheck(){
    val existentStudyId = DEFAULT_STUDY_ID;
    assertThat(service.isStudyExist(existentStudyId)).isTrue();
    val nonExistentStudyId = genStudyId();
    assertThat(service.isStudyExist(nonExistentStudyId)).isFalse();
  }

  private String genStudyId(){
    return randomGenerator.generateRandomAsciiString(15);
  }

}
