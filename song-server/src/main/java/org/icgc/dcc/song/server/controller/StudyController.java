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

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.icgc.dcc.song.server.model.entity.Study;
import org.icgc.dcc.song.server.model.entity.composites.StudyWithDonors;
import org.icgc.dcc.song.server.service.StudyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(path = "/studies")
@RequiredArgsConstructor
@Api(tags = "Study", description = "Create and read studies")
public class StudyController {

  /**
   * Dependencies
   */
  @Autowired
  private final StudyService studyService;

  @ApiOperation(value = "GetStudy",
      notes = "Retrieves information for a study. If the study does not exist, an empty array is returned")
  @GetMapping("/{studyId}")
  public Study getStudy(
      @PathVariable("studyId") String studyId) {
    return studyService.read(studyId);
  }

  @ApiOperation(value = "GetEntireStudy", notes = "Retrieves all donor, specimen and sample data for a study")
  @GetMapping("/{studyId}/all")
  public StudyWithDonors getEntireStudy(
      @PathVariable("studyId") String studyId) {
    return studyService.readWithChildren(studyId);
  }

  @ApiOperation(value = "GetAllStudyIds", notes = "Retrieves all studyIds")
  @GetMapping("/all")
  public List<String> findAllStudies() {
    return studyService.findAllStudies();
  }

  @ApiOperation(value = "CreateStudy", notes = "Creates a new study")
  @PostMapping(value = "/{studyId}/", consumes = { APPLICATION_JSON_VALUE, APPLICATION_JSON_UTF8_VALUE })
  @PreAuthorize("@studySecurity.authorize(authentication, #studyId)")
  @ResponseBody
  public int saveStudy(@PathVariable("studyId") String studyId,
      @RequestHeader(value = AUTHORIZATION, required = false) final String accessToken,
      @RequestBody Study study) {
    return studyService.saveStudy(study);
  }

}
