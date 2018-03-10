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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.val;
import org.icgc.dcc.song.server.model.Metadata;

import static org.icgc.dcc.song.server.model.enums.Constants.DONOR_GENDER;
import static org.icgc.dcc.song.server.model.enums.Constants.validate;

@EqualsAndHashCode(callSuper = false)
@Data
@ToString(callSuper = true)
@JsonPropertyOrder({ "donorId", "donorSubmitterId", "studyId", "donorGender", "specimens", "info" })
@JsonInclude(JsonInclude.Include.ALWAYS)

public class Donor extends Metadata {

  private String donorId = "";
  private String donorSubmitterId = "";
  private String studyId = "";
  private String donorGender = "";

  public static Donor create(String id, String submitterId, String studyId, String gender) {
    val d = new Donor();
    d.setDonorId(id);
    d.setStudyId(studyId);
    d.setDonorSubmitterId(submitterId);
    d.setDonorGender(gender);

    return d;
  }

  public void setDonorGender(String gender) {
    validate(DONOR_GENDER, gender);
    this.donorGender = gender;
  }

}
