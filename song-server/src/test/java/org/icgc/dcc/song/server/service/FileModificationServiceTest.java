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
import org.icgc.dcc.song.server.model.entity.file.FileUpdateRequest;
import org.icgc.dcc.song.server.model.enums.AccessTypes;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.util.Lists.newArrayList;
import static org.icgc.dcc.common.core.json.JsonNodeBuilders.object;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.INVALID_FILE_UPDATE_REQUEST;
import static org.icgc.dcc.song.core.testing.SongErrorAssertions.assertSongError;
import static org.icgc.dcc.song.core.utils.RandomGenerator.createRandomGenerator;
import static org.icgc.dcc.song.server.model.enums.FileUpdateTypes.CONTENT_UPDATE;
import static org.icgc.dcc.song.server.model.enums.FileUpdateTypes.METADATA_UPDATE;
import static org.icgc.dcc.song.server.model.enums.FileUpdateTypes.NO_UPDATE;
import static org.icgc.dcc.song.server.service.FileModificationService.doUnpublish;
import static org.icgc.dcc.song.server.utils.TestConstants.DEFAULT_ANALYSIS_ID;
import static org.icgc.dcc.song.server.utils.TestConstants.DEFAULT_FILE_ID;
import static org.icgc.dcc.song.server.utils.TestConstants.DEFAULT_STUDY_ID;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles("dev")
public class FileModificationServiceTest {

  @Autowired
  FileService fileService;
  @Autowired
  StudyService studyService;
  @Autowired
  AnalysisService analysisService;

  @Autowired
  FileModificationService fileModificationService;

  private final RandomGenerator randomGenerator = createRandomGenerator(FileModificationServiceTest.class.getSimpleName());

  @Before
  public void beforeTest(){
    assertThat(studyService.isStudyExist(DEFAULT_STUDY_ID)).isTrue();
    assertThat(analysisService.isAnalysisExist(DEFAULT_ANALYSIS_ID)).isTrue();
    assertThat(fileService.isFileExist(DEFAULT_FILE_ID)).isTrue();
  }

  @Test
  public void testDoPublish(){
    assertThat(doUnpublish(NO_UPDATE)).isFalse();
    assertThat(doUnpublish(METADATA_UPDATE)).isFalse();
    assertThat(doUnpublish(CONTENT_UPDATE)).isTrue();
  }

  @Test
  public void testCheckFileUpdateRequestValidation(){
    val badSize_1  = FileUpdateRequest.builder().fileSize(-1L).build();
    val badSize_2  = FileUpdateRequest.builder().fileSize(0L).build();
    val badMd5_1 = FileUpdateRequest.builder().fileMd5sum("q123").build(); // less than 32 and non-hex number
    val badMd5_2 = FileUpdateRequest.builder().fileMd5sum("q0123456789012345678901234567890123456789").build(); //more than 32 and non-hex number
    val badAccess = FileUpdateRequest.builder().fileAccess("not_open_or_controlled").build();
    val badAll = FileUpdateRequest.builder()
        .fileAccess("not_open_or_controlled")
        .fileMd5sum("q123")
        .fileSize(0L)
        .info(object().with("something1", "value2").end())
        .build();

    val good = FileUpdateRequest.builder()
        .fileAccess(randomGenerator.randomEnum(AccessTypes.class).toString())
        .fileMd5sum(randomGenerator.generateRandomMD5())
        .fileSize((long)randomGenerator.generateRandomIntRange(1,100000))
        .info(object().with("someKey", "someValue").end())
        .build();
    val good2 = FileUpdateRequest.builder()
        .fileAccess(randomGenerator.randomEnum(AccessTypes.class).toString())
        .build();
    val good3 = FileUpdateRequest.builder()
        .fileMd5sum(randomGenerator.generateRandomMD5())
        .build();
    val good4 = FileUpdateRequest.builder()
        .fileSize((long)randomGenerator.generateRandomIntRange(1,100000))
        .build();
    val good5 = FileUpdateRequest.builder()
        .info(object().with("someKey", "someValue").end())
        .build();

    val goodRequests = newArrayList(good, good2, good3, good4, good5);
    val badRequests = newArrayList(badAccess, badMd5_1, badMd5_2, badSize_1, badSize_2, badAll);

    goodRequests
        .forEach(x -> fileModificationService.checkFileUpdateRequestValidation(DEFAULT_FILE_ID, x));
    badRequests
        .forEach(x ->{
          log.info("Processing bad request: {}", x);
          assertSongError(() -> fileModificationService.checkFileUpdateRequestValidation(DEFAULT_FILE_ID, x),
              INVALID_FILE_UPDATE_REQUEST, "Bad Request did not cause an error: %s" , x);
        });
  }


}
