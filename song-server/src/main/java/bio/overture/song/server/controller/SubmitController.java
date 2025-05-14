/*
 * Copyright (c) 2019. Ontario Institute for Cancer Research
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package bio.overture.song.server.controller;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import bio.overture.song.core.model.SubmitResponse;
import bio.overture.song.server.service.SubmitService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.util.HashMap;
import java.util.Map;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(path = "/submit")
@RequiredArgsConstructor
@Api(tags = "Submit", description = "Submit and validate json metadata")
public class SubmitController {

  /** Dependencies */
  @Autowired private final SubmitService submitService;

  @ApiOperation(value = "Submit", notes = "Synchronously submit a json payload")
  @PostMapping(
      value = "/{studyId}",
      consumes = {APPLICATION_JSON_VALUE, APPLICATION_JSON_UTF8_VALUE})
  @PreAuthorize("@studySecurity.authorize(authentication, #studyId)")
  public ResponseEntity<?> submit(
      @RequestHeader(value = AUTHORIZATION, required = false) final String accessToken,
      @PathVariable("studyId") String studyId,
      @RequestParam(value = "allowDuplicates", defaultValue = "false") boolean allowDuplicates,
      @RequestBody @Valid String json_payload) {
    try {
      SubmitResponse response = submitService.submit(studyId, json_payload, allowDuplicates);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.error("Exception occurred {}", e.getMessage());

      Map<String, Object> error = new HashMap<>();
      error.put("error", "Internal Server Error");
      error.put("message", e.getMessage());
      error.put("status", 500);
      error.put("timestamp", LocalDateTime.now().toString());

      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
  }
}
