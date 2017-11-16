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

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.icgc.dcc.id.client.core.IdClient;
import org.icgc.dcc.song.server.model.enums.IdPrefix;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

import static java.lang.String.format;

@Service
@RequiredArgsConstructor
public class IdService {

  /**
   * Dependencies.
   */
  @Autowired
  private final IdClient idClient;

  public String generateDonorId(@NonNull String submittedDonorId, @NonNull String study) {
    return idClient.createDonorId(submittedDonorId, study);
  }

  public String generateSpecimenId(@NonNull String submittedSpecimenId, @NonNull String study) {
    return idClient.createSpecimenId(submittedSpecimenId, study);
  }

  public String generateSampleId(@NonNull String submittedSampleId, @NonNull String study) {
    return idClient.createSampleId(submittedSampleId, study);
  }

  public String generateFileId(@NonNull String analysisId, @NonNull String fileName) {
    val opt = idClient.getObjectId(analysisId, fileName);
    if (opt.isPresent()) {
      return opt.get();
    } else {
      throw new IllegalStateException("Generating objectId should not yield missing value.");
    }
  }

  public String generateAnalysisId(String analysisSubmitterId) {

  }
  public String generateAnalysisId() {
    val opt = idClient.getAnalysisId();
    if (opt.isPresent()) {
      return opt.get();
    } else {
      throw new IllegalStateException("Generating AnalysisId should not yield missing value.");
    }
  }

  public String generate(IdPrefix prefix) {
    return format("%s-%s", prefix.toString(), UUID.randomUUID());
  }

}