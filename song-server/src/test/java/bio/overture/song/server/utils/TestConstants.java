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

package bio.overture.song.server.utils;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toUnmodifiableList;
import static lombok.AccessLevel.PRIVATE;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@NoArgsConstructor(access = PRIVATE)
public class TestConstants {

  public static final String DEFAULT_STUDY_ID = "ABC123";
  public static final String DEFAULT_ANALYSIS_ID = "AN1";
  public static final String DEFAULT_FILE_ID = "FI1";
  public static final String DEFAULT_SAMPLE_ID = "SA1";
  public static final String DEFAULT_SPECIMEN_ID = "SP1";
  public static final String DEFAULT_DONOR_ID = "DO1";

  @Getter
  @RequiredArgsConstructor
  public enum SampleTypes  implements  Constant{
    TOTAL_DNA("Total DNA"),
    AMPLIFIED_DNA("Amplified DNA"),
    CTDNA("ctDNA"),
    OTHER_DNA_ENRICHMENTS("Other DNA enrichments"),
    TOTAL_RNA("Total RNA"),
    RIBOZERO_RNA("Ribo-Zero RNA"),
    POLYA_RNA("polyA+ RNA"),
    OTHER_RNA_FRACTIONS("Other RNA fractions");

    @NonNull private final String text;
  }

  @Getter
  @RequiredArgsConstructor
  public enum TumourNormalDesignations implements  Constant{
    NORMAL("Normal"),
    TUMOUR("Tumour");
    @NonNull private final String text;
  }

  @Getter
  @RequiredArgsConstructor
  public enum SpecimenTypes implements Constant{
    NORMAL(                                             "Normal"),
    NORMAL_TISSUE_ADJACENT_TO_PRIMARY_TUMOUR(           "Normal - tissue adjacent to primary tumour"),
    PRIMARY_TUMOUR(                                     "Primary tumour"),
    PRIMARY_TUMOUR_ADJACENT_TO_NORMAL(                  "Primary tumour - adjacent to normal"),
    PRIMARY_TUMOUR_ADDITIONAL_NEW_PRIMARY(              "Primary tumour - additional new primary"),
    RECURRENT_TUMOUR(                                   "Recurrent tumour"),
    METASTATIC_TUMOUR(                                  "Metastatic tumour"),
    METASTATIC_TUMOUR_METASTASIS_LOCAL_TO_LYMPH_NODE(   "Metastatic tumour - metastasis local to lymph node"),
    METASTATIC_TUMOUR_METASTASIS_TO_DISTANT_LOCATION(   "Metastatic tumour - metastasis to distant location"),
    METASTATIC_TUMOUR_ADDITIONAL_METASTATIC(            "Metastatic tumour - additional metastatic"),
    XENOGRAFT_DERIVED_FROM_PRIMARY_TUMOUR(              "Xenograft - derived from primary tumour"),
    XENOGRAFT_DERIVED_FROM_TUMOUR_CELL_LINE(            "Xenograft - derived from tumour cell line"),
    CELL_LINE_DERIVED_FROM_XENOGRAFT_TUMOUR(            "Cell line - derived from xenograft tumour"),
    CELL_LINE_DERIVED_FROM_TUMOUR(                      "Cell line - derived from tumour"),
    CELL_LINE_DERIVED_FROM_NORMAL(                      "Cell line - derived from normal");

    @NonNull private final String text;
  }


  @Getter
  @RequiredArgsConstructor
  public enum SpecimenTissueSources implements Constant{
    BLOOD_DERIVED(                         "Blood derived"),
    BLOOD_DERIVED_BONE_MARROW(             "Blood derived - bone marrow"),
    BLOOD_DERIVED_PERIPHERAL_BLOOD(        "Blood derived - peripheral blood"),
    BONE_MARROW(                           "Bone marrow"),
    BUCCAL_CELL(                           "Buccal cell"),
    LYMPH_NODE(                            "Lymph node"),
    SOLID_TISSUE(                          "Solid tissue"),
    PLASMA(                                "Plasma"),
    SERUM(                                 "Serum"),
    URINE(                                 "Urine"),
    CEREBROSPINAL_FLUID(                   "Cerebrospinal fluid"),
    SPUTUM(                                "Sputum"),
    OTHER(                                 "Other"),
    PLEURAL_EFFUSION(                      "Pleural effusion"),
    MONONUCLEAR_CELLS_FROM_BONE_MARROW(    "Mononuclear cells from bone marrow"),
    SALIVA(                                "Saliva"),
    SKIN(                                  "Skin"),
    INTESTINE(                             "Intestine"),
    BUFFY_COAT(                            "Buffy coat"),
    STOMACH(                               "Stomach"),
    ESOPHAGUS(                             "Esophagus"),
    TONSIL(                                "Tonsil"),
    SPLEEN(                                "Spleen"),
    BONE(                                  "Bone"),
    CEREBELLUM(                            "Cerebellum"),
    ENDOMETRIUM(                           "Endometrium");

    @NonNull private final String text;
  }

  @Getter
  @RequiredArgsConstructor
  public enum Genders implements Constant{
    MALE("Male"),
    FEMALE("Female"),
    OTHER("Other");

    @NonNull private final String text;
  }

  public interface Constant{
    String getText();
  }

  public static final Collection<String> SAMPLE_TYPE = getStringList(SampleTypes.values());

  public static final List<String> TUMOUR_NORMAL_DESIGNATION = getStringList(TumourNormalDesignations.values());

  public static final List<String> SPECIMEN_TYPE = getStringList(SpecimenTypes.values());

  public static final List<String> SPECIMEN_TISSUE_SOURCE = getStringList(SpecimenTissueSources.values());

  public static final Collection<String> GENDER = getStringList(Genders.values());

  private static <T extends Constant> List<String> getStringList(T[] constants){
    return stream(constants).map(Constant::getText).collect(toUnmodifiableList());
  }

}
