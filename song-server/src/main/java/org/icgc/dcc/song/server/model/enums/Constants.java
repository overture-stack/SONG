/*
 * Copyright (c) 2017 The Ontario Institute for Cancer Research. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the GNU Public License v3.0.
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
 * SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER
 * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */
package org.icgc.dcc.song.server.model.enums;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

public class Constants {

  public static final Collection<String> ANALYSIS_STATE = list("PUBLISHED","UNPUBLISHED","SUPPRESSED");
  public static final Collection<String> DONOR_GENDER = list("male", "female", "unspecified");
  public static final Collection<String> FILE_TYPE =
      list("FASTA", "FAI", "FASTQ", "BAM", "BAI", "VCF", "TBI", "IDX", "XML");
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

  public static final Collection<String> INFO_TYPE = list( "Study","Donor","Specimen","Sample","File","Analysis",
          "SequencingRead","VariantCall");

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
