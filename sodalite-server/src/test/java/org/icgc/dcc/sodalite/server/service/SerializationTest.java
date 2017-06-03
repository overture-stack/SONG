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
 */
package org.icgc.dcc.sodalite.server.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.Map;

import org.icgc.dcc.sodalite.server.model.entity.Donor;
import org.icgc.dcc.sodalite.server.utils.JsonUtils;
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
  public void testDonor() {
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
    val donor = JsonUtils.fromJson(json, Donor.class);
    assertThat(donor.getDonorId()).isEqualTo(donorId);
    assertThat(donor.getDonorSubmitterId()).isEqualTo(submitter);
    assertThat(donor.getStudyId()).isEqualTo(study);
    assertThat(donor.getDonorGender()).isEqualTo(gender);
    assertThat(donor.getSpecimens()).isEqualTo(Collections.emptyList());
    assertThat(donor.getMetadata()).isEqualTo(metadata);
  }

  @Test
  public void testDonorToJson() {
    val donor = new Donor();
    val json = JsonUtils.toJson(donor);

    val expected =
        "{'donorId':'','donorSubmitterId':'','studyId':'','donorGender':'',"
            + "'specimens':[],'metadata':'{}'}";
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
            + "'specimens':[],'metadata':'{}'}";
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
            + "'specimens':[],'metadata':'{%s}'}",
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
}
