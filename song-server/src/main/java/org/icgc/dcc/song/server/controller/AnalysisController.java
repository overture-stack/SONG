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
import lombok.SneakyThrows;
import org.icgc.dcc.song.server.model.analysis.Analysis;
import org.icgc.dcc.song.server.model.entity.File;
import org.icgc.dcc.song.server.service.AnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequiredArgsConstructor
@RequestMapping("/studies/{studyId}/analysis")
public class AnalysisController {

  /**
   * Dependencies
   */
  @Autowired
  private final AnalysisService analysisService;

  @PutMapping(consumes = { APPLICATION_JSON_VALUE, APPLICATION_JSON_UTF8_VALUE })
  @SneakyThrows
  @PreAuthorize("@studySecurity.authorize(authentication, #studyId)")
  public ResponseEntity<String> modifyAnalysis(@PathVariable("studyId") String studyId, @RequestBody Analysis analysis) {
    return analysisService.updateAnalysis(studyId, analysis);
  }

  @PutMapping(value="/publish/{id}", consumes = { APPLICATION_JSON_VALUE, APPLICATION_JSON_UTF8_VALUE })
  @SneakyThrows
  @PreAuthorize("@studySecurity.authorize(authentication, #studyId)")
  public ResponseEntity<String> publishAnalysis(
      @RequestHeader(value = HttpHeaders.AUTHORIZATION) final String accessToken,
      @PathVariable("id") String id) {
    return analysisService.publish(accessToken,id);
  }

  @PutMapping(value="/suppress/{analysisId}", consumes = { APPLICATION_JSON_VALUE, APPLICATION_JSON_UTF8_VALUE })
  @SneakyThrows
  @PreAuthorize("@studySecurity.authorize(authentication, #studyId)")
  public ResponseEntity<String> suppressAnalysis(@PathVariable("analysisId") String analysisId) {
    return analysisService.suppress(analysisId);
  }

  /***
   * Return the JSON for this analysis (it's type, details, fileIds, etc.)
   * @param id An analysis id
   * @return A JSON object representing this analysis
   */
  @GetMapping(value = "/{id}")
  public Analysis read(@PathVariable("id") String id) {
    return analysisService.read(id);
  }

  /***
   * Return all of the files in the fileset for this analyis
   * @param id The analysis id
   * @return A list of all the files in this analysis analysisId's fileset.
   */
  @GetMapping(value = "/{id}/files")
  public List<File> getFilesById(@PathVariable("id") String id) {
    return analysisService.readFiles(id);
  }

  /***
   * Return all the analysis ids for this study matching the given parameters
   * @param params A set of command line parameters to search against
   * @return A list of analysis ids
   */
  @GetMapping(value = "")
  public List<String> getAnalyses(@RequestParam Map<String, String> params) {
    return analysisService.getAnalyses(params);
  }

}
