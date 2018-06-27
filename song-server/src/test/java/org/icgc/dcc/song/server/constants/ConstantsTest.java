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

package org.icgc.dcc.song.server.constants;

import lombok.val;
import org.icgc.dcc.song.server.model.enums.AccessTypes;
import org.icgc.dcc.song.core.model.enums.AnalysisStates;
import org.icgc.dcc.song.core.model.enums.FileTypes;
import org.icgc.dcc.song.server.model.enums.InfoSearchResponseColumns;
import org.icgc.dcc.song.server.model.enums.InfoTypes;
import org.icgc.dcc.song.server.model.enums.UploadStates;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.icgc.dcc.song.server.model.enums.AccessTypes.CONTROLLED;
import static org.icgc.dcc.song.server.model.enums.AccessTypes.OPEN;
import static org.icgc.dcc.song.server.model.enums.AccessTypes.resolveAccessType;
import static org.icgc.dcc.song.core.model.enums.AnalysisStates.PUBLISHED;
import static org.icgc.dcc.song.core.model.enums.AnalysisStates.SUPPRESSED;
import static org.icgc.dcc.song.core.model.enums.AnalysisStates.UNPUBLISHED;
import static org.icgc.dcc.song.core.model.enums.FileTypes.BAI;
import static org.icgc.dcc.song.core.model.enums.FileTypes.BAM;
import static org.icgc.dcc.song.core.model.enums.FileTypes.FAI;
import static org.icgc.dcc.song.core.model.enums.FileTypes.FASTA;
import static org.icgc.dcc.song.core.model.enums.FileTypes.FASTQ;
import static org.icgc.dcc.song.core.model.enums.FileTypes.IDX;
import static org.icgc.dcc.song.core.model.enums.FileTypes.TBI;
import static org.icgc.dcc.song.core.model.enums.FileTypes.VCF;
import static org.icgc.dcc.song.core.model.enums.FileTypes.XML;
import static org.icgc.dcc.song.core.model.enums.FileTypes.resolveFileType;
import static org.icgc.dcc.song.server.model.enums.InfoSearchResponseColumns.ANALYSIS_ID;
import static org.icgc.dcc.song.server.model.enums.InfoSearchResponseColumns.INFO;
import static org.icgc.dcc.song.server.model.enums.InfoTypes.ANALYSIS;
import static org.icgc.dcc.song.server.model.enums.InfoTypes.DONOR;
import static org.icgc.dcc.song.server.model.enums.InfoTypes.FILE;
import static org.icgc.dcc.song.server.model.enums.InfoTypes.SAMPLE;
import static org.icgc.dcc.song.server.model.enums.InfoTypes.SEQUENCING_READ;
import static org.icgc.dcc.song.server.model.enums.InfoTypes.SPECIMEN;
import static org.icgc.dcc.song.server.model.enums.InfoTypes.STUDY;
import static org.icgc.dcc.song.server.model.enums.InfoTypes.VARIANT_CALL;
import static org.icgc.dcc.song.server.model.enums.InfoTypes.resolveInfoType;
import static org.icgc.dcc.song.server.model.enums.UploadStates.CREATED;
import static org.icgc.dcc.song.server.model.enums.UploadStates.SAVED;
import static org.icgc.dcc.song.server.model.enums.UploadStates.UPDATED;
import static org.icgc.dcc.song.server.model.enums.UploadStates.UPLOADED;
import static org.icgc.dcc.song.server.model.enums.UploadStates.VALIDATED;
import static org.icgc.dcc.song.server.model.enums.UploadStates.VALIDATION_ERROR;
import static org.icgc.dcc.song.server.model.enums.UploadStates.resolveState;

public class ConstantsTest {

  @Test
  public void testAnalysisStates(){
    assertThat(PUBLISHED.toString()).isEqualTo("PUBLISHED");
    assertThat(UNPUBLISHED.toString()).isEqualTo("UNPUBLISHED");
    assertThat(SUPPRESSED.toString()).isEqualTo("SUPPRESSED");
    assertThat(AnalysisStates.values()).hasSize(3);
  }

  @Test
  public void testInfoSearchResponseColumns(){
    assertThat(ANALYSIS_ID.toString()).isEqualTo("analysis_id");
    assertThat(INFO.toString()).isEqualTo("info");
    assertThat(InfoSearchResponseColumns.values()).hasSize(2);
  }

  @Test
  public void testAccessTypes(){
    assertThat(CONTROLLED.toString()).isEqualTo("controlled");
    assertThat(OPEN.toString()).isEqualTo("open");
    assertThat(AccessTypes.values()).hasSize(2);
    assertThat(resolveAccessType("open")).isEqualTo(OPEN);
    assertThat(resolveAccessType("controlled")).isEqualTo(CONTROLLED);
    val thrown = catchThrowable(() -> resolveAccessType("somethingNotAccessType"));
    assertThat(thrown).isExactlyInstanceOf(IllegalStateException.class);
  }

  @Test
  public void testFileTypes(){
    assertThat(FASTA.toString()).isEqualTo("FASTA");
    assertThat(FAI.toString()).isEqualTo("FAI");
    assertThat(FASTQ.toString()).isEqualTo("FASTQ");
    assertThat(BAM.toString()).isEqualTo("BAM");
    assertThat(BAI.toString()).isEqualTo("BAI");
    assertThat(VCF.toString()).isEqualTo("VCF");
    assertThat(TBI.toString()).isEqualTo("TBI");
    assertThat(IDX.toString()).isEqualTo("IDX");
    assertThat(XML.toString()).isEqualTo("XML");

    assertThat(FASTA.getExtension()).isEqualTo("fasta");
    assertThat(FAI.getExtension())  .isEqualTo("fai");
    assertThat(FASTQ.getExtension()).isEqualTo("fastq");
    assertThat(BAM.getExtension()).isEqualTo("bam");
    assertThat(BAI.getExtension()).isEqualTo("bai");
    assertThat(VCF.getExtension()).isEqualTo("vcf");
    assertThat(TBI.getExtension()).isEqualTo("tbi");
    assertThat(IDX.getExtension()).isEqualTo("idx");
    assertThat(XML.getExtension()).isEqualTo("xml");

    assertThat(FileTypes.values()).hasSize(9);

    assertThat(resolveFileType("FASTA")).isEqualTo(FASTA);
    assertThat(resolveFileType("FAI")).isEqualTo(FAI);
    assertThat(resolveFileType("FASTQ")).isEqualTo(FASTQ);
    assertThat(resolveFileType("BAM")).isEqualTo(BAM);
    assertThat(resolveFileType("BAI")).isEqualTo(BAI);
    assertThat(resolveFileType("VCF")).isEqualTo(VCF);
    assertThat(resolveFileType("TBI")).isEqualTo(TBI);
    assertThat(resolveFileType("IDX")).isEqualTo(IDX);
    assertThat(resolveFileType("XML")).isEqualTo(XML);

    val thrown = catchThrowable(() -> resolveFileType("somethingThatsNotAFileType"));
    assertThat(thrown).isExactlyInstanceOf(IllegalStateException.class);
  }

  @Test
  public void testInfoTypes(){
    assertThat(STUDY.toString()).isEqualTo("Study");
    assertThat(DONOR.toString()).isEqualTo("Donor");
    assertThat(SPECIMEN.toString()).isEqualTo("Specimen");
    assertThat(SAMPLE.toString()).isEqualTo("Sample");
    assertThat(FILE.toString()).isEqualTo("File");
    assertThat(ANALYSIS.toString()).isEqualTo("Analysis");
    assertThat(SEQUENCING_READ.toString()).isEqualTo("SequencingRead");
    assertThat(VARIANT_CALL.toString()).isEqualTo("VariantCall");
    assertThat(InfoTypes.values()).hasSize(8);

    assertThat(resolveInfoType("Study")).isEqualTo(STUDY);
    assertThat(resolveInfoType("Donor")).isEqualTo(DONOR);
    assertThat(resolveInfoType("Specimen")).isEqualTo(SPECIMEN);
    assertThat(resolveInfoType("Sample")).isEqualTo(SAMPLE);
    assertThat(resolveInfoType("File")).isEqualTo(FILE);
    assertThat(resolveInfoType("Analysis")).isEqualTo(ANALYSIS);
    assertThat(resolveInfoType("SequencingRead")).isEqualTo(SEQUENCING_READ);
    assertThat(resolveInfoType("VariantCall")).isEqualTo(VARIANT_CALL);

    val thrown = catchThrowable(() -> resolveInfoType("somethingThatsNotAnInfoType"));
    assertThat(thrown).isExactlyInstanceOf(IllegalStateException.class);
  }

  @Test
  public void testUploadStates(){
    assertThat(CREATED.toString()).isEqualTo("CREATED");
    assertThat(VALIDATED.toString()).isEqualTo("VALIDATED");
    assertThat(VALIDATION_ERROR.toString()).isEqualTo("VALIDATION_ERROR");
    assertThat(UPLOADED.toString()).isEqualTo("UPLOADED");
    assertThat(UPDATED.toString()).isEqualTo("UPDATED");
    assertThat(SAVED.toString()).isEqualTo("SAVED");
    assertThat(UploadStates.values()).hasSize(6);

    assertThat(resolveState("CREATED")).isEqualTo(CREATED);
    assertThat(resolveState("VALIDATED")).isEqualTo(VALIDATED);
    assertThat(resolveState("VALIDATION_ERROR")).isEqualTo(VALIDATION_ERROR);
    assertThat(resolveState("UPLOADED")).isEqualTo(UPLOADED);
    assertThat(resolveState("UPDATED")).isEqualTo(UPDATED);
    assertThat(resolveState("SAVED")).isEqualTo(SAVED);

    val thrown = catchThrowable(() -> resolveState("notAnUploadState"));
    assertThat(thrown).isExactlyInstanceOf(IllegalStateException.class);
  }

}
