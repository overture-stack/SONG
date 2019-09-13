/*
 * Copyright (c) 2018. Ontario Institute for Cancer Research
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

package bio.overture.song.server.service;

import static bio.overture.song.core.model.ExportedPayload.createExportedPayload;
import static bio.overture.song.core.utils.JsonUtils.mapper;
import static bio.overture.song.server.service.AnalysisTypeService.resolveAnalysisTypeId;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.groupingBy;
import static org.icgc.dcc.common.core.util.stream.Collectors.toImmutableList;
import static org.icgc.dcc.common.core.util.stream.Collectors.toImmutableSet;

import bio.overture.song.core.model.ExportedPayload;
import bio.overture.song.core.model.enums.AnalysisStates;
import bio.overture.song.server.converter.PayloadConverter;
import bio.overture.song.server.model.analysis.Analysis;
import bio.overture.song.server.model.dto.Payload;
import bio.overture.song.server.model.entity.Donor;
import bio.overture.song.server.model.entity.FileEntity;
import bio.overture.song.server.model.entity.Sample;
import bio.overture.song.server.model.entity.Specimen;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ExportService {

  /** Constants */
  private static final Set<String> ALL_ANALYSIS_STATES =
      stream(AnalysisStates.values()).map(AnalysisStates::toString).collect(toImmutableSet());

  /** Dependencies */
  private final AnalysisService analysisService;

  private final PayloadConverter payloadConverter;

  @Autowired
  public ExportService(
      @NonNull AnalysisService analysisService, @NonNull PayloadConverter payloadConverter) {
    this.analysisService = analysisService;
    this.payloadConverter = payloadConverter;
  }

  @SneakyThrows
  public List<ExportedPayload> exportPayload(
      @NonNull List<String> analysisIds, boolean includeAnalysisId) {
    val payloadMap = aggregateByStudy(analysisIds, includeAnalysisId);
    return payloadMap.entrySet().stream()
        .map(e -> buildExportedPayload(e.getKey(), e.getValue(), includeAnalysisId))
        .collect(toImmutableList());
  }

  @SneakyThrows
  public List<ExportedPayload> exportPayloadsForStudy(
      @NonNull String studyId, boolean includeAnalysisId) {
    val payloads =
        analysisService.getAnalysis(studyId, ALL_ANALYSIS_STATES).stream()
            .map(x -> convertToPayloadDTO(x, includeAnalysisId))
            .map(x -> convertToExportedPayload(x, includeAnalysisId))
            .collect(toImmutableList());
    return ImmutableList.of(createExportedPayload(studyId, payloads));
  }

  public Payload convertToPayloadDTO(@NonNull Analysis a, boolean includeAnalysisIds) {
    val payload =
        Payload.builder()
            .analysisId(includeAnalysisIds ? a.getAnalysisId() : null)
            .analysisType(resolveAnalysisTypeId(a.getAnalysisSchema()))
            .study(a.getStudy())
            .sample(payloadConverter.convertToSamplePayloads(a.getSample()))
            .file(payloadConverter.convertToFilePayloads(a.getFile()))
            .build();
    payload.addData(a.getAnalysisData().getData());
    return payload;
  }

  private Map<String, List<Payload>> aggregateByStudy(
      List<String> analysisIds, boolean includeAnalysisId) {
    return analysisService.unsecuredDeepReads(analysisIds).stream()
        .map(x -> convertToPayloadDTO(x, includeAnalysisId))
        .collect(groupingBy(Payload::getStudy));
  }

  private static ExportedPayload buildExportedPayload(
      String studyId, List<Payload> payloads, boolean includeAnalysisId) {
    val payloadJsons =
        payloads.stream()
            .map(x -> convertToExportedPayload(x, includeAnalysisId))
            .collect(toImmutableList());
    return createExportedPayload(studyId, payloadJsons);
  }

  @SneakyThrows
  private static JsonNode convertToExportedPayload(@NonNull Payload p, boolean includeAnalysisId) {
    if (!includeAnalysisId) {
      p.setAnalysisId(null);
    }
    return mapper()
        .addMixIn(Donor.class, PayloadNonEmptyMixin.class)
        .addMixIn(Payload.class, PayloadNonEmptyMixin.class)
        .addMixIn(CompositeEntityService.class, PayloadNonEmptyMixin.class)
        .addMixIn(Sample.class, PayloadNonEmptyMixin.class)
        .addMixIn(Specimen.class, PayloadNonEmptyMixin.class)
        .addMixIn(FileEntity.class, PayloadNonEmptyMixin.class)
        .valueToTree(p);
  }

  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  public static class PayloadNonEmptyMixin {}
}
