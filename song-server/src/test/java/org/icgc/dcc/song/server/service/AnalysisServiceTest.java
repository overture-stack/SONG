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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.assertj.core.api.Assertions;
import org.flywaydb.test.annotation.FlywayTest;
import org.flywaydb.test.junit.FlywayTestExecutionListener;
import org.icgc.dcc.song.server.model.analysis.Analysis;
import org.icgc.dcc.song.server.model.analysis.SequencingReadAnalysis;
import org.icgc.dcc.song.server.model.analysis.VariantCallAnalysis;
import org.icgc.dcc.song.server.model.entity.File;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.icgc.dcc.song.core.utils.JsonUtils.fromJson;
import static org.icgc.dcc.song.core.utils.JsonUtils.toJson;
import static org.icgc.dcc.song.server.model.enums.Constants.list;
import static java.lang.String.format;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, FlywayTestExecutionListener.class })
@FlywayTest
@ActiveProfiles("dev")
public class AnalysisServiceTest {

  @Autowired
  FileService fileService;
  @Autowired
  AnalysisService service;

  @SneakyThrows
  private String readFile(String name) {
    return new String(Files.readAllBytes(new java.io.File("..", name).toPath()));
  }

  @Test
  public void testCreateAndUpdate() {
    val study="ABC123";
    val json = readFile("sequencingRead.json");
    val analysis = fromJson(json, Analysis.class);
    val analysisId=service.create(study, analysis);

    val created = service.read(analysisId);
    assertThat(created.getAnalysisId()).isEqualTo(analysisId);
    assertThat(created.getAnalysisState()).isEqualTo(analysis.getAnalysisState());
    assertThat(created.getAnalysisType()).isEqualTo("sequencingRead");
    assertThat(created.getSample().size()).isEqualTo(1);
    val sample = created.getSample().get(0);
    val experiment = ((SequencingReadAnalysis) created).getExperiment();
    assertThat(experiment).isNotNull();
    assertThat(experiment.getAlignmentTool().equals("BigWrench"));

    // test update
    val change="ModifiedToolName";
    experiment.setAlignmentTool(change);
    service.updateAnalysis(study, created);
    val gotBack = service.read(analysisId);
    val experiment2 =((SequencingReadAnalysis)gotBack).getExperiment();
    assertThat(experiment2.getAlignmentTool() ).isEqualTo(change);

    log.info(format("Created '%s'",toJson(created)));
  }

  @Test
  public void testCreateAndUpdateVariantCall() {
    val study="ABC123";
    val json = readFile("variantCall.json");
    val analysis = fromJson(json, Analysis.class);
    val analysisId=service.create(study, analysis);

    val created = service.read(analysisId);
    assertThat(created.getAnalysisId()).isEqualTo(analysisId);
    assertThat(created.getAnalysisState()).isEqualTo(analysis.getAnalysisState());
    assertThat(created.getAnalysisType()).isEqualTo("variantCall");
    assertThat(created.getSample().size()).isEqualTo(1);
    val sample = created.getSample().get(0);
    val experiment = ((VariantCallAnalysis) created).getExperiment();
    assertThat(experiment).isNotNull();
    assertThat(experiment.getVariantCallingTool()).isEqualTo("silver bullet");

    // test update
    val change="GoldenHammer";
    experiment.setVariantCallingTool(change) ;
    service.updateAnalysis(study, created);
    val gotBack = service.read(analysisId);
    val experiment2 =((VariantCallAnalysis)gotBack).getExperiment();
    assertThat(experiment2.getVariantCallingTool()).isEqualTo(change);

    log.info(format("Created '%s'",toJson(created)));
  }


  @Test
  public void testRead() {
    // test sequencing read
    val id1="AN1";
    val json1 = readFile("existingVariantCall.json");
    val analysis1 = service.read(id1);
    assertThat(analysis1.getAnalysisId()).isEqualTo("AN1");
    //assertThat(analysis1.getAnalysisState()).isEqualTo("UNPUBLISHED");
    assertThat(analysis1.getAnalysisType()).isEqualTo("variantCall");
    assertThat(analysis1.getStudy()).isEqualTo("ABC123");
    assertThat(analysis1.getSample().size()).isEqualTo(2);
    assertThat(analysis1.getInfo()).isEqualTo("{}");
    assertThat(analysis1.getFile().size()).isEqualTo(2);
    assertThat(analysis1).isInstanceOf(VariantCallAnalysis.class);
    val experiment1 = ((VariantCallAnalysis) analysis1).getExperiment();
    assertThat(experiment1).isNotNull();
    assertThat(experiment1.getVariantCallingTool()).isEqualTo("SuperNewVariantCallingTool");


    // test variant call
    val id2="AN2";
    val json2 = readFile("existingSequencingRead.json");
    val analysis2 = service.read(id2);
    assertThat(analysis2.getAnalysisId()).isEqualTo("AN2");
    //assertThat(analysis2.getAnalysisState()).isEqualTo("UNPUBLISHED");
    assertThat(analysis2.getAnalysisType()).isEqualTo("sequencingRead");
    assertThat(analysis2.getFile().size()).isEqualTo(2);
    assertThat(analysis2).isInstanceOf(SequencingReadAnalysis.class);
    val experiment2 = ((SequencingReadAnalysis) analysis2).getExperiment();
    assertThat(experiment2).isNotNull();
    assertThat(experiment2.getAlignmentTool()).isEqualTo("BigWrench");

    //checkRead(id2, fromJson(json2, Analysis.class));

    // test not found
    val id3="ANDOESNTEXIST";
    val analysis3 = service.read(id3);
    assertThat(analysis3).isNull();
  }


  @Ignore
  @Test
  public void testPublish() {
    // TODO: Figure out how to test this
    val token = "mockToken";
    val id = "AN1";
    service.publish(token, id);

    val analysis = service.read(id);
    assertThat(analysis.getAnalysisState()).isEqualTo("PUBLISHED");
  }

  @Test
  public void testUpdate() {
    val id = "AN1";
    val analysis = service.read(id);
    // FIXME: implement this...
  }

  @Test
  public void testSuppress() {
    val id = "AN2";
    service.suppress(id);

    val analysis = service.read(id);
    assertThat(analysis.getAnalysisState()).isEqualTo("SUPPRESSED");
  }

  public String getJsonNodeFromClasspath(String name) throws Exception {
    InputStream is1 = Thread.currentThread().getContextClassLoader().getResourceAsStream(name);
    ObjectMapper mapper = new ObjectMapper();
    JsonNode node = mapper.readTree(is1);
    return mapper.writeValueAsString(node);
  }

  @Test
  public void testReadFiles() {
    val files = service.readFiles("AN1");
    System.err.printf("Got files '%s'", files);
    val expectedFiles = new ArrayList<File>();
    expectedFiles.add(fileService.read("FI1"));
    expectedFiles.add(fileService.read("FI2"));

    Assertions.assertThat(files).containsAll(expectedFiles);
    assertThat(expectedFiles).containsAll(files);
  }

}
