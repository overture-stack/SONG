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

import com.google.common.collect.Sets;
import lombok.val;
import org.icgc.dcc.song.core.utils.JsonUtils;
import org.icgc.dcc.song.core.utils.RandomGenerator;
import org.icgc.dcc.song.server.model.entity.Sample;
import org.icgc.dcc.song.server.model.entity.Specimen;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.SAMPLE_ALREADY_EXISTS;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.SAMPLE_DOES_NOT_EXIST;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.SAMPLE_ID_IS_CORRUPTED;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.STUDY_ID_DOES_NOT_EXIST;
import static org.icgc.dcc.song.core.testing.SongErrorAssertions.assertSongError;
import static org.icgc.dcc.song.core.utils.RandomGenerator.createRandomGenerator;
import static org.icgc.dcc.song.server.model.enums.Constants.SAMPLE_TYPE;
import static org.icgc.dcc.song.server.model.enums.Constants.SPECIMEN_CLASS;
import static org.icgc.dcc.song.server.model.enums.Constants.SPECIMEN_TYPE;
import static org.icgc.dcc.song.server.utils.TestConstants.DEFAULT_DONOR_ID;
import static org.icgc.dcc.song.server.utils.TestConstants.DEFAULT_SAMPLE_ID;
import static org.icgc.dcc.song.server.utils.TestConstants.DEFAULT_SPECIMEN_ID;
import static org.icgc.dcc.song.server.utils.TestConstants.DEFAULT_STUDY_ID;
import static org.icgc.dcc.song.server.utils.TestFiles.getInfoName;

@SpringBootTest
@RunWith(SpringRunner.class)
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class })
@ActiveProfiles("dev")
public class SampleServiceTest {

  @Autowired
  SampleService sampleService;
  @Autowired
  StudyService studyService;
  @Autowired
  SpecimenService specimenService;

  private final RandomGenerator randomGenerator = createRandomGenerator(SampleServiceTest.class.getSimpleName());

  @Before
  public void beforeTest(){
    assertThat(studyService.isStudyExist(DEFAULT_STUDY_ID)).isTrue();
  }

  @Test
  public void testReadSample() {
    val id = "SA1";
    val sample = sampleService.read(id);
    assertThat(sample.getSampleId()).isEqualTo(id);
    assertThat(sample.getSampleSubmitterId()).isEqualTo("T285-G7-A5");
    assertThat(sample.getSampleType()).isEqualTo("DNA");
    assertThat(getInfoName(sample)).isEqualTo("sample1");
  }

  @Test
  public void testCreateAndDeleteSample() {
    val specimenId = "SP2";
    val metadata = JsonUtils.fromSingleQuoted("{'ageCategory': 3, 'species': 'human'}");
    val s = Sample.create("", "101-IP-A", specimenId, "Amplified DNA");
    s.setInfo(metadata);

    val status = sampleService.create(DEFAULT_STUDY_ID, s);
    val id = s.getSampleId();
    assertThat(sampleService.isSampleExist(id)).isTrue();

    assertThat(id).startsWith("SA");
    assertThat(status).isEqualTo(id);

    Sample check = sampleService.read(id);
    assertThat(check).isEqualToComparingFieldByField(s);

    sampleService.delete(newArrayList(id));
    assertThat(sampleService.isSampleExist(id)).isFalse();
  }

  @Test
  public void testUpdateSample() {

    val specimenId = "SP2";
    val s = Sample.create("", "102-CBP-A", specimenId, "RNA");

    sampleService.create(DEFAULT_STUDY_ID, s);

    val id = s.getSampleId();

    val metadata = JsonUtils.fromSingleQuoted("{'species': 'Canadian Beaver'}");
    val s2 = Sample.create(id, "Sample 102", s.getSpecimenId(), "FFPE RNA");
    s2.setInfo(metadata);
    sampleService.update(s2);

    val s3 = sampleService.read(id);
    assertThat(s3).isEqualToComparingFieldByField(s2);
  }

  @Test
  public void testCreateStudyDNE(){
    val randomStudyId = randomGenerator.generateRandomUUIDAsString();
    val sample = new Sample();
    assertSongError(() -> sampleService.create(randomStudyId, sample), STUDY_ID_DOES_NOT_EXIST);
  }

  @Test
  public void testCreateCorruptedAndAlreadyExists(){
    val specimenId = DEFAULT_SPECIMEN_ID;
    val existingStudyId = DEFAULT_STUDY_ID;

    val sample = new Sample();
    sample.setSampleSubmitterId(randomGenerator.generateRandomUUIDAsString());
    sample.setSampleType(randomGenerator.randomElement(newArrayList(SAMPLE_TYPE)));
    sample.setSpecimenId(specimenId);

    // Create a sample
    val sampleId = sampleService.create(existingStudyId, sample);
    assertThat(sampleService.isSampleExist(sampleId)).isTrue();

    // Try to create the sample again, and assert that the right exception is thrown
    assertSongError(() -> sampleService.create(existingStudyId, sample), SAMPLE_ALREADY_EXISTS);

    // 'Accidentally' set the sampleId to something not generated by the idService, and try to create. Should
    // detected the corrupted id field, indicating user might have accidentally set the id, thinking it would be
    // persisted
    val sample2 = new Sample();
    sample2.setSpecimenId(specimenId);
    sample2.setSampleType(randomGenerator.randomElement(newArrayList(SAMPLE_TYPE)));
    sample2.setSampleSubmitterId(randomGenerator.generateRandomUUIDAsString());
    sample2.setSampleId(randomGenerator.generateRandomUUIDAsString());
    assertThat(sampleService.isSampleExist(sample2.getSampleId())).isFalse();
    assertSongError(() -> sampleService.create(existingStudyId, sample2), SAMPLE_ID_IS_CORRUPTED);
  }

  @Test
  public void testSampleExists(){
    val existingSampleId= DEFAULT_SAMPLE_ID;
    assertThat(sampleService.isSampleExist(existingSampleId)).isTrue();
    sampleService.checkSampleExists(existingSampleId);
    val nonExistingSampleId = randomGenerator.generateRandomUUIDAsString();
    assertThat(sampleService.isSampleExist(nonExistingSampleId)).isFalse();
    sampleService.checkSampleExists(existingSampleId);
    sampleService.checkSampleDoesNotExist(nonExistingSampleId);

    assertSongError(() -> sampleService.checkSampleExists(nonExistingSampleId), SAMPLE_DOES_NOT_EXIST);
    assertSongError(() -> sampleService.checkSampleDoesNotExist(existingSampleId), SAMPLE_ALREADY_EXISTS);
  }

  @Test
  public void testReadSampleDNE(){
    val randomSampleId = randomGenerator.generateRandomUUIDAsString();
    assertThat(sampleService.isSampleExist(randomSampleId)).isFalse();
    assertSongError(() -> sampleService.read(randomSampleId), SAMPLE_DOES_NOT_EXIST);
  }

  @Test
  public void testReadAndDeleteByParentId(){
    val studyId = DEFAULT_STUDY_ID;
    val donorId = DEFAULT_DONOR_ID;
    val specimen = new Specimen();
    specimen.setDonorId(donorId);
    specimen.setSpecimenClass(randomGenerator.randomElement(newArrayList(SPECIMEN_CLASS)));
    specimen.setSpecimenType(randomGenerator.randomElement(newArrayList(SPECIMEN_TYPE)));
    specimen.setSpecimenSubmitterId(randomGenerator.generateRandomUUIDAsString());

    // Create specimen
    val specimenId = specimenService.create(studyId, specimen);
    specimen.setSpecimenId(specimenId);

    // Create samples
    val numSamples = 5;
    val expectedSampleIds = Sets.<String>newHashSet();
    for (int i =0; i< numSamples; i++){
      val sample = new Sample();
      sample.setSpecimenId(specimenId);
      sample.setSampleType(randomGenerator.randomElement(newArrayList(SAMPLE_TYPE)));
      sample.setSampleSubmitterId(randomGenerator.generateRandomUUIDAsString());
      val sampleId = sampleService.create(studyId, sample);
      expectedSampleIds.add(sampleId);
    }

    // Read the samples by parent Id (specimenId)
    val actualSamples = sampleService.readByParentId(specimenId);
    assertThat(actualSamples).hasSize(numSamples);
    assertThat(actualSamples.stream().map(Sample::getSampleId).collect(toSet())).containsAll(expectedSampleIds);

    // Assert that reading by a non-existent specimenId returns something empty
    val randomSpecimenId = randomGenerator.generateRandomUUIDAsString();
    assertThat(specimenService.isSpecimenExist(randomSpecimenId)).isFalse();
    val emptySampleList = sampleService.readByParentId(randomSpecimenId);
    assertThat(emptySampleList).isEmpty();

    // Delete by parent id
    val response = sampleService.deleteByParentId(specimenId);
    assertThat(response).isEqualTo("OK");
    val emptySampleList2 = sampleService.readByParentId(specimenId);
    assertThat(emptySampleList2).isEmpty();
  }

  @Test
  public void testDeleteSampleDNE(){
    val randomSpecimenId = randomGenerator.generateRandomUUIDAsString();
    assertSongError(() -> sampleService.delete(randomSpecimenId), SAMPLE_DOES_NOT_EXIST);
    assertSongError(() -> sampleService.delete(newArrayList(randomSpecimenId)), SAMPLE_DOES_NOT_EXIST);
  }

  @Test
  public void testUpdateSpecimenDNE(){
    val randomSampleId = randomGenerator.generateRandomUUIDAsString();
    val sample = new Sample();
    sample.setSampleSubmitterId(randomGenerator.generateRandomUUIDAsString());
    sample.setSampleId(randomSampleId);
    sample.setSampleType(randomGenerator.randomElement(newArrayList(SAMPLE_TYPE)));
    sample.setSpecimenId(DEFAULT_SPECIMEN_ID);
    assertSongError(() -> sampleService.update(sample), SAMPLE_DOES_NOT_EXIST);
  }

}
