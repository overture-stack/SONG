package org.icgc.dcc.song.server.constants;

import lombok.val;
import org.icgc.dcc.song.server.model.enums.AccessTypes;
import org.icgc.dcc.song.server.model.enums.AnalysisStates;
import org.icgc.dcc.song.server.model.enums.InfoSearchResponseColumns;
import org.icgc.dcc.song.server.model.enums.InfoTypes;
import org.icgc.dcc.song.server.model.enums.UploadStates;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.icgc.dcc.song.server.model.enums.AccessTypes.*;
import static org.icgc.dcc.song.server.model.enums.InfoTypes.*;
import static org.icgc.dcc.song.server.model.enums.UploadStates.*;
import static org.icgc.dcc.song.server.model.enums.AnalysisStates.*;
import static org.icgc.dcc.song.server.model.enums.InfoSearchResponseColumns.*;

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
  public void tesstInfoTypes(){
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
