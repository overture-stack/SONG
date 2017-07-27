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
import lombok.val;
import org.assertj.core.api.Assertions;
import org.icgc.dcc.song.core.utils.JsonUtils;
import org.icgc.dcc.song.server.model.entity.Donor;
import org.icgc.dcc.song.server.model.entity.Specimen;
import org.icgc.dcc.song.server.model.entity.composites.DonorWithSpecimens;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import static org.assertj.core.api.Assertions.assertThat;



@SpringBootTest
@RunWith(SpringRunner.class)
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class})
@ActiveProfiles("dev")
public class InfoServiceTest {
  @Autowired
  InfoService infoService;


  @Test
  @SneakyThrows
  public void testInfo() {
    val json = JsonUtils.mapper().createObjectNode();
    val id = "DOX12345";

    json.put("ageCategory", "A");
    json.put("survivalStatus", "deceased");

    String info = JsonUtils.nodeToJSON(json);

    val d = new Donor();
    d.setDonorId(id);
    d.setInfo(info);

    infoService.save(d,d.getDonorId(),"Donor");

    val d2 = new Donor();
    d2.setDonorId(id);
    infoService.setInfo(d2,d2.getDonorId(),"Donor");
    val json2 = JsonUtils.readTree(d2.getInfo());

    assertThat(json).isEqualTo(json2);

    json.put("species", "human");
    d.setInfo(JsonUtils.nodeToJSON(json));

    infoService.update(d,d.getDonorId(),"Donor");

    val d3 = new Donor();
    d3.setDonorId(id);
    infoService.setInfo(d3,d.getDonorId(),"Donor");
    val json3 = JsonUtils.readTree(d3.getInfo());

    assertThat(json3).isEqualTo(json);


  }


}
