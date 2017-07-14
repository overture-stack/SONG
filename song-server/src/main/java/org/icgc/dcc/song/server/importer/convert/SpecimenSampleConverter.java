package org.icgc.dcc.song.server.importer.convert;

import com.google.common.collect.ImmutableList;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.val;
import org.icgc.dcc.song.server.importer.model.PortalFileMetadata;
import org.icgc.dcc.song.server.importer.resolvers.SampleTypes;
import org.icgc.dcc.song.server.importer.resolvers.SpecimenClasses;
import org.icgc.dcc.song.server.model.entity.Sample;
import org.icgc.dcc.song.server.model.entity.Specimen;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import static com.google.common.base.Preconditions.checkState;
import static org.icgc.dcc.common.core.util.stream.Collectors.toImmutableSet;
import static org.icgc.dcc.song.server.importer.convert.Converters.NA;
import static org.icgc.dcc.song.server.importer.convert.DonorConverter.getDonorId;
import static org.icgc.dcc.song.server.importer.convert.SpecimenSampleConverter.SpecimenSampleTuple.createSpecimenSampleContainer;

@RequiredArgsConstructor
public class SpecimenSampleConverter {

  public Set<SpecimenSampleTuple> convertSpecimenSampleTuples(@NonNull List<PortalFileMetadata> portalFileMetadatas){
    return portalFileMetadatas.stream()
        .map(SpecimenSampleConverter::convertToSpecimenSampleTuple)
        .flatMap(Collection::stream)
        .collect(toImmutableSet());
  }

  public static List<SpecimenSampleTuple> convertToSpecimenSampleTuple(@NonNull PortalFileMetadata portalFileMetadata){
    // extract the lists
    val submittedSampleIds = portalFileMetadata.getSubmittedSampleIds();
    val submittedSpecimenIds = portalFileMetadata.getSubmittedSpecimenIds();
    val specimenTypes = portalFileMetadata.getSpecimenTypes();
    val donorId = getDonorId(portalFileMetadata);
    val sampleIds = portalFileMetadata.getSampleIds();
    val specimenIds = portalFileMetadata.getSpecimenIds();


    // Validate assumptions
    checkState(sampleIds.size() == submittedSampleIds.size(),
        "The size of sampleIds [%s] does not match the size of submittedSampleIds [%s]"
        , sampleIds, submittedSampleIds);
    checkState(sampleIds.size() == specimenIds.size(),
        "The assumption that for every file there is only one specimen and one sample, is false");


    val numSamples = sampleIds.size();
    val list = ImmutableList.<SpecimenSampleTuple>builder();
    for (int i = 0; i< numSamples; i++){
      val specimenType = specimenTypes.get(i);
      val sampleId = sampleIds.get(i);
      val submittedSampleId = submittedSampleIds.get(i);
      val specimenId = specimenIds.get(i);
      val submittedSpecimenId = submittedSpecimenIds.get(i);

      val sampleEntity = Sample.create(
          sampleId,
          submittedSampleId,
          specimenId,
          getSampleType(portalFileMetadata),
          getSampleInfo()
      );

      val specimenEntity = Specimen.create(
          specimenId,
          submittedSpecimenId,
          donorId,
          resolveSpecimenClass(specimenType),
          specimenType,
          getSpecimenInfo()
      );
      list.add(createSpecimenSampleContainer(specimenEntity, sampleEntity));

    }
    return list.build();
  }

  public static String getSpecimenInfo(){
    return NA;
  }

  public static String getSampleInfo(){
    return NA;
  }

  public static String getSampleType(@NonNull PortalFileMetadata portalFileMetadata){
    return SampleTypes.resolve(portalFileMetadata).getSampleTypeName();
  }

  private static String resolveSpecimenClass(String specimenType ){
    return SpecimenClasses.resolve(specimenType).getSpecimenClassName();
  }

  public static SpecimenSampleConverter createSpecimenSampleConverter() {
    return new SpecimenSampleConverter();
  }

  @Value
  public static class SpecimenSampleTuple {

    @NonNull private final Specimen specimen;
    @NonNull private final Sample sample;

    public static SpecimenSampleTuple createSpecimenSampleContainer(Specimen specimen,
        Sample sample) {
      return new SpecimenSampleTuple(specimen, sample);
    }

  }

}
