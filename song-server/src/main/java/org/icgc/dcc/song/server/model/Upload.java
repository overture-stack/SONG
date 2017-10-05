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
package org.icgc.dcc.song.server.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonRawValue;
import lombok.Data;
import lombok.NonNull;
import lombok.val;
import org.icgc.dcc.song.server.model.enums.UploadStates;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.icgc.dcc.song.server.model.enums.UploadStates.resolveState;

@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonPropertyOrder({ "uploadId", "studyId", "state", "createdAt", "updatedAt", "errors", "payload"
})

@Data
public class Upload {

  private String uploadId = "";
  private String studyId = "";
  private String state = "";
  private List<String> errors = new ArrayList<>();
  private String payload = "";
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  public void setState(@NonNull UploadStates state){
    this.state = state.getText();
  }

  public void setState(@NonNull String state){
    setState(resolveState(state));
  }

  public static Upload create(String id, String study, UploadStates state, String errors,
      String payload, LocalDateTime created, LocalDateTime updated) {
    val u = new Upload();

    u.setUploadId(id);
    u.setStudyId(study);
    u.setState(state);
    u.setErrors(errors);
    u.setPayload(payload);
    u.setCreatedAt(created);
    u.setUpdatedAt(updated);

    return u;
  }

  @JsonIgnore
  private Map<String, Object> additionalProperties = new HashMap<>();

  @JsonRawValue
  public String getPayload() {
    return payload;
  }

  public void setErrors(String errorString) {
    if (errorString == null) {
      errorString = "";
    }

    this.errors.clear();
    this.errors.addAll(asList(errorString.split("\\|")));
  }

  public void addErrors(Collection<String> errors) {
    errors.addAll(errors);
  }

}
