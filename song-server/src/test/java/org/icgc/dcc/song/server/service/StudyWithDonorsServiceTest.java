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

import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.icgc.dcc.song.core.utils.RandomGenerator;
import org.icgc.dcc.song.server.model.analysis.SequencingReadAnalysis;
import org.icgc.dcc.song.server.model.entity.Donor;
import org.icgc.dcc.song.server.model.entity.Sample;
import org.icgc.dcc.song.server.model.entity.Specimen;
import org.icgc.dcc.song.server.model.entity.composites.CompositeEntity;
import org.icgc.dcc.song.server.model.entity.composites.DonorWithSpecimens;
import org.icgc.dcc.song.server.model.entity.composites.SpecimenWithSamples;
import org.icgc.dcc.song.server.utils.StudyGenerator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import java.util.Collection;

import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.icgc.dcc.song.core.utils.RandomGenerator.createRandomGenerator;
import static org.icgc.dcc.song.server.utils.AnalysisGenerator.createAnalysisGenerator;
import static org.icgc.dcc.song.server.utils.StudyGenerator.createStudyGenerator;
import static org.icgc.dcc.song.server.utils.TestFiles.assertSetsMatch;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class})
@ActiveProfiles("dev")
public class StudyWithDonorsServiceTest {

  @Autowired
  private StudyWithDonorsService studyWithDonorsService;

  @Autowired
  private StudyService studyService;

  @Autowired
  private AnalysisService analysisService;

  private final RandomGenerator randomGenerator =
      createRandomGenerator(StudyWithDonorsServiceTest.class.getSimpleName());

  private StudyGenerator studyGenerator;

  @Before
  public void init(){
    studyGenerator = createStudyGenerator(studyService, randomGenerator);
  }

  @Test
  public void testReadWithChildren(){
    // Create random isolated study
    val studyId = studyGenerator.createRandomStudy();

    // Generate Random SequencingRead analyses
    val analysisGenerator = createAnalysisGenerator(studyId, analysisService, randomGenerator);
    val numAnalysis = 11;
    val analysisMap = Maps.<String, SequencingReadAnalysis>newHashMap();
    for (int i=0; i<numAnalysis; i++){
      val sequencingReadAnalysis = analysisGenerator.createDefaultRandomSequencingReadAnalysis();
      analysisMap.put(sequencingReadAnalysis.getAnalysisId(), sequencingReadAnalysis);
    }

    // Extract expected donors and verify
    val expectedDonors = analysisMap.values().stream()
        .flatMap(x -> x.getSample().stream())
        .map(CompositeEntity::getDonor)
        .collect(toSet());
    assertThat(expectedDonors).hasSize(numAnalysis);
    assertThat(expectedDonors.stream().map(Donor::getDonorSubmitterId).distinct().count()).isEqualTo(numAnalysis);
    assertThat(expectedDonors.stream().filter(x -> x.getStudyId().equals(studyId)).count()).isEqualTo(numAnalysis);

    // Extract expected specimens and verify
    val expectedSpecimens = analysisMap.values().stream()
        .flatMap(x -> x.getSample().stream())
        .map(CompositeEntity::getSpecimen)
        .collect(toSet());
    assertThat(expectedSpecimens).hasSize(numAnalysis);
    assertThat(expectedSpecimens.stream().map(Specimen::getSpecimenSubmitterId).distinct().count()).isEqualTo(numAnalysis);

    // Extract expected samples and verify
    val expectedSamples = analysisMap.values().stream()
        .flatMap(x -> x.getSample().stream())
        .collect(toSet());
    val expectedSampleSubmitterIds = expectedSamples.stream().map(Sample::getSampleSubmitterId).collect(toSet());
    assertThat(expectedSamples).hasSize(numAnalysis);
    assertThat(expectedSampleSubmitterIds).hasSize(numAnalysis);

    // Run the target method to test, readWithChildren
    val studyWithDonors = studyWithDonorsService.readWithChildren(studyId);

    // Extract actual donors
    val actualDonors = studyWithDonors.getDonors().stream()
        .map(DonorWithSpecimens::createDonor)
        .collect(toSet());

    // Extract actual specimens
    val actualSpecimens = studyWithDonors.getDonors().stream()
        .map(DonorWithSpecimens::getSpecimens)
        .flatMap(Collection::stream)
        .map(SpecimenWithSamples::getSpecimen)
        .collect(toSet());

    // Extract actual samples
    val actualSamples = studyWithDonors.getDonors().stream()
        .map(DonorWithSpecimens::getSpecimens)
        .flatMap(Collection::stream)
        .map(SpecimenWithSamples::getSamples)
        .flatMap(Collection::stream)
        .collect(toSet());
    val actualSampleSubmitterIds = actualSamples.stream().map(Sample::getSampleSubmitterId).collect(toSet());

    // Verify expected donors and actual donors match
    assertSetsMatch(expectedDonors, actualDonors);

    // Verify expected specimens and actual specimens match
    assertSetsMatch(expectedSpecimens, actualSpecimens);

    // Verify expected sampleSubmitterIds and actual sampleSubmitterIds match
    assertSetsMatch(expectedSampleSubmitterIds, actualSampleSubmitterIds);
  }

}
