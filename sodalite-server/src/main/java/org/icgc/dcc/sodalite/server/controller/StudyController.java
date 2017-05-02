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
 */
package org.icgc.dcc.sodalite.server.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.icgc.dcc.sodalite.server.model.Study;
import org.icgc.dcc.sodalite.server.model.SubmissionStatus;
import org.icgc.dcc.sodalite.server.service.StatusService;
import org.icgc.dcc.sodalite.server.service.StudyService;
import org.icgc.dcc.sodalite.server.service.ValidationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.http.ResponseEntity.badRequest;

import java.util.Arrays;
import java.util.List;

import javax.validation.Valid;

import static org.springframework.http.MediaType.*;

@Slf4j
@RestController
@RequestMapping(path="/studies")
@RequiredArgsConstructor
public class StudyController {

  /**
   * Dependencies
   */
  @Autowired
  private final StudyService studyService;
  @Autowired
  private final ValidationService validationService;
  @Autowired
  private final StatusService statusService;
  
  @GetMapping("/{studyId}")
  public List<Study> getStudy(@PathVariable("studyId") String studyId) {
    return Arrays.asList(studyService.getStudy(studyId));
  }
  
  @PostMapping(value = "/{studyId}/analyses/sequencingread/{uploadId}", consumes = {APPLICATION_JSON_VALUE, APPLICATION_JSON_UTF8_VALUE})
  public ResponseEntity<String> registerSequencingRead(
  		@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) final String accessToken,
  		@PathVariable("studyId") String studyId, 
  		@PathVariable("uploadId") String uploadId,
  		@RequestBody @Valid String payload) {

  	// TODO: security check
  	return register("registerSequencingRead", studyId, uploadId, payload);
  }

  @PostMapping(value = "/{studyId}/analyses/variantcall/{uploadId}", consumes = {APPLICATION_JSON_VALUE, APPLICATION_JSON_UTF8_VALUE})
  public ResponseEntity<String> registerVariantCall(
  		@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) final String accessToken,
  		@PathVariable("studyId") String studyId, 
  		@PathVariable("uploadId") String uploadId,
  		@RequestBody @Valid String payload) {
  	
  	// TODO: security check
  	return register("registerVariantCall", studyId, uploadId, payload);
  }

  /**
   * Common registration logic for both Sequencing Reads and Variant Calls
   * 
   * @param schemaName
   * @param studyId
   * @param uploadId
   * @param payload
   * @return
   */
	protected ResponseEntity<String> register(String schemaName, String studyId, String uploadId, String payload) {

    // do pre-check for whether this upload id has been used. We want to return this error synchronously
    if (statusService.exists(studyId, uploadId)) {
      return conflict(studyId, uploadId);
    }

    try {
      validationService.validate(schemaName, studyId, uploadId, payload);
  	}
    catch(Exception e) {
      log.error(e.toString());
      return badRequest().body(e.getMessage());
    }
    return ok(uploadId);
	}
  
  @PostMapping(value = "/{studyId}/", consumes = {APPLICATION_JSON_VALUE, APPLICATION_JSON_UTF8_VALUE})
  @ResponseBody
  public int saveStudy(
  		@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) final String accessToken,
  		@RequestBody Study study) {

  	// TODO: security check
    return studyService.saveStudy(study);
  }

  @GetMapping(value = "/{studyId}/statuses/{uploadId}")
  public @ResponseBody SubmissionStatus getStatus(
      @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) final String accessToken,
      @PathVariable("studyId") String studyId,
      @PathVariable("uploadId") String uploadId) {

    return statusService.getStatus(studyId, uploadId);
  }

  protected ResponseEntity<String> conflict(String studyId, String uploadId) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
    	  .body(String.format("The upload id '%s' has already been used in a previous submission for this study (%s)", uploadId, studyId));
  }
}
