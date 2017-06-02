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
import org.icgc.dcc.sodalite.server.model.enums.DonorGender;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.val;

@EqualsAndHashCode(callSuper = false)
@Data
@JsonPropertyOrder({ "donorId", "donorSubmitterId", "studyId", "donorGender", "specimens", "metadata" })
@JsonInclude(JsonInclude.Include.ALWAYS)
public class Donor extends Metadata {

  private String donorId = "";
  private String donorSubmitterId = "";
  private String studyId = "";
  private DonorGender donorGender = DonorGender.UNSPECIFIED;
  private final Collection<Specimen> specimens = new ArrayList<>();

  public static Donor create(String id, String submitterId, String studyId, String gender, String metadata) {
    val d = new Donor();
    d.setDonorId(id);
    d.setStudyId(studyId);
    d.setDonorSubmitterId(submitterId);
    d.setDonorGender(gender);
    d.addMetadata(metadata);

    return d;
  }

  public void setDonorGender(String gender) {
    donorGender = DonorGender.fromValue(gender);
  }

  public String getDonorGender() {
    return donorGender.value();
  }

  public void addSpecimen(Specimen specimen) {
    specimens.add(specimen);
  }

  public void setSpecimens(Collection<Specimen> specimens) {
    this.specimens.clear();
    this.specimens.addAll(specimens);
  }

}
