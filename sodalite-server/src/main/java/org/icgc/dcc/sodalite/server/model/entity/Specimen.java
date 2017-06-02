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

package org.icgc.dcc.sodalite.server.model.entity;

import java.util.ArrayList;
import java.util.Collection;

import org.icgc.dcc.sodalite.server.model.Metadata;
import org.icgc.dcc.sodalite.server.model.enums.SpecimenClass;
import org.icgc.dcc.sodalite.server.model.enums.SpecimenType;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.val;

@Data
@EqualsAndHashCode(callSuper = false)
@JsonInclude(JsonInclude.Include.NON_ABSENT)
public class Specimen extends Metadata {

  private String specimenId = "";
  private String specimenSubmitterId = "";
  private String donorId = "";
  private SpecimenClass specimenClass = SpecimenClass.NORMAL;
  private SpecimenType specimenType = SpecimenType.NORMAL_OTHER;

  private Collection<Sample> samples = new ArrayList<>();

  public static Specimen create(String id, String submitter, String donor, String specimenClass, String type,
      String metadata) {
    val s = new Specimen();
    s.setSpecimenId(id);
    s.setSpecimenSubmitterId(submitter);
    s.setDonorId(donor);
    s.setSpecimenClass(specimenClass);
    s.setSpecimenType(type);

    return s;
  }

  public void setSpecimenClass(String specimenClass) {
    this.specimenClass = SpecimenClass.fromValue(specimenClass);
  }

  public String getSpecimenClass() {
    return specimenClass.value();
  }

  public void setSpecimenType(String type) {
    specimenType = SpecimenType.fromValue(type);
  }

  public String getSpecimenType() {
    return specimenType.value();
  }

  public void setSamples(Collection<Sample> samples) {
    this.samples.clear();
    this.samples.addAll(samples);
  }

  public void addSample(Sample sample) {
    samples.add(sample);
  }

}
