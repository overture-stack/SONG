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

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.icgc.dcc.song.core.utils.JsonUtils;
import org.icgc.dcc.song.server.model.Upload;
import org.icgc.dcc.song.server.model.analysis.Analysis;
import org.icgc.dcc.song.server.model.enums.IdPrefix;
import org.icgc.dcc.song.server.repository.UploadRepository;
import org.skife.jdbi.v2.exceptions.UnableToExecuteStatementException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

import static java.lang.String.format;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.ANALYSIS_ID_NOT_CREATED;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.PAYLOAD_PARSING;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.UPLOAD_ID_NOT_FOUND;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.UPLOAD_ID_NOT_VALIDATED;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.UPLOAD_REPOSITORY_CREATE_RECORD;
import static org.icgc.dcc.song.core.exceptions.SongError.error;
import static org.springframework.http.ResponseEntity.ok;

@RequiredArgsConstructor
@Service
@Slf4j
public class UploadService {

  private static final String MESSAGE_CONTEXT = UploadService.class.getSimpleName();

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

  private void create(@NonNull String studyId, String analysisSubmitterId, @NonNull String uploadId,
                      @NonNull String jsonPayload) {
    uploadRepository.create(uploadId, studyId, analysisSubmitterId, Upload.CREATED, jsonPayload);
  }

  private void update(@NonNull String uploadId, @NonNull String jsonPayload) {
    uploadRepository.update_payload(uploadId, Upload.UPDATED, jsonPayload);
  }

  @SneakyThrows
  public ResponseEntity<String> upload(@NonNull String studyId, @NonNull String payload, boolean isAsyncValidation) {
    String analysisType;
    String uploadId;
    val status = JsonUtils.ObjectNode();
    status.put("status","ok");

    try {
      val analysisSubmitterId=JsonUtils.readTree(payload).at("/analysisSubmitterId").asText();
      List<String> ids;

      if (analysisSubmitterId.equals("")) {
        // Our business rules say that we always want to create a new record if no analysisSubmitterId is set,
        // even if the rest of the content is duplicated.
        ids = Collections.emptyList();
      } else {
        ids = uploadRepository.findByBusinessKey(studyId, analysisSubmitterId);
      }

      if (ids == null || ids.isEmpty()) {
        uploadId = id.generate(IdPrefix.Upload);
        create(studyId, analysisSubmitterId, uploadId, payload);
      } else if (ids.size() == 1) {
        uploadId = ids.get(0);
        val previousUpload = uploadRepository.get(uploadId);
        status.put("status",
                format("WARNING: replaced content for analysisSubmitterId '%s'",
                        analysisSubmitterId));
        status.put("replaced", previousUpload.getPayload());
        update(uploadId, payload);

      } else {
        return error(MESSAGE_CONTEXT, UPLOAD_ID_NOT_FOUND,
                "Multiple upload ids found for analysisSubmitterId='%s', study='%s'",
                analysisSubmitterId, studyId);
      }
      analysisType = JsonUtils.readTree(payload).at("/analysisType").asText("");
    } catch (UnableToExecuteStatementException jdbie) {
      log.error(jdbie.getCause().getMessage());

      //TODO: Should we do this for all respository calls in the other services???
      return error(MESSAGE_CONTEXT, UPLOAD_REPOSITORY_CREATE_RECORD,
          "Unable to create record in upload repository");

    } catch (JsonProcessingException jpe){
      log.error(jpe.getCause().getMessage());
      return error(MESSAGE_CONTEXT, PAYLOAD_PARSING,
          "Unable parse the input payload: %s ",payload);
    }

    if (isAsyncValidation){
      validator.asyncValidate(uploadId, payload, analysisType); // Asynchronous operation.
    } else {
      validator.syncValidate(uploadId, payload, analysisType); // Synchronous operation
    }
    status.put("uploadId", uploadId);
    return ok(status.toString());
  }

  public ResponseEntity<String> save(@NonNull String studyId, @NonNull String uploadId) {
    val s = read(uploadId);
    if (s == null ){
      return error(MESSAGE_CONTEXT, UPLOAD_ID_NOT_FOUND,
          "UploadId %s does not exist", uploadId);
    }
    val state = s.getState();
    if (!state.equals(Upload.VALIDATED)) {
      return error(MESSAGE_CONTEXT, UPLOAD_ID_NOT_VALIDATED,
          "UploadId %s is in state '%s', but must be in state '%s' before it can be saved",
          uploadId, state, Upload.VALIDATED);
    }

    val json = s.getPayload();
    val analysis = JsonUtils.fromJson(json, Analysis.class);

    val analysisId = analysisService.create(studyId, analysis);
    if (analysisId == null) {
      return error(MESSAGE_CONTEXT, ANALYSIS_ID_NOT_CREATED,
          "Could not create analysisId for upload id '%s",uploadId);
    }

    updateAsSaved(uploadId);
    val reply = JsonUtils.fromSingleQuoted(format("{'analysisId': '%s', 'status': '%s'}", analysisId, "ok"));
    return ok(reply);
  }

  private void updateAsSaved(@NonNull String uploadId) {
    uploadRepository.update(uploadId, Upload.SAVED, "");
  }

}
