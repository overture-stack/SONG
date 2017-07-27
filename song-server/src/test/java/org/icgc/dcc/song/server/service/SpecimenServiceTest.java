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
//import org.flywaydb.test.annotation.FlywayTest;
//import org.flywaydb.test.junit.FlywayTestExecutionListener;
import org.icgc.dcc.song.core.utils.JsonUtils;
import org.icgc.dcc.song.server.model.entity.Sample;
import org.icgc.dcc.song.server.model.entity.Specimen;
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
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class})
//@FlywayTest
@ActiveProfiles("dev")
public class SpecimenServiceTest {

    @Autowired
    SpecimenService specimenService;
    @Autowired
    SampleService sampleService;

    @Test
    public void testReadSpecimen2() {
        // find existing specimen in the database
        val id = "SP1";
        val s = specimenService.read(id);
        assertThat(s.getSpecimenId()).isEqualTo(id);
        assertThat(s.getSpecimenSubmitterId()).isEqualTo("Tissue-Culture 284 Gamma 3");
        assertThat(s.getSpecimenClass()).isEqualTo("Tumour");
        assertThat(s.getSpecimenType()).isEqualTo("Recurrent tumour - solid tissue");

    }

    @Test
    public void testReadSpecimen() {
        // see if we can read a composite object successfully
        val id = "SP1";
        val specimen = specimenService.readWithSamples(id);
        assertThat(specimen.getSpecimenId()).isEqualTo(id);
        assertThat(specimen.getSpecimenSubmitterId()).isEqualTo("Tissue-Culture 284 Gamma 3");
        assertThat(specimen.getSpecimenClass()).isEqualTo("Tumour");
        assertThat(specimen.getSpecimenType()).isEqualTo("Recurrent tumour - solid tissue");
        assertThat(specimen.getSamples().size()).isEqualTo(2);

        // Verify that we got the same samples as the sample service says we should.
        specimen.getSamples().forEach(sample -> assertThat(sample.equals(getSample(sample.getSampleId()))));
    }

    private Sample getSample(String id) {
        return sampleService.read(id);
    }

    private Specimen createSpecimen(String id, String submitterId, String donorId, String specimenClass, String type) {
        val sp = new Specimen();
        sp.setSpecimenId(id);
        sp.setSpecimenSubmitterId(submitterId);
        sp.setDonorId(donorId);
        sp.setSpecimenClass(specimenClass);
        sp.setSpecimenType(type);
        return sp;

    }

    @Test
    public void testCreateAndDeleteSpecimen() {
        val donorId = "DO2";
        Specimen s = createSpecimen("", "Specimen 101 Ipsilon Prime", donorId, "Tumour",
                "Cell line - derived from tumour");
        s.setInfo(JsonUtils.fromSingleQuoted("{'ageCategory': 42, 'status': 'deceased'}"));

        val status = specimenService.create("Study123", s);
        val id = s.getSpecimenId();

        assertThat(id).startsWith("SP");
        Assertions.assertThat(status).isEqualTo(id);

        val check = specimenService.read(id);
        assertThat(s).isEqualToComparingFieldByField(check);

        specimenService.delete(id);
        Specimen check2 = specimenService.read(id);
        assertThat(check2).isNull();
    }

    @Test
    public void testUpdateSpecimen() {
        val donorId = "DO2";
        val s = createSpecimen("", "Specimen 102 Chiron-Beta Prime", donorId, "Tumour",
                "Metastatic tumour - additional metastatic");

        specimenService.create("Study123", s);

        val id = s.getSpecimenId();

        val s2 = createSpecimen(id, "Specimen 102", s.getDonorId(), "Normal", "Normal - other");

        specimenService.update(s2);

        val s3 = specimenService.read(id);
        Assertions.assertThat(s3).isEqualToComparingFieldByField(s2);
    }

}
