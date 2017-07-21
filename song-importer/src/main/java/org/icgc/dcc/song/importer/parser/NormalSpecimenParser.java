package org.icgc.dcc.song.importer.parser;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.NonNull;
import lombok.val;
import org.icgc.dcc.song.importer.resolvers.SpecimenClasses;

import static org.icgc.dcc.song.importer.parser.FieldNames.ANALYZED_ID;
import static org.icgc.dcc.song.importer.parser.FieldNames.ID;
import static org.icgc.dcc.song.importer.parser.FieldNames.SAMPLES;
import static org.icgc.dcc.song.importer.parser.FieldNames.SUBMITTED_ID;
import static org.icgc.dcc.song.importer.parser.FieldNames.TYPE;
import static org.icgc.dcc.song.importer.resolvers.SpecimenClasses.NORMAL;

public class NormalSpecimenParser {

  private final JsonNode normalSpecimen;

  private NormalSpecimenParser(@NonNull JsonNode donor) {
    this.normalSpecimen = findNormalSpecimen(donor);
  }

  public String getNormalSpecimenId() {
    return normalSpecimen.path(ID).textValue();
  }

  public String getNormalSubmittedSpecimenId() {
    return normalSpecimen.path(SUBMITTED_ID).textValue();
  }

  public String getNormalSpecimenType() {
    return normalSpecimen.path(TYPE).textValue();
  }

  public String getNormalSampleId() {
    return getFirstSample(normalSpecimen).path(ID).textValue();
  }

  public String getNormalAnalyzedId() {
    return getFirstSample(normalSpecimen).path(ANALYZED_ID).textValue();
  }

  private static JsonNode getFirstSample(@NonNull JsonNode normalSpecimen) {
    return normalSpecimen.path(SAMPLES).get(0);
  }

  private static JsonNode findNormalSpecimen(@NonNull JsonNode donor) {
    for (int i = 0; i < DonorPortalJsonParser.getNumSpecimens(donor); i++) {
      val specimenJsonNode = DonorPortalJsonParser.getSpecimen(donor, i);
      val specimenType = specimenJsonNode.path(TYPE).textValue();
      val specimenClass = SpecimenClasses.resolve(specimenType);
      if (specimenClass == NORMAL) {
        return specimenJsonNode;
      }
    }
    throw new IllegalStateException(
        String.format("Could not find specimenType matching SpecimenClass: [%s]",
            NORMAL.name()));
  }

  public static NormalSpecimenParser createNormalSpecimenParser(@NonNull JsonNode donor) {
    return new NormalSpecimenParser(donor);
  }
}
