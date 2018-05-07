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
import org.icgc.dcc.song.server.model.LegacyEntity;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles("dev")
public class LegacyEntityServiceTest {

  @Autowired
  private LegacyEntityService legacyEntityService;

  @Test
  public void testGetLegacyEntityByGnosId() {
    val analysisId = "AN1";
    val gnosId = analysisId;
    val entities = legacyEntityService.getEntitiesByGnosId(gnosId,2000, 0).getContent();
    assertThat(entities.size()).isGreaterThanOrEqualTo(2);
    val opt1 = entities.stream().filter(x -> x.getId().equals("FI1")).findFirst();
    val opt2 = entities.stream().filter(x -> x.getId().equals("FI2")).findFirst();
    assertThat(opt1.isPresent()).isTrue();
    assertThat(opt2.isPresent()).isTrue();
    LegacyEntity entity1 = opt1.get();
    LegacyEntity entity2 = opt2.get();

    assertThat(entity1.getAccess()).isEqualTo("open");
    assertThat(entity1.getFileName()).isEqualTo("ABC-TC285G7-A5-ae3458712345.bam");
    assertThat(entity1.getGnosId()).isEqualTo("AN1");
    assertThat(entity1.getId()).isEqualTo("FI1");
    assertThat(entity1.getProjectCode()).isEqualTo("ABC123");

    assertThat(entity2.getAccess()).isEqualTo("controlled");
    assertThat(entity2.getFileName()).isEqualTo("ABC-TC285G7-A5-wleazprt453.bai");
    assertThat(entity2.getGnosId()).isEqualTo("AN1");
    assertThat(entity2.getId()).isEqualTo("FI2");
    assertThat(entity2.getProjectCode()).isEqualTo("ABC123");

  }

  @Test
  public void testGetLegacyEntityByFileId() {
    val fileId1 = "FI1";
    val entity1 = legacyEntityService.getEntity(fileId1);
    assertThat(entity1.getAccess()).isEqualTo("open");
    assertThat(entity1.getFileName()).isEqualTo("ABC-TC285G7-A5-ae3458712345.bam");
    assertThat(entity1.getGnosId()).isEqualTo("AN1");
    assertThat(entity1.getId()).isEqualTo(fileId1);
    assertThat(entity1.getProjectCode()).isEqualTo("ABC123");

    val fileId2 = "FI2";
    val entity2 = legacyEntityService.getEntity(fileId2);
    assertThat(entity2.getAccess()).isEqualTo("controlled");
    assertThat(entity2.getFileName()).isEqualTo("ABC-TC285G7-A5-wleazprt453.bai");
    assertThat(entity2.getGnosId()).isEqualTo("AN1");
    assertThat(entity2.getId()).isEqualTo(fileId2);
    assertThat(entity2.getProjectCode()).isEqualTo("ABC123");

  }

}
