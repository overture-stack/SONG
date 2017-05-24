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

package org.icgc.dcc.sodalite.server.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.icgc.dcc.sodalite.server.model.SubmissionStatus;
import org.icgc.dcc.sodalite.server.service.FunctionService;
import org.icgc.dcc.sodalite.server.service.RegistrationService;
import org.icgc.dcc.sodalite.server.service.StatusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import static org.icgc.dcc.sodalite.server.utils.JsonUtils.jsonStatus;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.ResponseEntity.badRequest;
import static org.springframework.http.ResponseEntity.ok;

@Slf4j
@RestController
@RequestMapping(path = "/upload")
@RequiredArgsConstructor
public class UploadController {

  /**
   * Dependencies
   */
  @Autowired
  private final RegistrationService registrationService;
  @Autowired
  private final StatusService statusService;
  @Autowired
  private final FunctionService functionService;

  @PostMapping(value = "/{studyId}/sequencingread/{uploadId}", consumes = { APPLICATION_JSON_VALUE, APPLICATION_JSON_UTF8_VALUE })
  @PreAuthorize("@studySecurity.authorize(authentication, #studyId)")
  public ResponseEntity<String> registerSequencingRead(
      @PathVariable("studyId") String studyId,
      @PathVariable("uploadId") String uploadId,
      @RequestBody @Valid String payload) {

    return register("registerSequencingRead", studyId, uploadId, payload);
  }

  @PostMapping(value = "/{studyId}/variantcall/{uploadId}", consumes = { APPLICATION_JSON_VALUE, APPLICATION_JSON_UTF8_VALUE })
  @PreAuthorize("@studySecurity.authorize(authentication, #studyId)")
  public ResponseEntity<String> registerVariantCall(
      @PathVariable("studyId") String studyId,
      @PathVariable("uploadId") String uploadId,
      @RequestBody @Valid String payload) {

    return register("registerVariantCall", studyId, uploadId, payload);
  }

  @GetMapping(value = "/{uploadId}")
  public @ResponseBody
  SubmissionStatus getStatus(@PathVariable("uploadId") String uploadId) {
    return statusService.getStatus(uploadId);
  }

  @PostMapping(value = "/{upload_id}/publish")
  @ResponseBody
  @PreAuthorize("@studySecurity.authorize(authentication, #studyId)")
  public String publishByUploadId(@PathVariable("study_id") String studyId,
                                  @PathVariable("upload_id") String uploadId) {
    val status = functionService.publishId(studyId, uploadId);
    return jsonStatus(status, "status", "Successfully published " + uploadId, "Publish of " + uploadId + " failed");
  }

  private ResponseEntity<String> conflict(String studyId, String uploadId) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(String.format("The upload id '%s' has already been used in a previous submission for this study (%s)",
            uploadId, studyId));
  }

  private ResponseEntity<String> register(String schemaName, String studyId, String uploadId, String payload) {

    // do pre-check for whether this upload id has been used. We want to return
    // this error synchronously
    if (statusService.exists(studyId, uploadId)) {
      return conflict(studyId, uploadId);
    }

    try {
      registrationService.register(schemaName, studyId, uploadId, payload);
    } catch (Exception e) {
      log.error(e.toString());
      return badRequest().body(e.getMessage());
    }
    return ok(uploadId);
  }

}
