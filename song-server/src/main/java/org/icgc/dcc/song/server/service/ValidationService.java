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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.icgc.dcc.song.core.utils.JsonUtils;
import org.icgc.dcc.song.server.repository.UploadRepository;
import org.icgc.dcc.song.server.validation.SchemaValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import static java.lang.String.format;
import static org.icgc.dcc.song.server.model.enums.UploadStates.VALIDATED;
import static org.icgc.dcc.song.server.model.enums.UploadStates.VALIDATION_ERROR;

@Slf4j
@Service
@RequiredArgsConstructor
public class ValidationService {

  @Autowired
  private SchemaValidator validator;

  @Autowired(required = false)
  private Long validationDelayMs = -1L;

  protected static final ObjectMapper mapper = new ObjectMapper().registerModule(new ParameterNamesModule())
      .registerModule(new Jdk8Module())
      .registerModule(new JavaTimeModule());

  @Autowired
  private final UploadRepository uploadRepository;

  private String upperCaseFirstLetter(String s) {
    return s.substring(0, 1).toUpperCase() + s.substring(1);
  }

  @Async
  public void asyncValidate(@NonNull String uploadId, @NonNull String payload, String analysisType) {
    syncValidate(uploadId, payload, analysisType);
  }

  public void syncValidate(@NonNull String uploadId, @NonNull String payload, String analysisType) {
    log.info("Validating payload for upload Id=" + uploadId + "payload=" + payload);
    log.info(format("Analysis type='%s'",analysisType));
    try {
      val jsonNode = JsonUtils.readTree(payload);

      if (analysisType == null) {
        updateAsInvalid(uploadId, "Uploaded JSON document does not contain a valid analysis type");
      } else {
        val schemaId = "upload" + upperCaseFirstLetter(analysisType);
        val response = validator.validate(schemaId, jsonNode);

        if (response.isValid()) {
          updateAsValid(uploadId);
        } else {
          updateAsInvalid(uploadId, response.getValidationErrors());
        }
      }
    } catch (JsonProcessingException jpe) {
      log.error(jpe.getMessage());
      updateAsInvalid(uploadId, format("Invalid JSON document submitted: %s", jpe.getMessage()));
    } catch (Exception e) {
      log.error(e.getMessage());
      updateAsInvalid(uploadId, format("Unknown processing problem: %s", e.getMessage()));
    }

  }


  private void updateAsValid(@NonNull String uploadId) {
    uploadRepository.update(uploadId, VALIDATED.getText(), "");
  }

  private void updateAsInvalid(@NonNull String uploadId, @NonNull String errorMessages) {
    uploadRepository.update(uploadId, VALIDATION_ERROR.getText(), errorMessages);
  }

}
