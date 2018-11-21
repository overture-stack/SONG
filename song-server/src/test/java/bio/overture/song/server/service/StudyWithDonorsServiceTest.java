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

import bio.overture.song.core.utils.RandomGenerator;
import bio.overture.song.server.model.analysis.SequencingReadAnalysis;
import bio.overture.song.server.model.entity.Donor;
import bio.overture.song.server.model.entity.Sample;
import bio.overture.song.server.model.entity.Specimen;
import bio.overture.song.server.model.entity.composites.CompositeEntity;
import bio.overture.song.server.model.entity.composites.DonorWithSpecimens;
import bio.overture.song.server.model.entity.composites.SpecimenWithSamples;
import bio.overture.song.server.utils.generator.StudyGenerator;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.assertj.core.api.Assertions;
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
import static bio.overture.song.core.utils.RandomGenerator.createRandomGenerator;
import static bio.overture.song.server.utils.generator.AnalysisGenerator.createAnalysisGenerator;
import static bio.overture.song.server.utils.TestFiles.assertSetsMatch;
import static bio.overture.song.server.utils.generator.StudyGenerator.createStudyGenerator;

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
    Assertions.assertThat(expectedSamples).hasSize(numAnalysis);
    Assertions.assertThat(expectedSampleSubmitterIds).hasSize(numAnalysis);

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
