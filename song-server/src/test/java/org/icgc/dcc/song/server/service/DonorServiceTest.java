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

import lombok.val;
import org.assertj.core.api.Assertions;
import org.flywaydb.test.annotation.FlywayTest;
import org.flywaydb.test.junit.FlywayTestExecutionListener;
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
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, FlywayTestExecutionListener.class })
@FlywayTest
@ActiveProfiles("dev")
public class DonorServiceTest {

  @Autowired
  DonorService service;
  @Autowired
  SpecimenService specimenService;

  @Test
  public void testReadDonor() {
    // check for data that we know exists in the H2 database already
    val d = service.readWithSpecimens("DO1");
    assertThat(d != null);
    assertThat(d.getDonorId()).isEqualTo("DO1");
    assertThat(d.getDonorGender()).isEqualTo("male");
    assertThat(d.getDonorSubmitterId()).isEqualTo("Subject-X23Alpha7");
    assertThat(d.getSpecimens().size()).isEqualTo(2);

    // Just check that each specimen object that we get is the same as the one we get from the
    // specimen service. Let the specimen service tests verify that the contents are right.
    d.getSpecimens().forEach(specimen -> assertThat(specimen.equals(getMatchingSpecimen(specimen))));

  }

  Specimen getMatchingSpecimen(Specimen specimen) {
    return specimenService.read(specimen.getSpecimenId());
  }

  @Test
  public void testCreateAndDeleteDonor() {
    val json = JsonUtils.mapper().createObjectNode();
    val studyId = "XYZ234";
    json.put("donorId", "");
    json.put("donorSubmitterId", "Subject X21-Alpha");
    json.put("studyId", studyId);
    json.put("donorGender", "unspecified");

    DonorWithSpecimens d = JsonUtils.mapper().convertValue(json, DonorWithSpecimens.class);
    assertThat(d.getDonorId()).isEqualTo("");

    val status = service.create(d);
    val id = d.getDonorId();

    assertThat(id).startsWith("DO");
    Assertions.assertThat(status).isEqualTo(id);

    DonorWithSpecimens check = service.readWithSpecimens(id);
    assertThat(d).isEqualToComparingFieldByField(check);

    service.delete("XYZ234", id);
    Donor check2 = service.read(id);
    assertThat(check2).isNull();
  }

  @Test
  public void testUpdateDonor() {
    val studyId = "ABC123";

    val d = new DonorWithSpecimens();
    d.setDonorId("");
    d.setDonorSubmitterId("Triangle-Arrow-S");
    d.setStudyId(studyId);
    d.setDonorGender("male");
    service.create(d);

    val id = d.getDonorId();

    val d2 = new Donor();
    d2.setDonorId(id);
    d2.setDonorSubmitterId("X21-Beta-17");
    d2.setStudyId(studyId);
    d2.setDonorGender("female");

    service.update(d2);

    val d3 = service.read(id);
    assertThat(d3).isEqualToComparingFieldByField(d2);
  }

}
