/*
 * Copyright (c) 2017 The Ontario Institute for Cancer Research. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the GNU Public License v3.0.
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
 * SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER
 * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */
package org.icgc.dcc.song.server.service;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.icgc.dcc.song.core.utils.JsonUtils;
import org.icgc.dcc.song.server.utils.RandomGenerator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import static org.assertj.core.api.Assertions.assertThat;
import static org.icgc.dcc.common.core.json.JsonNodeBuilders.object;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.INFO_ALREADY_EXISTS;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.INFO_NOT_FOUND;
import static org.icgc.dcc.song.server.utils.ErrorTesting.assertSongError;
import static org.icgc.dcc.song.server.utils.RandomGenerator.createRandomGenerator;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class})
@ActiveProfiles("dev")
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
    assertThat(json).isEqualTo(json2);

    json.put("species", "human");
    val new_info= JsonUtils.nodeToJSON(json);

    infoService.update(id, new_info);
    val info2 = infoService.readInfo(id);
    assertThat(info2.isPresent()).isTrue();
    val json3 = JsonUtils.readTree(info2.get());

    assertThat(json3).isEqualTo(json);
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
    assertThat(actualData.get()).isEqualTo(expectedData);

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
    assertSongError(() -> infoService.create(donorId, dd), INFO_ALREADY_EXISTS);
    assertSongError(() -> infoService.create(donorId, null), INFO_ALREADY_EXISTS);
  }

  @Test
  public void testReadInfoError(){
    val nonExistentDonorId = genDonorId();
    assertSongError(() -> infoService.readInfo(nonExistentDonorId), INFO_NOT_FOUND);
  }

  @Test
  public void testUpdateError(){
    val nonExistentDonorId = genDonorId();
    assertSongError(() -> infoService.update(nonExistentDonorId, getDummyInfo("someKey", "239dk")), INFO_NOT_FOUND);
    assertSongError(() -> infoService.update(nonExistentDonorId, null), INFO_NOT_FOUND);
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
    assertSongError(() -> infoService.delete(nonExistentDonorId), INFO_NOT_FOUND);
  }

  @Test
  public void testReadNullableInfo(){
    val donorId = genDonorId();
    infoService.create(donorId, null);
    assertThat(infoService.readNullableInfo(donorId)).isNull();

    val donorId2 = genDonorId();
    val info = getDummyInfo("someKey", "2idj94");
    infoService.create(donorId2, info);
    assertThat(infoService.readNullableInfo(donorId2)).isEqualTo(info);
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
