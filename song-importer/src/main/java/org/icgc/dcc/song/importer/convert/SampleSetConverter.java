package org.icgc.dcc.song.importer.convert;

import lombok.NonNull;
import lombok.val;
import org.icgc.dcc.song.importer.model.PortalFileMetadata;
import org.icgc.dcc.song.importer.model.SampleSet;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import static org.icgc.dcc.common.core.util.stream.Collectors.toImmutableSet;

public class SampleSetConverter {

  public Set<SampleSet> convertSampleSets(@NonNull List<PortalFileMetadata> portalFileMetadataList){
    return portalFileMetadataList.stream()
        .map(SampleSetConverter::convertToSampleSets)
        .flatMap(Collection::stream)
        .collect(toImmutableSet());
  }

  public static Set<SampleSet> convertToSampleSets(@NonNull PortalFileMetadata portalFileMetadata){
    val analysisId = AnalysisConverter.getAnalysisId(portalFileMetadata);
    return portalFileMetadata
        .getSampleIds()
        .stream()
        .map(sampleId -> SampleSet.createSampleEntry(analysisId, sampleId))
        .collect(toImmutableSet());
  }

  public static SampleSetConverter createSampleSetConverter() {
    return new SampleSetConverter();
  }

}
