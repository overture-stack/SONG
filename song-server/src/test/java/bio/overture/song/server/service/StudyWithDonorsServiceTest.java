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

import static bio.overture.song.core.testing.SongErrorAssertions.assertCollectionsMatchExactly;
import static bio.overture.song.core.utils.RandomGenerator.createRandomGenerator;
import static bio.overture.song.server.utils.generator.AnalysisGenerator.createAnalysisGenerator;
import static bio.overture.song.server.utils.generator.StudyGenerator.createStudyGenerator;
import static java.util.stream.Collectors.toSet;
import static org.junit.Assert.assertEquals;

import bio.overture.song.core.utils.RandomGenerator;
import bio.overture.song.server.model.analysis.Analysis2;
import bio.overture.song.server.model.entity.Donor;
import bio.overture.song.server.model.entity.Sample;
import bio.overture.song.server.model.entity.Specimen;
import bio.overture.song.server.model.entity.composites.CompositeEntity;
import bio.overture.song.server.model.entity.composites.DonorWithSpecimens;
import bio.overture.song.server.model.entity.composites.SpecimenWithSamples;
import bio.overture.song.server.utils.generator.StudyGenerator;
import com.google.common.collect.Maps;
import java.util.Collection;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class})
@ActiveProfiles("test")
public class StudyWithDonorsServiceTest {

  @Autowired private StudyWithDonorsService studyWithDonorsService;

  @Autowired private StudyService studyService;

  @Autowired private AnalysisService2 analysisService;

  private final RandomGenerator randomGenerator =
      createRandomGenerator(StudyWithDonorsServiceTest.class.getSimpleName());

  private StudyGenerator studyGenerator;

  @Before
  public void init() {
    studyGenerator = createStudyGenerator(studyService, randomGenerator);
  }

  @Test
  public void testReadWithChildren() {
    // Create random isolated study
    val studyId = studyGenerator.createRandomStudy();

    // Generate Random SequencingRead analyses
    val analysisGenerator = createAnalysisGenerator(studyId, analysisService, randomGenerator);
    val numAnalysis = 11;
    val analysisMap = Maps.<String, Analysis2>newHashMap();
    for (int i = 0; i < numAnalysis; i++) {
      val sequencingReadAnalysis = analysisGenerator.createDefaultRandomSequencingReadAnalysis();
      analysisMap.put(sequencingReadAnalysis.getAnalysisId(), sequencingReadAnalysis);
    }

    // Extract expected donors and verify
    val expectedDonors =
        analysisMap.values().stream()
            .flatMap(x -> x.getSample().stream())
            .map(CompositeEntity::getDonor)
            .collect(toSet());
    assertEquals(expectedDonors.size(), numAnalysis);
    assertEquals(
        expectedDonors.stream().map(Donor::getDonorSubmitterId).distinct().count(), numAnalysis);
    assertEquals(
        expectedDonors.stream().filter(x -> x.getStudyId().equals(studyId)).count(), numAnalysis);

    // Extract expected specimens and verify
    val expectedSpecimens =
        analysisMap.values().stream()
            .flatMap(x -> x.getSample().stream())
            .map(CompositeEntity::getSpecimen)
            .collect(toSet());
    assertEquals(expectedSpecimens.size(), numAnalysis);
    assertEquals(
        expectedSpecimens.stream().map(Specimen::getSpecimenSubmitterId).distinct().count(),
        numAnalysis);

    // Extract expected samples and verify
    val expectedSamples =
        analysisMap.values().stream().flatMap(x -> x.getSample().stream()).collect(toSet());
    val expectedSampleSubmitterIds =
        expectedSamples.stream().map(Sample::getSampleSubmitterId).collect(toSet());
    assertEquals(expectedSamples.size(), numAnalysis);
    assertEquals(expectedSampleSubmitterIds.size(), numAnalysis);

    // Run the target method to test, readWithChildren
    val studyWithDonors = studyWithDonorsService.readWithChildren(studyId);

    // Extract actual donors
    val actualDonors =
        studyWithDonors.getDonors().stream().map(DonorWithSpecimens::createDonor).collect(toSet());

    // Extract actual specimens
    val actualSpecimens =
        studyWithDonors.getDonors().stream()
            .map(DonorWithSpecimens::getSpecimens)
            .flatMap(Collection::stream)
            .map(SpecimenWithSamples::getSpecimen)
            .collect(toSet());

    // Extract actual samples
    val actualSamples =
        studyWithDonors.getDonors().stream()
            .map(DonorWithSpecimens::getSpecimens)
            .flatMap(Collection::stream)
            .map(SpecimenWithSamples::getSamples)
            .flatMap(Collection::stream)
            .collect(toSet());
    val actualSampleSubmitterIds =
        actualSamples.stream().map(Sample::getSampleSubmitterId).collect(toSet());

    // Verify expected donors and actual donors match
    assertCollectionsMatchExactly(expectedDonors, actualDonors);

    // Verify expected specimens and actual specimens match
    assertCollectionsMatchExactly(expectedSpecimens, actualSpecimens);

    // Verify expected sampleSubmitterIds and actual sampleSubmitterIds match
    assertCollectionsMatchExactly(expectedSampleSubmitterIds, actualSampleSubmitterIds);
  }
}
