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
import org.icgc.dcc.song.server.model.entity.Sample;
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
public class SampleServiceTest {

  @Autowired
  SampleService sampleService;

  @Test
  public void testReadSample() {
    val id = "SA1";
    val sample = sampleService.read(id);
    Assertions.assertThat(sample.getSampleId()).isEqualTo(id);
    Assertions.assertThat(sample.getSampleSubmitterId()).isEqualTo("T285-G7-A5");
    Assertions.assertThat(sample.getSampleType()).isEqualTo("DNA");
  }

  @Test
  public void testCreateAndDeleteSample() {
    val specimenId = "";
    val metadata = "";
    val s = Sample.create("", "101-IP-A", specimenId, "Amplified DNA", metadata);

    val status = sampleService.create("SP2", s);
    val id = s.getSampleId();

    assertThat(id).startsWith("SA");
    assertThat(status).isEqualTo(id);

    Sample check = sampleService.read(id);
    assertThat(s).isEqualToComparingFieldByField(check);

    sampleService.delete(id);
    Sample check2 = sampleService.read(id);
    assertThat(check2).isNull();
  }

  @Test
  public void testUpdateSample() {
    val metadata = "";
    val specimenId = "";
    val s = Sample.create("", "102-CBP-A", specimenId, "RNA", metadata);

    sampleService.create("SP2", s);

    val id = s.getSampleId();

    val s2 = Sample.create(id, "Sample 102", s.getSpecimenId(), "FFPE RNA", metadata);

    sampleService.update(s2);

    val s3 = sampleService.read(id);
    Assertions.assertThat(s3).isEqualToComparingFieldByField(s2);
  }

}
