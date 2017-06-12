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

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.icgc.dcc.song.server.model.analysis.Analysis;
import org.icgc.dcc.song.server.model.analysis.Experiment;
import org.icgc.dcc.song.server.model.analysis.SequencingRead;
import org.icgc.dcc.song.server.model.entity.Donor;
import org.icgc.dcc.song.server.model.entity.File;
import org.icgc.dcc.song.server.model.entity.composites.DonorSpecimens;
import org.icgc.dcc.song.server.utils.JsonUtils;
import org.junit.Test;

import lombok.SneakyThrows;
import lombok.val;

public class SerializationTest {

  @Test
  @SneakyThrows
  public void testConvertValue() {
    val json = "{}";

    @SuppressWarnings("rawtypes")
    val m = JsonUtils.fromJson(json, Map.class);
    assertThat(Collections.emptyMap()).isEqualTo(m);
  }

  @Test
  @SneakyThrows
  public void testDonorSpecimens() {
    val donorId = "DO1234";
    val submitter = "1234";
    val study = "X2345-QRP";
    val gender = "female";

    val single = String.format(
        "{'donorId':'%s','donorSubmitterId':'%s','studyId':'%s','donorGender':'%s',"
            + "'roses':'red','violets':'blue'}",
        donorId, submitter, study, gender);
    val metadata = JsonUtils.fromSingleQuoted("{'roses':'red','violets':'blue'}");
    val json = JsonUtils.fromSingleQuoted(single);
    val donor = JsonUtils.fromJson(json, DonorSpecimens.class);
    assertThat(donor.getDonorId()).isEqualTo(donorId);
    assertThat(donor.getDonorSubmitterId()).isEqualTo(submitter);
    assertThat(donor.getStudyId()).isEqualTo(study);
    assertThat(donor.getDonorGender()).isEqualTo(gender);
    assertThat(donor.getSpecimens()).isEqualTo(Collections.emptyList());
    assertThat(donor.getInfo()).isEqualTo(metadata);
  }

  @Test
  public void testDonorToJson() {
    val donor = new Donor();
    val json = JsonUtils.toJson(donor);

    val expected =
        "{'donorId':'','donorSubmitterId':'','studyId':'','donorGender':'',"
            + "'info':'{}'}";
    val expectedJson = JsonUtils.fromSingleQuoted(expected);
    assertThat(json).isEqualTo(expectedJson);
  }

  @Test
  public void testDonorSettings() {
    val donor = new Donor();
    donor.setDonorId(null);
    val json = JsonUtils.toJson(donor);
    System.err.printf("json='%s'\n", json);
    val expected =
        "{'donorId':null,'donorSubmitterId':'','studyId':'','donorGender':'',"
            + "'info':'{}'}";
    val expectedJson = JsonUtils.fromSingleQuoted(expected);
    assertThat(json).isEqualTo(expectedJson);
  }

  @Test
  public void testDonorValues() {
    val id = "DO000123";
    val submitterId = "123";
    val studyId = "X23-CA";
    val gender = "male";
    val metadata = "";

    val donor = Donor.create(id, submitterId, studyId, gender, metadata);
    val json = JsonUtils.toJson(donor);

    val expected = String.format(
        "{'donorId':'%s','donorSubmitterId':'%s','studyId':'%s','donorGender':'%s',"
            + "'info':'{%s}'}",
        id, submitterId, studyId, gender, metadata);
    val expectedJson = JsonUtils.fromSingleQuoted(expected);
    assertThat(json).isEqualTo(expectedJson);
  }

  @Test
  public void testInvalidValues() {
    val id = "DO000123";
    val submitterId = "123";
    val studyId = "X23-CA";
    val gender = "potatoes";
    val metadata = "";

    boolean failed = false;
    try {
      Donor.create(id, submitterId, studyId, gender, metadata);
    } catch (IllegalArgumentException e) {
      failed = true;
    }

    assertThat(failed).isTrue();

  }

  @Test
  public void testSequencingReadToJSON() {
    val id="AN1";
    val aligned=true;
    val alignmentTool="BigWrench";
    val insertSize=25;
    val libraryStrategy="Other";
    val pairedEnd = false;
    //val metadata = JsonUtils.fromSingleQuoted("'sequencingTool': 'NanoporeSeq123'");
    val metadata = "";

    val sequencingRead = SequencingRead.create(id, aligned, alignmentTool, insertSize,
            libraryStrategy, pairedEnd, metadata);
    val json = JsonUtils.toJson(sequencingRead);

    val expected = String.format("{'analysisType':'sequencingRead','analysisId':'%s','aligned':%s,'alignmentTool':'%s'," +
            "'insertSize':%s,'libraryStrategy':'%s','pairedEnd':%s,'info':'{%s}'}", id, aligned, alignmentTool,
            insertSize, libraryStrategy, pairedEnd, metadata);
    val expectedJson = JsonUtils.fromSingleQuoted(expected);
    assertThat(json).isEqualTo(expectedJson);
  }

  @Test
  public void testSequencingReadFromJson() {
    val id="AN1";
    val aligned=true;
    val alignmentTool="BigWrench";
    val insertSize=25;
    val libraryStrategy="Other";
    val pairedEnd = false;

    val metadata = "";

    val sequencingRead1 = SequencingRead.create(id, aligned, alignmentTool, insertSize,
            libraryStrategy, pairedEnd, metadata);

    val singleQuotedJson = String.format("{'analysisType':'sequencingRead', 'analysisId':'%s','aligned':%s,'alignmentTool':'%s'," +
                    "'insertSize':%s,'libraryStrategy':'%s','pairedEnd':%s,'info':'{%s}'}", id, aligned, alignmentTool,
            insertSize, libraryStrategy, pairedEnd, metadata);
    val json = JsonUtils.fromSingleQuoted(singleQuotedJson);
    //System.out.printf("Constructing sequencing read from '%s'\n", json);
    val sequencingRead2 = JsonUtils.fromJson(json, SequencingRead.class);


    assertThat(sequencingRead1).isEqualToComparingFieldByField(sequencingRead2);

  }

  @Test
  public void testListFile() throws IOException {
    val singleQuotedJson = "{'file':[ { 'objectId': 'FI12345', 'fileName':'dna3.bam', 'fileMd5':'A1B2C3D4E5F6'}," +
            "{'objectId': 'FI34567', 'fileName': 'dna7.fasta', 'fileType':'BAM', 'fileSize':1234, 'fileMd5': 'F1E2D3'}]}";

    val json = JsonUtils.fromSingleQuoted(singleQuotedJson);
    val root=JsonUtils.readTree(json);
    val files=root.get("file");
    String fileJson=JsonUtils.toJson(files);

    List<File> f = Arrays.asList(JsonUtils.fromJson(fileJson, File[].class));

    assertThat(f.size()).isEqualTo(2);
    assertThat(f.get(0).getFileName()).isEqualTo("dna3.bam");
  }

  @Test
  public void testSequencingReadAnalysisFromJson() throws IOException {
    val json = new String(Files.readAllBytes(new java.io.File("..","meta2.json").toPath()));
    val analysis = JsonUtils.fromJson(json, Analysis.class);

    System.out.printf("*** Analysis object='%s'\n",analysis);

    assertThat(analysis.getStudy()).isEqualTo("ABC123");
    assertThat(analysis.getFile().size()).isEqualTo(2);
    assertThat(analysis.getSample().get(0).getDonor().getDonorSubmitterId()).isEqualTo("internal_donor_123456789-00");

    Experiment e = analysis.getExperiment();

    assertThat(e).isNotNull();

    //assertThat(e).isInstanceOf(SequencingRead.class);
    val r = (SequencingRead) e;

    assertThat(r.getLibraryStrategy()).isEqualTo("WXS");
    assertThat(r.getInsertSize()).isEqualTo(900);
    assertThat(r.getAlignmentTool()).isEqualTo("MUSE variant call pipeline");
  }

  @Test
  public void testVariantCallAnalysisFromJson() throws IOException {
    val json = new String(Files.readAllBytes(new java.io.File("..","variantCall.json").toPath()));
    val analysis = JsonUtils.fromJson(json, Analysis.class);

    System.out.printf("*** Analysis object='%s'\n",analysis);


  }

}
