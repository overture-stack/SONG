package org.icgc.dcc.song.server.importer.convert;

import com.google.common.collect.ImmutableList;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.icgc.dcc.song.server.importer.model.PortalFileMetadata;
import org.icgc.dcc.song.server.importer.resolvers.SampleTypes;
import org.icgc.dcc.song.server.model.entity.Sample;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import static com.google.common.base.Preconditions.checkState;
import static org.icgc.dcc.common.core.util.stream.Collectors.toImmutableSet;
import static org.icgc.dcc.song.server.importer.convert.Converters.NA;

@RequiredArgsConstructor
public class SampleConverter {

  public Set<Sample> convertSamples(@NonNull List<PortalFileMetadata> portalFileMetadatas){
    return portalFileMetadatas.stream()
        .map(SampleConverter::convertToFile)
        .flatMap(Collection::stream)
        .collect(toImmutableSet());
  }

  public static List<Sample> convertToFile(PortalFileMetadata portalFileMetadata){
    val submittedSampleIds = portalFileMetadata.getSubmittedSampleIds();
    val sampleIds = portalFileMetadata.getSampleIds();
    val specimenIds = portalFileMetadata.getSpecimenIds();
    checkState(sampleIds.size() == submittedSampleIds.size(),
        "The size of sampleIds [%s] does not match the size of submittedSampleIds [%s]"
        , sampleIds, submittedSampleIds);
    checkState(sampleIds.size() == specimenIds.size(),
        "The assumption that for every file there is only one specimen and one sample, is false");
    val numSamples = sampleIds.size();
    val sampleEntityList = ImmutableList.<Sample>builder();
    for (int i = 0; i< numSamples; i++){
      val sampleId = sampleIds.get(i);
      val submittedSampleId = submittedSampleIds.get(i);
      val specimenId = specimenIds.get(i);
      val sampleEntity = Sample.create(
          sampleId,
          submittedSampleId,
          specimenId,
          getSampleType(portalFileMetadata),
          getSampleInfo()
      );
      sampleEntityList.add(sampleEntity);
    }
    return sampleEntityList.build();
  }

  public static String getSampleInfo(){
    return NA;
  }

  public static String getSampleType(PortalFileMetadata portalFileMetadata){
    return SampleTypes.resolve(portalFileMetadata).getSampleTypeName();
  }

  public static SampleConverter createSampleConverter() {
    return new SampleConverter();
  }

}
