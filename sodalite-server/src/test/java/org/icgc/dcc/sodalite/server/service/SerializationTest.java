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
    val donor = JsonUtils.fromJson("{}", Donor.class);
    assertThat(donor.getDonorId()).isEqualTo("");
    assertThat(donor.getDonorSubmitterId()).isEqualTo("");
    assertThat(donor.getStudyId()).isEqualTo("");
    assertThat(donor.getDonorGender()).isEqualTo("unspecified");
    assertThat(donor.getSpecimens()).isEqualTo(Collections.emptyList());
  }

  @Test
  public void testDonorToJson() {
    val donor = new Donor();
    val json = JsonUtils.toJson(donor);

    val expected =
        "{\"donorId\":\"\",\"donorSubmitterId\":\"\",\"studyId\":\"\",\"donorGender\":\"unspecified\",\"specimens\":[]}";
    assertThat(json).isEqualTo(expected);
  }

  @Test
  public void testDonorSettings() {
    val donor = new Donor();
    donor.setDonorId(null);
    val json = JsonUtils.toJson(donor);
    val expected =
        "{\"donorSubmitterId\":\"\",\"studyId\":\"\",\"donorGender\":\"unspecified\",\"specimens\":[]}";
    assertThat(json).isEqualTo(expected);
  }
}
