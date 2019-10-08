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

import static bio.overture.song.core.exceptions.ServerErrors.INFO_ALREADY_EXISTS;
import static bio.overture.song.core.exceptions.ServerErrors.INFO_NOT_FOUND;
import static bio.overture.song.core.utils.RandomGenerator.createRandomGenerator;
import static org.icgc.dcc.common.core.json.JsonNodeBuilders.object;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

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

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
public class InfoServiceTest {
  @Autowired DonorInfoService infoService;

  private final RandomGenerator randomGenerator =
      createRandomGenerator(InfoServiceTest.class.getSimpleName());

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
    assertTrue(info1.isPresent());
    val json2 = JsonUtils.readTree(info1.get());
    assertEquals(json, json2);

    json.put("species", "human");
    val new_info = JsonUtils.nodeToJSON(json);

    infoService.update(id, new_info);
    val info2 = infoService.readInfo(id);
    assertTrue(info2.isPresent());
    val json3 = JsonUtils.readTree(info2.get());

    assertEquals(json3, json);
  }

  @Test
  public void testInfoExists() {
    val donorId = genDonorId();
    assertFalse(infoService.isInfoExist(donorId));
    infoService.create(donorId, getDummyInfo("someKey", "1234"));
    assertTrue(infoService.isInfoExist(donorId));

    // Also check that null info is properly handled when checking for existence
    val donorId2 = genDonorId();
    assertFalse(infoService.isInfoExist(donorId2));
    infoService.create(donorId2, null);
    assertTrue(infoService.isInfoExist(donorId2));
  }

  @Test
  public void testCreate() {
    val donorId = genDonorId();
    val expectedData = getDummyInfo("someKey", "234433rff");
    infoService.create(donorId, expectedData);
    val actualData = infoService.readInfo(donorId);
    assertTrue(actualData.isPresent());
    assertEquals(actualData.get(), expectedData);

    // Also check creation of null info fields
    val nonExistentDonorId2 = genDonorId();
    infoService.create(nonExistentDonorId2, null);
    assertFalse(infoService.readInfo(nonExistentDonorId2).isPresent());
  }

  @Test
  public void testCreateError() {
    val donorId = genDonorId();
    infoService.create(donorId, getDummyInfo("someKey", "234433rff"));
    val dd = getDummyInfo("someKey", "999999993333333333");
    SongErrorAssertions.assertSongErrorRunnable(
        () -> infoService.create(donorId, dd), INFO_ALREADY_EXISTS);
    SongErrorAssertions.assertSongErrorRunnable(
        () -> infoService.create(donorId, null), INFO_ALREADY_EXISTS);
  }

  @Test
  public void testReadInfoError() {
    val nonExistentDonorId = genDonorId();
    SongErrorAssertions.assertSongError(
        () -> infoService.readInfo(nonExistentDonorId), INFO_NOT_FOUND);
  }

  @Test
  public void testUpdateError() {
    val nonExistentDonorId = genDonorId();
    SongErrorAssertions.assertSongErrorRunnable(
        () -> infoService.update(nonExistentDonorId, getDummyInfo("someKey", "239dk")),
        INFO_NOT_FOUND);
    SongErrorAssertions.assertSongErrorRunnable(
        () -> infoService.update(nonExistentDonorId, null), INFO_NOT_FOUND);
  }

  @Test
  public void testDelete() {
    val donorId = genDonorId();
    infoService.create(donorId, getDummyInfo("someKey", "234433rff"));
    assertTrue(infoService.isInfoExist(donorId));
    infoService.delete(donorId);
    assertFalse(infoService.isInfoExist(donorId));

    // Also check when info is null
    val donorId2 = genDonorId();
    infoService.create(donorId2, null);
    assertTrue(infoService.isInfoExist(donorId2));
    infoService.delete(donorId2);
    assertFalse(infoService.isInfoExist(donorId2));
  }

  @Test
  public void testDeleteError() {
    val nonExistentDonorId = genDonorId();
    SongErrorAssertions.assertSongErrorRunnable(
        () -> infoService.delete(nonExistentDonorId), INFO_NOT_FOUND);
  }

  @Test
  public void testReadNullableInfo() {
    val donorId = genDonorId();
    infoService.create(donorId, null);
    assertTrue(infoService.isInfoExist(donorId));
    assertNull(infoService.readNullableInfo(donorId));

    val donorId2 = genDonorId();
    val info = getDummyInfo("someKey", "2idj94");
    infoService.create(donorId2, info);
    assertTrue(infoService.isInfoExist(donorId2));
    assertEquals(infoService.readNullableInfo(donorId2), info);

    val nonExistingDonorId = randomGenerator.generateRandomUUIDAsString();
    assertFalse(infoService.isInfoExist(nonExistingDonorId));
    assertNull(infoService.readNullableInfo(nonExistingDonorId));
  }

  private String genDonorId() {
    return randomGenerator.generateRandomAsciiString(25);
  }

  private static String getDummyInfo(String key, String value) {
    val out = object().with(key, value).end();
    return out.toString();
  }
}
