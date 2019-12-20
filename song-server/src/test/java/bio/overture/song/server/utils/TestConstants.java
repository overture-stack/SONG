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

import static lombok.AccessLevel.PRIVATE;

import java.util.Collection;
import java.util.List;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = PRIVATE)
public class TestConstants {

  public static final String DEFAULT_STUDY_ID = "ABC123";
  public static final String DEFAULT_ANALYSIS_ID = "AN1";
  public static final String DEFAULT_FILE_ID = "FI1";
  public static final String DEFAULT_SAMPLE_ID = "SA1";
  public static final String DEFAULT_SPECIMEN_ID = "SP1";
  public static final String DEFAULT_DONOR_ID = "DO1";

  public static final Collection<String> SAMPLE_TYPE =
      List.of(
          "Total DNA",
          "Amplified DNA",
          "ctDNA",
          "Other DNA enrichments",
          "Total RNA",
          "Ribo-Zero RNA",
          "polyA+ RNA",
          "Other RNA fractions");

  public static final Collection<String> TUMOUR_NORMAL_DESIGNATION =
      List.of(
          "Normal",
          "Normal - tissue adjacent to primary tumour",
          "Primary tumour",
          "Primary tumour - adjacent to normal",
          "Primary tumour - additional new primary",
          "Recurrent tumour",
          "Metastatic tumour",
          "Metastatic tumour - metastasis local to lymph node",
          "Metastatic tumour - metastasis to distant location",
          "Metastatic tumour - additional metastatic",
          "Xenograft - derived from primary tumour",
          "Xenograft - derived from tumour cell line",
          "Cell line - derived from xenograft tumour",
          "Cell line - derived from tumour",
          "Cell line - derived from normal");

  public static final Collection<String> SPECIMEN_CLASS =
      List.of("Normal", "Tumour", "Adjacent normal");

  public static final Collection<String> DONOR_GENDER = List.of("Male", "Female", "Other");
}
