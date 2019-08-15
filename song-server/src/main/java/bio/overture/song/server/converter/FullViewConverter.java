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

package bio.overture.song.server.converter;

import static bio.overture.song.core.utils.JsonUtils.toJsonNode;
import static bio.overture.song.server.model.enums.AnalysisTypes.resolveAnalysisType;
import static java.lang.String.format;
import static java.util.stream.Collectors.groupingBy;
import static lombok.AccessLevel.PRIVATE;
import static org.icgc.dcc.common.core.util.stream.Collectors.toImmutableSet;

import bio.overture.song.server.model.analysis.AbstractAnalysis;
import bio.overture.song.server.model.entity.Donor;
import bio.overture.song.server.model.entity.FileEntity;
import bio.overture.song.server.model.entity.FullView;
import bio.overture.song.server.model.entity.Sample;
import bio.overture.song.server.model.entity.Specimen;
import bio.overture.song.server.model.entity.composites.CompositeEntity;
import bio.overture.song.server.model.enums.AnalysisTypes;
import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.val;

/**
 * Contains methods for converting FullView entities into fully populated SequencingReadAnalyses or
 * VariantCallAnalyses
 */
@NoArgsConstructor(access = PRIVATE)
public class FullViewConverter {

  /**
   * Converts a list of FullView entities to a fully populated SequencingReadAnalysis or
   * VariantCallAnalysis object map. This method basically takes a flattened analysis (aka FullView)
   * and converts it to a fully populated analysis object map.
   *
   * @param fullViewEntities List of FullView entities
   * @param analysisType the AnalysisType to filter on
   * @param analysisInstantiatorCallback callback that instantiates the correct analysis object
   * @return list of abstract analyses of the specified analysis type
   */
  public static List<AbstractAnalysis> processAnalysisForType(
      @NonNull List<FullView> fullViewEntities,
      @NonNull AnalysisTypes analysisType,
      @NonNull Function<String, AbstractAnalysis> analysisInstantiatorCallback) {

    // Ensure only processing fullView entities of specified analysisType
    val analysisIdMap =
        fullViewEntities.stream()
            .filter(x -> analysisType == resolveAnalysisType(x.getAnalysisType()))
            .collect(groupingBy(FullView::getAnalysisId));

    val outputList = ImmutableList.<AbstractAnalysis>builder();

    for (val analysisIdEntry : analysisIdMap.entrySet()) {
      val analysisId = analysisIdEntry.getKey();
      val analysisIdResults = analysisIdEntry.getValue();

      val firstAnalysis = analysisIdResults.get(0);
      val analysisState = firstAnalysis.getAnalysisState();
      val analysisInfo = toJsonNode(firstAnalysis.getAnalysisInfo());
      val studyId = firstAnalysis.getStudyId();

      val a = analysisInstantiatorCallback.apply(analysisType.toString());
      a.setAnalysisId(analysisId);
      a.setAnalysisState(analysisState);
      a.setStudy(studyId);
      a.setInfo(analysisInfo);

      val files = extractFiles(analysisIdResults);
      a.setFile(files);

      val compositeEntites = extractCompositeEntities(analysisIdResults);
      a.setSample(compositeEntites);
      outputList.add(a);
    }
    return outputList.build();
  }

  private static <T, R> Set<R> transformImmutableSet(List<T> list, Function<T, R> trFunction) {
    return list.stream().map(trFunction).collect(toImmutableSet());
  }

  private static FileEntity extractFile(FullView fullView) {
    val f =
        FileEntity.builder()
            .analysisId(fullView.getAnalysisId())
            .fileAccess(fullView.getFileAccess())
            .fileMd5sum(fullView.getFileMd5())
            .fileName(fullView.getFileName())
            .fileSize(fullView.getFileSize())
            .fileType(fullView.getFileType())
            .objectId(fullView.getFileObjectId())
            .studyId(fullView.getStudyId())
            .build();
    f.setInfo(toJsonNode(fullView.getFileInfo()));
    return f;
  }

  private static List<FileEntity> extractFiles(List<FullView> fullViews) {
    return ImmutableList.copyOf(transformImmutableSet(fullViews, FullViewConverter::extractFile));
  }

  private static Sample extractSample(FullView fullView) {
    val s =
        Sample.builder()
            .sampleId(fullView.getSampleId())
            .sampleSubmitterId(fullView.getSampleSubmitterId())
            .sampleType(fullView.getSampleType())
            .specimenId(fullView.getSpecimenId())
            .build();
    s.setInfo(toJsonNode(fullView.getSampleInfo()));
    return s;
  }

  private static Specimen extractSpecimen(FullView fullView) {
    val s =
        Specimen.builder()
            .specimenId(fullView.getSpecimenId())
            .specimenSubmitterId(fullView.getSpecimenSubmitterId())
            .specimenType(fullView.getSpecimenType())
            .specimenClass(fullView.getSpecimenClass())
            .donorId(fullView.getDonorId())
            .build();
    s.setInfo(toJsonNode(fullView.getSpecimenInfo()));
    return s;
  }

  private static Donor extractDonor(FullView fullView) {
    val d =
        Donor.builder()
            .donorId(fullView.getDonorId())
            .studyId(fullView.getStudyId())
            .donorSubmitterId(fullView.getDonorSubmitterId())
            .donorGender(fullView.getDonorGender())
            .build();
    d.setInfo(toJsonNode(fullView.getDonorInfo()));
    return d;
  }

  private static Optional<Specimen> extractSpecimenForSample(
      List<FullView> fullViews, String sampleId) {
    return fullViews.stream()
        .filter(x -> x.getSampleId().equals(sampleId))
        .map(FullViewConverter::extractSpecimen)
        .findFirst();
  }

  private static Optional<Donor> extractDonorForSpecimen(
      List<FullView> fullViews, String specimenId) {
    return fullViews.stream()
        .filter(x -> x.getSpecimenId().equals(specimenId))
        .map(FullViewConverter::extractDonor)
        .findFirst();
  }

  private static List<CompositeEntity> extractCompositeEntities(List<FullView> fullViews) {
    val samples = extractSamples(fullViews);
    val list = ImmutableList.<CompositeEntity>builder();
    for (val sample : samples) {
      val specimen =
          extractSpecimenForSample(fullViews, sample.getSampleId())
              .orElseThrow(
                  () ->
                      new IllegalStateException(
                          format(
                              "The specimen '%s' for sampleId '%s' does not exist in the list of FullView entities",
                              sample.getSpecimenId(), sample.getSampleId())));

      val donor =
          extractDonorForSpecimen(fullViews, specimen.getSpecimenId())
              .orElseThrow(
                  () ->
                      new IllegalStateException(
                          format(
                              "The donor '%s' for specimenId '%s' does not exist in the list of FullView entities",
                              specimen.getDonorId(), specimen.getSpecimenId())));
      val c = new CompositeEntity();
      c.setDonor(donor);
      c.setSpecimen(specimen);
      c.setWithSample(sample);
      list.add(c);
    }
    return list.build();
  }

  private static List<Sample> extractSamples(List<FullView> fullViews) {
    return ImmutableList.copyOf(transformImmutableSet(fullViews, FullViewConverter::extractSample));
  }
}
