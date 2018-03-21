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
import com.google.common.collect.Sets;
import lombok.val;
import org.assertj.core.api.Assertions;
import org.icgc.dcc.song.core.utils.JsonUtils;
import org.icgc.dcc.song.core.utils.RandomGenerator;
import org.icgc.dcc.song.server.model.entity.Donor;
import org.icgc.dcc.song.server.model.entity.Sample;
import org.icgc.dcc.song.server.model.entity.Specimen;
import org.icgc.dcc.song.server.model.entity.composites.DonorWithSpecimens;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import java.util.Set;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.SPECIMEN_ALREADY_EXISTS;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.SPECIMEN_DOES_NOT_EXIST;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.SPECIMEN_ID_IS_CORRUPTED;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.STUDY_ID_DOES_NOT_EXIST;
import static org.icgc.dcc.song.core.testing.SongErrorAssertions.assertSongError;
import static org.icgc.dcc.song.core.utils.RandomGenerator.createRandomGenerator;
import static org.icgc.dcc.song.server.model.enums.Constants.DONOR_GENDER;
import static org.icgc.dcc.song.server.model.enums.Constants.SAMPLE_TYPE;
import static org.icgc.dcc.song.server.model.enums.Constants.SPECIMEN_CLASS;
import static org.icgc.dcc.song.server.model.enums.Constants.SPECIMEN_TYPE;
import static org.icgc.dcc.song.server.utils.TestConstants.DEFAULT_DONOR_ID;
import static org.icgc.dcc.song.server.utils.TestConstants.DEFAULT_SPECIMEN_ID;
import static org.icgc.dcc.song.server.utils.TestConstants.DEFAULT_STUDY_ID;
import static org.icgc.dcc.song.server.utils.TestFiles.getInfoName;

@SpringBootTest
@RunWith(SpringRunner.class)
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class})
@ActiveProfiles("dev")
public class SpecimenServiceTest {

    @Autowired
    SpecimenService specimenService;
    @Autowired
    SampleService sampleService;
    @Autowired
    StudyService studyService;
    @Autowired
    DonorService donorService;

    private final RandomGenerator randomGenerator = createRandomGenerator(SpecimenServiceTest.class.getSimpleName());

    @Before
    public void beforeTest(){
        assertThat(studyService.isStudyExist(DEFAULT_STUDY_ID)).isTrue();
    }

    @Test
    public void testReadSpecimen() {
        // find existing specimen in the database
        val id = "SP1";
        val s = specimenService.read(id);
        assertThat(s.getSpecimenId()).isEqualTo(id);
        assertThat(s.getSpecimenSubmitterId()).isEqualTo("Tissue-Culture 284 Gamma 3");
        assertThat(s.getSpecimenClass()).isEqualTo("Tumour");
        assertThat(s.getSpecimenType()).isEqualTo("Recurrent tumour - solid tissue");
        assertThat(getInfoName(s)).isEqualTo("specimen1");
    }

    @Test
    public void testReadWithSamples() {
        // see if we can read a composite object successfully
        val id = "SP1";
        val specimen = specimenService.readWithSamples(id);
        assertThat(specimen.getSpecimenId()).isEqualTo(id);
        assertThat(specimen.getSpecimenSubmitterId()).isEqualTo("Tissue-Culture 284 Gamma 3");
        assertThat(specimen.getSpecimenClass()).isEqualTo("Tumour");
        assertThat(specimen.getSpecimenType()).isEqualTo("Recurrent tumour - solid tissue");
        assertThat(specimen.getSamples().size()).isEqualTo(2);
        assertThat(getInfoName(specimen)).isEqualTo("specimen1");

        // Verify that we got the same samples as the sample service says we should.
        specimen.getSamples().forEach(sample -> assertThat(sample.equals(getSample(sample.getSampleId()))));
    }

    private Sample getSample(String id) {
        return sampleService.read(id);
    }

    @Test
    public void testCreateAndDeleteSpecimen() {
        val donorId = "DO2";
        Specimen s = Specimen.create("", "Specimen 101 Ipsilon Prime", donorId, "Tumour",
                "Cell line - derived from tumour");
        s.setInfo(JsonUtils.fromSingleQuoted("{'ageCategory': 42, 'status': 'deceased'}"));

        val status = specimenService.create(DEFAULT_STUDY_ID, s);
        val id = s.getSpecimenId();

        assertThat(id).startsWith("SP");
        Assertions.assertThat(status).isEqualTo(id);

        val check = specimenService.read(id);
        assertThat(s).isEqualToComparingFieldByField(check);

        val response = specimenService.delete(newArrayList(id));
        assertThat(specimenService.isSpecimenExist(id)).isFalse();
        assertThat(response).isEqualTo("OK");
    }

    @Test
    public void testUpdateSpecimen() {
        val donorId = "DO2";
        val s = Specimen.create("", "Specimen 102 Chiron-Beta Prime", donorId, "Tumour",
                "Metastatic tumour - additional metastatic");

        specimenService.create(DEFAULT_STUDY_ID, s);

        val id = s.getSpecimenId();

        val s2 = Specimen.create(id, "Specimen 102", s.getDonorId(), "Normal", "Normal - other");
        s2.setInfo(JsonUtils.fromSingleQuoted("{'notes': ['A sharp, B flat']}"));
        specimenService.update(s2);

        val s3 = specimenService.read(id);
        Assertions.assertThat(s3).isEqualToComparingFieldByField(s2);
    }

    @Test
    public void testSpecimenExists(){
        val existingSpecimenId= DEFAULT_SPECIMEN_ID;
        assertThat(specimenService.isSpecimenExist(existingSpecimenId)).isTrue();
        specimenService.checkSpecimenExist(existingSpecimenId);
        val nonExistingSpecimenId = randomGenerator.generateRandomUUIDAsString();
        assertThat(specimenService.isSpecimenExist(nonExistingSpecimenId)).isFalse();
        specimenService.checkSpecimenExist(existingSpecimenId);
        specimenService.checkSpecimenDoesNotExist(nonExistingSpecimenId);

        assertSongError(() -> specimenService.checkSpecimenExist(nonExistingSpecimenId), SPECIMEN_DOES_NOT_EXIST);
        assertSongError(() -> specimenService.checkSpecimenDoesNotExist(existingSpecimenId), SPECIMEN_ALREADY_EXISTS);
    }

    @Test
    public void testCreateStudyDNE(){
        val randomStudyId = randomGenerator.generateRandomUUIDAsString();
        val specimen = new Specimen();
        assertSongError(() -> specimenService.create(randomStudyId, specimen), STUDY_ID_DOES_NOT_EXIST);
    }

    @Test
    public void testCreateCorruptionAndAlreadyExistsErrors(){
        val donorId = DEFAULT_DONOR_ID;
        val existingStudyId = DEFAULT_STUDY_ID;

        val specimen = new Specimen();
        specimen.setSpecimenType(randomGenerator.randomElement(newArrayList(SPECIMEN_TYPE)));
        specimen.setSpecimenSubmitterId(randomGenerator.generateRandomUUIDAsString());
        specimen.setSpecimenClass(randomGenerator.randomElement(newArrayList(SPECIMEN_CLASS)));
        specimen.setDonorId(donorId);

        // Create a specimen
        val specimenId = specimenService.create(existingStudyId, specimen);
        assertThat(specimenService.isSpecimenExist(specimenId)).isTrue();

        // Try to create the specimen again, and assert that the right exception is thrown
        assertSongError(() -> specimenService.create(existingStudyId, specimen), SPECIMEN_ALREADY_EXISTS);

        // 'Accidentally' set the specimenId to something not generated by the idService, and try to create. Should
        // detected the corrupted id field, indicating user might have accidentally set the id, thinking it would be
        // persisted
        val specimen2 = new Specimen();
        specimen2.setSpecimenType(randomGenerator.randomElement(newArrayList(SPECIMEN_TYPE)));
        specimen2.setSpecimenSubmitterId(randomGenerator.generateRandomUUIDAsString());
        specimen2.setSpecimenClass(randomGenerator.randomElement(newArrayList(SPECIMEN_CLASS)));
        specimen2.setDonorId(donorId);
        specimen2.setSpecimenId(randomGenerator.generateRandomUUIDAsString());
        assertThat(specimenService.isSpecimenExist(specimen2.getSpecimenId())).isFalse();
        assertSongError(() -> specimenService.create(existingStudyId, specimen2), SPECIMEN_ID_IS_CORRUPTED);
    }

    @Test
    public void testReadSpecimenDNE(){
        val randomSpecimenId = randomGenerator.generateRandomUUIDAsString();
        assertThat(specimenService.isSpecimenExist(randomSpecimenId)).isFalse();
        assertSongError(() -> specimenService.read(randomSpecimenId), SPECIMEN_DOES_NOT_EXIST);
        assertSongError(() -> specimenService.readWithSamples(randomSpecimenId), SPECIMEN_DOES_NOT_EXIST);
    }

    @Test
    public void testReadAndDeleteByParentId(){
        // Create a donor, and then several specimens, and for each specimen 2 samples
        val studyId = DEFAULT_STUDY_ID;
        val donor = Donor.create("", randomGenerator.generateRandomUUIDAsString(),
            studyId, randomGenerator.randomElement(newArrayList(DONOR_GENDER)));
        val donorWithSpecimens = new DonorWithSpecimens();
        donorWithSpecimens.setDonor(donor);
        val donorId = donorService.create(donorWithSpecimens);

        val numSpecimens = 5;
        val numSamplesPerSpecimen = 2;
        val expectedSpecimenIds = Sets.<String>newHashSet();
        val expectedSampleIdMap = Maps.<String, Set<String>>newHashMap();
        for(int i=0; i<numSpecimens; i++){
            // Create specimen
            val specimen = Specimen.create("",
                randomGenerator.generateRandomUUIDAsString(),
                donorId,
                randomGenerator.randomElement(newArrayList(SPECIMEN_CLASS)),
                randomGenerator.randomElement(newArrayList(SPECIMEN_TYPE)));
            val specimenId = specimenService.create(studyId,specimen);
            expectedSpecimenIds.add(specimenId);

            //Create samples
            for (int j=0; j<numSamplesPerSpecimen; j++){
                val sample = Sample.create("",
                    randomGenerator.generateRandomUUIDAsString(),
                    specimenId,
                    randomGenerator.randomElement(newArrayList(SAMPLE_TYPE)));
                val sampleId = sampleService.create(studyId, sample);

                // Store the expected sampleId
                if (!expectedSampleIdMap.containsKey(specimenId)){
                    expectedSampleIdMap.put(specimenId, newHashSet());
                }
                val sampleIds = expectedSampleIdMap.get(specimenId);
                sampleIds.add(sampleId);
            }
        }

        // ReadByParentId (newly created donorId)
        val specimens = specimenService.readByParentId(donorId);
        assertThat(specimens).hasSize(numSpecimens);
        for(val specimen :  specimens){
            val actualSpecimenId = specimen.getSpecimenId();
            val actualSampleIds = specimen.getSamples().stream().map(Sample::getSampleId).collect(toSet());
            assertThat(expectedSpecimenIds).contains(actualSpecimenId);
            assertThat(actualSampleIds).hasSize(numSamplesPerSpecimen);
            val expectedSampleIds = expectedSampleIdMap.get(actualSpecimenId);
            assertThat(expectedSampleIds).hasSize(numSamplesPerSpecimen);
            assertThat(actualSampleIds).isSubsetOf(expectedSampleIds);
            assertThat(expectedSampleIds).isSubsetOf(actualSampleIds);
        }


        // Assert that reading by a non-existent donorId returns something empty

        val randomDonorId = randomGenerator.generateRandomUUIDAsString();
        assertThat(donorService.isDonorExist(randomDonorId)).isFalse();
        val emptySpecimenList = specimenService.readByParentId(randomDonorId);
        assertThat(emptySpecimenList).isEmpty();

        // Delete by parent id
      val response = specimenService.deleteByParentId(donorId);
      assertThat(response).isEqualTo("OK");
      val emptySpecimenList2 = specimenService.readByParentId(donorId);
      assertThat(emptySpecimenList2).isEmpty();
    }

    @Test
    public void testDeleteSpecimenDNE(){
        val randomSpecimenId = randomGenerator.generateRandomUUIDAsString();
        assertSongError(() -> specimenService.delete(randomSpecimenId), SPECIMEN_DOES_NOT_EXIST);
        assertSongError(() -> specimenService.delete(newArrayList(randomSpecimenId)), SPECIMEN_DOES_NOT_EXIST);
    }

}
