package org.icgc.dcc.song.importer.parser;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.NonNull;
import lombok.val;
import org.icgc.dcc.song.importer.resolvers.SpecimenClasses;

import static java.lang.String.format;

public class NormalSpecimenParser {

  private final JsonNode normalSpecimen;

  private NormalSpecimenParser(@NonNull JsonNode donor) {
    this.normalSpecimen = findNormalSpecimen(donor);
  }

  public String getNormalSpecimenId() {
    return normalSpecimen.path(FieldNames.ID).textValue();
  }

  public String getNormalSubmittedSpecimenId() {
    return normalSpecimen.path(FieldNames.SUBMITTED_ID).textValue();
  }

  public String getNormalSpecimenType() {
    return normalSpecimen.path(FieldNames.TYPE).textValue();
  }

  public String getNormalSampleId() {
    return getFirstSample(normalSpecimen).path(FieldNames.ID).textValue();
  }

  public String getNormalAnalyzedId() {
    return getFirstSample(normalSpecimen).path(FieldNames.ANALYZED_ID).textValue();
  }

  private static JsonNode getFirstSample(@NonNull JsonNode normalSpecimen) {
    return normalSpecimen.path(FieldNames.SAMPLES).get(0);
  }

  private static JsonNode findNormalSpecimen(@NonNull JsonNode donor) {
    for (int i = 0; i < DonorPortalJsonParser.getNumSpecimens(donor); i++) {
      val specimenJsonNode = DonorPortalJsonParser.getSpecimen(donor, i);
      val specimenType = specimenJsonNode.path(FieldNames.TYPE).textValue();
      val specimenClass = SpecimenClasses.resolve(specimenType);
      if (specimenClass == SpecimenClasses.NORMAL) {
        return specimenJsonNode;
      }
    }
    throw new IllegalStateException(
        String.format("Could not find specimenType matching SpecimenClass: [%s]",
            SpecimenClasses.NORMAL.name()));
  }

  public static NormalSpecimenParser createNormalSpecimenParser(@NonNull JsonNode donor) {
    return new NormalSpecimenParser(donor);
  }
}
