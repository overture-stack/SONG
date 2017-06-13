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
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import lombok.SneakyThrows;
import lombok.val;
import org.assertj.core.api.Assertions;
import org.flywaydb.test.annotation.FlywayTest;
import org.flywaydb.test.junit.FlywayTestExecutionListener;
import org.icgc.dcc.song.server.model.entity.File;
import org.icgc.dcc.song.server.utils.JsonUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import java.io.InputStream;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@RunWith(SpringRunner.class)
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, FlywayTestExecutionListener.class })
@FlywayTest
@ActiveProfiles({"dev", "secure"})
public class AnalysisServiceTest {

  @Autowired
  FileService fileService;
  @Autowired
  AnalysisService service;

  @Test
  public void testGetAnalysisType_SequencingRead() {
    val node = JsonUtils.ObjectNode().put("sequencingRead", "{}");
    val s = JsonUtils.nodeToJSON(node);
    val result = service.getAnalysisType(s);
    assertThat(result).isEqualTo("sequencingRead");
  }

  @Test
  public void testGetAnalysisType_VariantCall() {
    val node = JsonUtils.ObjectNode().put("variantCall", "{}");
    val s = JsonUtils.nodeToJSON(node);
    val result = service.getAnalysisType(s);
    assertThat(result).isEqualTo("variantCall");
  }

  @Test
  public void testAddFile() {
    val id = "MU1";
    val fileId = "FI3";

    service.addFile(id, fileId);
    // TODO: verify record was added to FileSet table
    assertThat(true); // we didn't crash
  }

  @Test
  public void testCreateAnalysis() {
    val id = "AN3";
    val studyId = "ABC123";
    val type = "sequencingRead";

    service.createAnalysis(id, studyId, type);
    // TODO: verify record was added to Analysis table

    assertThat(true); // we didn't crash
  }

  @SneakyThrows
  @Test
  public void testCreateSequencingRead() {
    val id = "AN3";

    val node = JsonNodeFactory.instance.objectNode().put("libraryStrategy", "WXS").put("pairedEnd", false)
        .put("insertSize", 900L).put("aligned", true).put("alignmentTool", "Muse variant call pipeline")
        .put("referenceGenome", "hs37d5");

    service.createSequencingRead(id, node);
    // TODO: Verify record was added to SequencingRead table

    assertThat(true); // we didn't crash
  }

  @SneakyThrows
  @Test
  public void testCreateVariantCall() {
    val id = "AN4";
    val studyId = "ABC123";
    val type = "variantCall";

    val node = JsonNodeFactory.instance.objectNode().put("variantCallingTool", "silver bullet")
        .put("tumourSampleSubmitterId", "tumor1A").put("matchedNormalSampleSubmitterId", "reference2B");

    service.createAnalysis(id, studyId, type);
    service.createVariantCall(id, node);

    // TODO: Verify record was added to VariantCallTable
    assertThat(true); // no crash yet
  }

  @SneakyThrows
  @Test
  public void testSaveStudy() {
    val fileName = "documents/upload-sequencingread-valid.json";
    val studyId = "ABC123";

    String json = getJsonNodeFromClasspath(fileName);
    val study = JsonUtils.readTree(json).get("study");

    val fileIds = service.saveStudy(studyId, study);
    // TODO: 1) Verify that the correct records were added to / updated in the Donor, Specimen, Sample, and File tables
    // 2) Verify that the fileIds that were returned were the correct
    assertThat(fileIds.size() == 2);
  }

  @SneakyThrows
  @Test
  public void testCreate() {
    val fileName = "documents/upload-sequencingread-valid.json";
    val studyId = "ABC123";
    String json = getJsonNodeFromClasspath(fileName);
    service.create(studyId, json);
    // TODO: Verify that the study AND the correct type of analysis was created
    assertThat(true); // no crashes yet

  }

  public String getJsonNodeFromClasspath(String name) throws Exception {
    InputStream is1 = Thread.currentThread().getContextClassLoader().getResourceAsStream(name);
    ObjectMapper mapper = new ObjectMapper();
    JsonNode node = mapper.readTree(is1);
    return mapper.writeValueAsString(node);
  }

  @Test
  public void testReadFilesByAnalysisId() {
    val files = service.readFilesByAnalysisId("AN1");
    System.err.printf("Got files '%s'", files);
    val expectedFiles = new ArrayList<File>();
    expectedFiles.add(fileService.read("FI1"));
    expectedFiles.add(fileService.read("FI2"));

    Assertions.assertThat(files).containsAll(expectedFiles);
    assertThat(expectedFiles).containsAll(files);
  }

}
