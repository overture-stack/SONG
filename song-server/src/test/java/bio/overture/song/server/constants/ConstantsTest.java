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

package bio.overture.song.server.constants;

import static bio.overture.song.core.model.enums.AccessTypes.CONTROLLED;
import static bio.overture.song.core.model.enums.AccessTypes.OPEN;
import static bio.overture.song.core.model.enums.AccessTypes.resolveAccessType;
import static bio.overture.song.core.model.enums.AnalysisStates.PUBLISHED;
import static bio.overture.song.core.model.enums.AnalysisStates.SUPPRESSED;
import static bio.overture.song.core.model.enums.AnalysisStates.UNPUBLISHED;
import static bio.overture.song.core.model.enums.FileTypes.BAI;
import static bio.overture.song.core.model.enums.FileTypes.BAM;
import static bio.overture.song.core.model.enums.FileTypes.FAI;
import static bio.overture.song.core.model.enums.FileTypes.FASTA;
import static bio.overture.song.core.model.enums.FileTypes.FASTQ;
import static bio.overture.song.core.model.enums.FileTypes.IDX;
import static bio.overture.song.core.model.enums.FileTypes.TBI;
import static bio.overture.song.core.model.enums.FileTypes.TGZ;
import static bio.overture.song.core.model.enums.FileTypes.VCF;
import static bio.overture.song.core.model.enums.FileTypes.XML;
import static bio.overture.song.core.model.enums.FileTypes.resolveFileType;
import static bio.overture.song.core.model.enums.FileTypes.values;
import static bio.overture.song.core.testing.SongErrorAssertions.assertExceptionThrownBy;
import static bio.overture.song.server.model.enums.InfoSearchResponseColumns.ANALYSIS_ID;
import static bio.overture.song.server.model.enums.InfoSearchResponseColumns.INFO;
import static bio.overture.song.server.model.enums.InfoTypes.ANALYSIS;
import static bio.overture.song.server.model.enums.InfoTypes.DONOR;
import static bio.overture.song.server.model.enums.InfoTypes.FILE;
import static bio.overture.song.server.model.enums.InfoTypes.SAMPLE;
import static bio.overture.song.server.model.enums.InfoTypes.SEQUENCING_READ;
import static bio.overture.song.server.model.enums.InfoTypes.SPECIMEN;
import static bio.overture.song.server.model.enums.InfoTypes.STUDY;
import static bio.overture.song.server.model.enums.InfoTypes.VARIANT_CALL;
import static bio.overture.song.server.model.enums.InfoTypes.resolveInfoType;
import static bio.overture.song.server.model.enums.UploadStates.CREATED;
import static bio.overture.song.server.model.enums.UploadStates.SAVED;
import static bio.overture.song.server.model.enums.UploadStates.UPDATED;
import static bio.overture.song.server.model.enums.UploadStates.UPLOADED;
import static bio.overture.song.server.model.enums.UploadStates.VALIDATED;
import static bio.overture.song.server.model.enums.UploadStates.VALIDATION_ERROR;
import static bio.overture.song.server.model.enums.UploadStates.resolveState;
import static org.junit.Assert.assertEquals;

import bio.overture.song.core.model.enums.AccessTypes;
import bio.overture.song.core.model.enums.AnalysisStates;
import bio.overture.song.server.model.enums.InfoSearchResponseColumns;
import bio.overture.song.server.model.enums.InfoTypes;
import bio.overture.song.server.model.enums.UploadStates;
import org.junit.Test;

public class ConstantsTest {

  @Test
  public void testAnalysisStates() {
    assertEquals(PUBLISHED.toString(), "PUBLISHED");
    assertEquals(UNPUBLISHED.toString(), "UNPUBLISHED");
    assertEquals(SUPPRESSED.toString(), "SUPPRESSED");
    assertEquals(AnalysisStates.values().length, 3);
  }

  @Test
  public void testInfoSearchResponseColumns() {
    assertEquals(ANALYSIS_ID.toString(), "analysis_id");
    assertEquals(INFO.toString(), "info");
    assertEquals(InfoSearchResponseColumns.values().length, 2);
  }

  @Test
  public void testAccessTypes() {
    assertEquals(CONTROLLED.toString(), "controlled");
    assertEquals(OPEN.toString(), "open");
    assertEquals(AccessTypes.values().length, 2);
    assertEquals(resolveAccessType("open"), OPEN);
    assertEquals(resolveAccessType("controlled"), CONTROLLED);

    assertExceptionThrownBy(
        IllegalStateException.class, () -> resolveAccessType("somethingNotAccessType"));
  }

  @Test
  public void testFileTypes() {
    assertEquals(FASTA.toString(), "FASTA");
    assertEquals(FAI.toString(), "FAI");
    assertEquals(FASTQ.toString(), "FASTQ");
    assertEquals(BAM.toString(), "BAM");
    assertEquals(BAI.toString(), "BAI");
    assertEquals(VCF.toString(), "VCF");
    assertEquals(TBI.toString(), "TBI");
    assertEquals(IDX.toString(), "IDX");
    assertEquals(XML.toString(), "XML");
    assertEquals(TGZ.toString(), "TGZ");

    assertEquals(FASTA.getExtension(), "fasta");
    assertEquals(FAI.getExtension(), "fai");
    assertEquals(FASTQ.getExtension(), "fastq");
    assertEquals(BAM.getExtension(), "bam");
    assertEquals(BAI.getExtension(), "bai");
    assertEquals(VCF.getExtension(), "vcf");
    assertEquals(TBI.getExtension(), "tbi");
    assertEquals(IDX.getExtension(), "idx");
    assertEquals(XML.getExtension(), "xml");
    assertEquals(TGZ.getExtension(), "tgz");

    assertEquals(values().length, 10);

    assertEquals(resolveFileType("FASTA"), FASTA);
    assertEquals(resolveFileType("FAI"), FAI);
    assertEquals(resolveFileType("FASTQ"), FASTQ);
    assertEquals(resolveFileType("BAM"), BAM);
    assertEquals(resolveFileType("BAI"), BAI);
    assertEquals(resolveFileType("VCF"), VCF);
    assertEquals(resolveFileType("TBI"), TBI);
    assertEquals(resolveFileType("IDX"), IDX);
    assertEquals(resolveFileType("XML"), XML);
    assertEquals(resolveFileType("TGZ"), TGZ);

    assertExceptionThrownBy(
        IllegalStateException.class, () -> resolveFileType("somethingThatsNotAFileType"));
  }

  @Test
  public void testInfoTypes() {
    assertEquals(STUDY.toString(), "Study");
    assertEquals(DONOR.toString(), "Donor");
    assertEquals(SPECIMEN.toString(), "Specimen");
    assertEquals(SAMPLE.toString(), "Sample");
    assertEquals(FILE.toString(), "File");
    assertEquals(ANALYSIS.toString(), "Analysis");
    assertEquals(SEQUENCING_READ.toString(), "SequencingRead");
    assertEquals(VARIANT_CALL.toString(), "VariantCall");
    assertEquals(InfoTypes.values().length, 8);

    assertEquals(resolveInfoType("Study"), STUDY);
    assertEquals(resolveInfoType("Donor"), DONOR);
    assertEquals(resolveInfoType("Specimen"), SPECIMEN);
    assertEquals(resolveInfoType("Sample"), SAMPLE);
    assertEquals(resolveInfoType("File"), FILE);
    assertEquals(resolveInfoType("Analysis"), ANALYSIS);
    assertEquals(resolveInfoType("SequencingRead"), SEQUENCING_READ);
    assertEquals(resolveInfoType("VariantCall"), VARIANT_CALL);

    assertExceptionThrownBy(
        IllegalStateException.class, () -> resolveInfoType("somethingThatsNotAnInfoType"));
  }

  @Test
  public void testUploadStates() {
    assertEquals(CREATED.toString(), "CREATED");
    assertEquals(VALIDATED.toString(), "VALIDATED");
    assertEquals(VALIDATION_ERROR.toString(), "VALIDATION_ERROR");
    assertEquals(UPLOADED.toString(), "UPLOADED");
    assertEquals(UPDATED.toString(), "UPDATED");
    assertEquals(SAVED.toString(), "SAVED");
    assertEquals(UploadStates.values().length, 6);

    assertEquals(resolveState("CREATED"), CREATED);
    assertEquals(resolveState("VALIDATED"), VALIDATED);
    assertEquals(resolveState("VALIDATION_ERROR"), VALIDATION_ERROR);
    assertEquals(resolveState("UPLOADED"), UPLOADED);
    assertEquals(resolveState("UPDATED"), UPDATED);
    assertEquals(resolveState("SAVED"), SAVED);

    assertExceptionThrownBy(IllegalStateException.class, () -> resolveState("notAnUploadState"));
  }
}
