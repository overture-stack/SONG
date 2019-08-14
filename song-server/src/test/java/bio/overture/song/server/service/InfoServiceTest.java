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
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.icgc.dcc.common.core.json.JsonNodeBuilders.object;
import static bio.overture.song.core.exceptions.ServerErrors.INFO_ALREADY_EXISTS;
import static bio.overture.song.core.exceptions.ServerErrors.INFO_NOT_FOUND;
import static bio.overture.song.core.testing.SongErrorAssertions.assertSongError;
import static bio.overture.song.core.utils.RandomGenerator.createRandomGenerator;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
public class InfoServiceTest {
  @Autowired
  DonorInfoService infoService;

  private final RandomGenerator randomGenerator = createRandomGenerator(InfoServiceTest.class.getSimpleName());

  @Test
  @SneakyThrows
  public void testInfo() {
    val json = JsonUtils.mapper().createObjectNode();
    val id = "DOX12345";

    json.put("ageCategory", "A");
    json.put("survivalStatus", "deceased");
    String info = JsonUtils.nodeToJSON(json);

    infoService.create(id, info);
    val info1 = infoService.readInfo(id);
    assertThat(info1.isPresent()).isTrue();
    val json2 = JsonUtils.readTree(info1.get());
    assertEquals(json,json2);

    json.put("species", "human");
    val new_info= JsonUtils.nodeToJSON(json);

    infoService.update(id, new_info);
    val info2 = infoService.readInfo(id);
    assertThat(info2.isPresent()).isTrue();
    val json3 = JsonUtils.readTree(info2.get());

    assertEquals(json3,json);
  }

  @Test
  public void testInfoExists(){
    val donorId = genDonorId();
    assertThat(infoService.isInfoExist(donorId)).isFalse();
    infoService.create(donorId, getDummyInfo("someKey", "1234"));
    assertThat(infoService.isInfoExist(donorId)).isTrue();

    // Also check that null info is properly handled when checking for existence
    val donorId2 = genDonorId();
    assertThat(infoService.isInfoExist(donorId2)).isFalse();
    infoService.create(donorId2, null);
    assertThat(infoService.isInfoExist(donorId2)).isTrue();
  }

  @Test
  public void testCreate(){
    val donorId = genDonorId();
    val expectedData = getDummyInfo("someKey", "234433rff");
    infoService.create(donorId, expectedData);
    val actualData = infoService.readInfo(donorId);
    assertThat(actualData.isPresent()).isTrue();
    assertEquals(actualData.get(),expectedData);

    // Also check creation of null info fields
    val nonExistentDonorId2 = genDonorId();
    infoService.create(nonExistentDonorId2, null);
    assertThat(infoService.readInfo(nonExistentDonorId2).isPresent()).isFalse();
  }

  @Test
  public void testCreateError(){
    val donorId = genDonorId();
    infoService.create(donorId, getDummyInfo("someKey", "234433rff"));
    val dd  = getDummyInfo("someKey", "999999993333333333");
    SongErrorAssertions.assertSongErrorRunnable(() -> infoService.create(donorId, dd), INFO_ALREADY_EXISTS);
    SongErrorAssertions.assertSongErrorRunnable(() -> infoService.create(donorId, null), INFO_ALREADY_EXISTS);
  }

  @Test
  public void testReadInfoError(){
    val nonExistentDonorId = genDonorId();
    SongErrorAssertions.assertSongError(() -> infoService.readInfo(nonExistentDonorId), INFO_NOT_FOUND);
  }

  @Test
  public void testUpdateError(){
    val nonExistentDonorId = genDonorId();
    SongErrorAssertions
        .assertSongErrorRunnable(() -> infoService.update(nonExistentDonorId, getDummyInfo("someKey", "239dk")), INFO_NOT_FOUND);
    SongErrorAssertions.assertSongErrorRunnable(() -> infoService.update(nonExistentDonorId, null), INFO_NOT_FOUND);
  }

  @Test
  public void testDelete(){
    val donorId = genDonorId();
    infoService.create(donorId, getDummyInfo("someKey", "234433rff"));
    assertThat(infoService.isInfoExist(donorId)).isTrue();
    infoService.delete(donorId);
    assertThat(infoService.isInfoExist(donorId)).isFalse();

    // Also check when info is null
    val donorId2 = genDonorId();
    infoService.create(donorId2, null);
    assertThat(infoService.isInfoExist(donorId2)).isTrue();
    infoService.delete(donorId2);
    assertThat(infoService.isInfoExist(donorId2)).isFalse();
  }

  @Test
  public void testDeleteError(){
    val nonExistentDonorId = genDonorId();
    SongErrorAssertions.assertSongErrorRunnable(() -> infoService.delete(nonExistentDonorId), INFO_NOT_FOUND);
  }

  @Test
  public void testReadNullableInfo(){
    val donorId = genDonorId();
    infoService.create(donorId, null);
    assertThat(infoService.isInfoExist(donorId)).isTrue();
    assertThat(infoService.readNullableInfo(donorId)).isNull();

    val donorId2 = genDonorId();
    val info = getDummyInfo("someKey", "2idj94");
    infoService.create(donorId2, info);
    assertThat(infoService.isInfoExist(donorId2)).isTrue();
    assertEquals(infoService.readNullableInfo(donorId2),info);

    val nonExistingDonorId = randomGenerator.generateRandomUUIDAsString();
    assertThat(infoService.isInfoExist(nonExistingDonorId)).isFalse();
    assertThat(infoService.readNullableInfo(nonExistingDonorId)).isNull();
  }

  private String genDonorId(){
    return randomGenerator.generateRandomAsciiString(25);
  }

  private static String getDummyInfo(String key, String value){
    val out = object()
        .with(key, value)
        .end();
    return out.toString();
  }

}
