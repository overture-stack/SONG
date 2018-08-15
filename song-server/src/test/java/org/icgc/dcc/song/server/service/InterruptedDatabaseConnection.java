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
import org.icgc.dcc.song.core.utils.RandomGenerator;
import org.icgc.dcc.song.server.model.entity.Study;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.icgc.dcc.song.core.utils.RandomGenerator.createRandomGenerator;
import static org.icgc.dcc.song.server.utils.TestConstants.DEFAULT_STUDY_ID;
import static org.icgc.dcc.song.server.utils.TestFiles.getInfoName;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class})
@ActiveProfiles({"dev","test"})
public class InterruptedDatabaseConnection {

  @Autowired
  StudyService service;
  private final RandomGenerator randomGenerator = createRandomGenerator(StudyServiceTest.class.getSimpleName());
  private final String postgres_data_directory="/users/khartmann/postgres";
  public void testThatServiceWorks() {
    // Random test copied from StudyServiceTest;
    // it gets data from postgres when the database is up.
    // Unfortunately, it doesn't return anything when it hangs...

    val study = service.read("ABC123");
    assertThat(study).isNotNull();
    assertThat(study.getStudyId()).isEqualTo("ABC123");
    assertThat(study.getName()).isEqualTo("X1-CA");
    assertThat(study.getDescription()).isEqualTo("A fictional study");
    assertThat(study.getOrganization()).isEqualTo("Sample Data Research Institute");
    assertThat(getInfoName(study)).isEqualTo("study1");
  }

  @Test
  public void testInterruptConnection() throws IOException, InterruptedException {
    // run a copy of a unit test that uses the database to get a study
    testThatServiceWorks();
    killDatabase();
    Thread.sleep(1000);
    // make sure our fails now that there's no database to connect to
    assertConnectionFails();
    // start postgres up again
    restoreDatabase();
    // make sure the test still works
    testThatServiceWorks();
    // run a second test that makes sure we aren't caching, and that
    // we're really connected to the database
    testThatServiceReallyWorks();
  }

  public void exec(String cmd) throws InterruptedException, IOException {
    val p = Runtime.getRuntime().exec(cmd);
    val exitStatus=p.waitFor();
    assertThat(exitStatus).isEqualTo(0);
  }

  public void killDatabase() throws IOException, InterruptedException {
    exec("/usr/bin/killall -INT postgres");
  }

  public void testThatServiceReallyWorks(){
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

  public void assertConnectionFails() {
    Exception ex = null;
    try {
      testThatServiceWorks();
    } catch (Exception e) {
      ex = e;
    }
    assertThat(ex).isNotNull();
  }

  public void restoreDatabase() throws IOException, InterruptedException {
    Runtime.getRuntime().exec("/usr/local/bin/postgres -D " + postgres_data_directory);
    Thread.sleep(500);
  }

}
