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

package org.icgc.dcc.song.server.model.entity;

import static org.icgc.dcc.song.server.model.enums.Constants.SPECIMEN_CLASS;
import static org.icgc.dcc.song.server.model.enums.Constants.SPECIMEN_TYPE;
import static org.icgc.dcc.song.server.model.enums.Constants.validate;

import java.util.ArrayList;
import java.util.Collection;

import org.icgc.dcc.song.server.model.Metadata;

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
  private String specimenClass = "";
  private String specimenType = "";

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
    validate(SPECIMEN_CLASS, specimenClass);
    this.specimenClass = specimenClass;
  }

  public void setSpecimenType(String type) {
    validate(SPECIMEN_TYPE, type);
    specimenType = type;
  }

  public void setSamples(Collection<Sample> samples) {
    this.samples.clear();
    this.samples.addAll(samples);
  }

  public void addSample(Sample sample) {
    samples.add(sample);
  }

}
