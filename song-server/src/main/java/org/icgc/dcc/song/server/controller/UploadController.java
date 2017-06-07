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

package org.icgc.dcc.song.server.controller;

import lombok.RequiredArgsConstructor;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import javax.validation.Valid;

import org.icgc.dcc.song.server.model.Upload;
import org.icgc.dcc.song.server.service.UploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(path = "/upload")
@RequiredArgsConstructor
public class UploadController {

  /**
   * Dependencies
   */
  @Autowired
  private final UploadService uploadService;

  @PostMapping(value = "/{studyId}", consumes = { APPLICATION_JSON_VALUE, APPLICATION_JSON_UTF8_VALUE })
  @PreAuthorize("@studySecurity.authorize(authentication, #studyId)")
  public ResponseEntity<String> upload(
      @PathVariable("studyId") String studyId,
      @RequestBody @Valid String payload) {
    return uploadService.upload(studyId, payload);
  }

  @GetMapping(value = "/{studyId}/status/{uploadId}")
  @PreAuthorize("@studySecurity.authorize(authentication, #studyId)")
  public @ResponseBody
  Upload status(@PathVariable("uploadId") String uploadId) {
    return uploadService.read(uploadId);
  }

  @PostMapping(value = "/{studyId}/publish/{uploadId}")
  @ResponseBody
  @PreAuthorize("@studySecurity.authorize(authentication, #studyId)")
  public ResponseEntity<String> publish(
      @RequestHeader(value = HttpHeaders.AUTHORIZATION) final String accessToken,
      @PathVariable("studyId") String studyId,
      @PathVariable("uploadId") String uploadId) {
    return uploadService.publish(accessToken, studyId, uploadId);
  }

}
