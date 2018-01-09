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

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.icgc.dcc.song.server.model.LegacyEntity;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class})
@ActiveProfiles("dev")
public class LegacyEntityServiceTest {

  @Autowired
  private LegacyEntityService legacyEntityService;

  @Test
  public void testGetLegacyEntityByGnosId() {
    val analysisId = "AN1";
    val gnosId = analysisId;
    val entities = legacyEntityService.getEntitiesByGnosId(gnosId,2000, 0).getContent();
    assertThat(entities).hasSize(2);
    LegacyEntity entity1;
    LegacyEntity entity2;
    if (entities.get(0).getId().equals("FI1")){
      entity1 = entities.get(0);
      entity2 = entities.get(1);
    } else {
      entity2 = entities.get(0);
      entity1 = entities.get(1);
    }

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
