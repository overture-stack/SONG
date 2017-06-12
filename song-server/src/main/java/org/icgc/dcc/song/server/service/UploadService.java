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
import static java.lang.String.format;
import static org.springframework.http.ResponseEntity.ok;
import lombok.SneakyThrows;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.icgc.dcc.song.server.model.Upload;
import org.icgc.dcc.song.server.model.analysis.Analysis;
import org.icgc.dcc.song.server.model.enums.IdPrefix;
import org.icgc.dcc.song.server.repository.UploadRepository;
import org.icgc.dcc.song.server.utils.JsonUtils;
import org.skife.jdbi.v2.exceptions.UnableToExecuteStatementException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Slf4j
public class UploadService {

  @Autowired
  private final IdService id;
  @Autowired
  private final ValidationService validator;

  @Autowired
  private final AnalysisService analysisService;

  @Autowired
  private final UploadRepository uploadRepository;

  public Upload read(@NonNull String uploadId) {
    return uploadRepository.get(uploadId);
  }

  private void create(@NonNull String studyId, @NonNull String uploadId, @NonNull String jsonPayload) {
    uploadRepository.create(uploadId, studyId, Upload.CREATED, jsonPayload);
  }

  @SneakyThrows
  public ResponseEntity<String> upload(String studyId, String payload) {
    val uploadId = id.generate(IdPrefix.Upload);

    try {
      create(studyId, uploadId, payload);
    } catch (UnableToExecuteStatementException jdbie) {
      log.error(jdbie.getCause().getMessage());
      throw new RepositoryException(jdbie.getCause());
    }

    val analysisType = JsonUtils.readTree(payload).at("/experiment/analysisType").asText("");
    validator.validate(uploadId, payload, analysisType); // Async operation.

    return ok(uploadId);
  }

  public ResponseEntity<String> save(@NonNull String studyId, @NonNull String uploadId) {
    val s = read(uploadId);
    if (s == null ){
      return status(HttpStatus.NOT_FOUND, "UploadId %s does not exist", uploadId);
    }
    val state = s.getState();
    if (!state.equals(Upload.VALIDATED)) {
      return status(HttpStatus.CONFLICT,
          "UploadId %s is in state '%s', but must be in state 'VALIDATED' before it can be saved.", uploadId,
          state);
    }

    updateAsSaved(uploadId);
    val json = s.getPayload();
    val analysis = JsonUtils.fromJson(json, Analysis.class);
    return ok(analysisService.create(studyId, analysis));
  }

  private void updateAsSaved(@NonNull String uploadId) {
    uploadRepository.update(uploadId, Upload.SAVED, "");
  }

  private ResponseEntity<String> status(HttpStatus status, String format, Object... args) {
    return ResponseEntity.status(status).body(format(format, args));
  }

}
