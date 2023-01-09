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

import bio.overture.song.core.model.ExportedPayload;
import bio.overture.song.server.service.ExportService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/export")
@Api(tags = "Export", description = "Export payloads")
public class ExportController {

  /** Dependencies */
  @Autowired private final ExportService exportService;

  @ApiOperation(value = "ExportStudy", notes = "Exports all the payloads for a study")
  @GetMapping(value = "/studies/{studyId}")
  @ResponseBody
  public List<ExportedPayload> exportStudy(@PathVariable("studyId") String studyId) {
    return exportService.exportPayloadsForStudy(studyId);
  }

  @ApiOperation(value = "ExportAnalysis", notes = "Exports the payload for a list of analysisIds")
  @GetMapping(value = "/analysis/{analysisIds}")
  @ResponseBody
  public List<ExportedPayload> exportAnalysis(
      @PathVariable("analysisIds")
          @ApiParam(value = "Comma separated list of analysisIds", required = true)
          List<String> analysisIds) {
    return exportService.exportPayload(analysisIds);
  }
}
