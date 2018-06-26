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
package org.icgc.dcc.song.server.model.enums;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

public class Constants {

  public static final String SEQUENCING_READ_TYPE = "sequencingRead";
  public static final String VARIANT_CALL_TYPE = "variantCall";
  public static final String EMPTY_STRING = "";
  public static final Collection<String> ANALYSIS_STATE = list(AnalysisStates.toStringArray());
  public static final Collection<String> DONOR_GENDER = list("male", "female", "unspecified");
  public static final Collection<String> LIBRARY_STRATEGY =
      list("WGS", "WXS", "RNA-Seq", "ChIP-Seq", "miRNA-Seq", "Bisulfite-Seq", "Validation", "Amplicon", "Other");
  public static final Collection<String> SAMPLE_TYPE =
      list("DNA", "FFPE DNA", "Amplified DNA", "RNA", "Total RNA", "FFPE RNA");
  public static final Collection<String> SPECIMEN_CLASS =
      list("Normal", "Tumour", "Adjacent normal");

  public static final Collection<String> SPECIMEN_TYPE = list("Normal - solid tissue", "Normal - blood derived",
      "Normal - bone marrow", "Normal - tissue adjacent to primary", "Normal - buccal cell",
      "Normal - EBV immortalized", "Normal - lymph node", "Normal - other", "Primary tumour - solid tissue",
      "Primary tumour",
      "Primary tumour - blood derived (peripheral blood)", "Primary tumour - blood derived (bone marrow)",
      "Primary tumour - additional new primary", "Primary tumour - other", "Recurrent tumour - solid tissue",
      "Recurrent tumour - blood derived (peripheral blood)", "Recurrent tumour - blood derived (bone marrow)",
      "Recurrent tumour - other", "Metastatic tumour - NOS", "Metastatic tumour - lymph node",
      "Metastatic tumour - metastasis local to lymph node", "Metastatic tumour - metastasis to distant location",
      "Metastatic tumour - additional metastatic", "Xenograft - derived from primary tumour",
      "Xenograft - derived from tumour cell line", "Cell line - derived from tumour", "Primary tumour - lymph node",
      "Metastatic tumour - other", "Cell line - derived from xenograft tumour");

  public static Collection<String> list(String... s) {
    return Collections.unmodifiableCollection(Arrays.asList(s));
  }

  public static void validate(Collection<String> c, String s) {
    if (c.contains(s)) {
      return;
    }
    throw new IllegalArgumentException(s);
  }

}
