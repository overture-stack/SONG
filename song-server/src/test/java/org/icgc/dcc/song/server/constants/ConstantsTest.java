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
import static org.icgc.dcc.song.server.model.enums.AccessTypes.resolveAccessType;
import static org.icgc.dcc.song.server.model.enums.InfoTypes.resolveInfoType;
import static org.icgc.dcc.song.server.model.enums.UploadStates.resolveState;

public class ConstantsTest {

  @Test
  public void testAnalysisStates(){
    assertThat(AnalysisStates.PUBLISHED.toString()).isEqualTo("PUBLISHED");
    assertThat(AnalysisStates.UNPUBLISHED.toString()).isEqualTo("UNPUBLISHED");
    assertThat(AnalysisStates.SUPPRESSED.toString()).isEqualTo("SUPPRESSED");
    assertThat(AnalysisStates.values()).hasSize(3);
  }

  @Test
  public void testInfoSearchResponseColumns(){
    assertThat(InfoSearchResponseColumns.ANALYSIS_ID.toString()).isEqualTo("analysis_id");
    assertThat(InfoSearchResponseColumns.INFO.toString()).isEqualTo("info");
    assertThat(InfoSearchResponseColumns.values()).hasSize(2);
  }

  @Test
  public void testAccessTypes(){
    assertThat(AccessTypes.CONTROLLED.toString()).isEqualTo("controlled");
    assertThat(AccessTypes.OPEN.toString()).isEqualTo("open");
    assertThat(AccessTypes.values()).hasSize(2);
    assertThat(resolveAccessType("open")).isEqualTo(AccessTypes.OPEN);
    assertThat(resolveAccessType("controlled")).isEqualTo(AccessTypes.CONTROLLED);
    val thrown = catchThrowable(() -> resolveAccessType("somethingNotAccessType"));
    assertThat(thrown).isExactlyInstanceOf(IllegalStateException.class);
  }

  @Test
  public void tesstInfoTypes(){
    assertThat(InfoTypes.STUDY.toString()).isEqualTo("Study");
    assertThat(InfoTypes.DONOR.toString()).isEqualTo("Donor");
    assertThat(InfoTypes.SPECIMEN.toString()).isEqualTo("Specimen");
    assertThat(InfoTypes.SAMPLE.toString()).isEqualTo("Sample");
    assertThat(InfoTypes.FILE.toString()).isEqualTo("File");
    assertThat(InfoTypes.ANALYSIS.toString()).isEqualTo("Analysis");
    assertThat(InfoTypes.SEQUENCING_READ.toString()).isEqualTo("SequencingRead");
    assertThat(InfoTypes.VARIANT_CALL.toString()).isEqualTo("VariantCall");
    assertThat(InfoTypes.values()).hasSize(8);

    assertThat(resolveInfoType("Study")).isEqualTo(InfoTypes.STUDY);
    assertThat(resolveInfoType("Donor")).isEqualTo(InfoTypes.DONOR);
    assertThat(resolveInfoType("Specimen")).isEqualTo(InfoTypes.SPECIMEN);
    assertThat(resolveInfoType("Sample")).isEqualTo(InfoTypes.SAMPLE);
    assertThat(resolveInfoType("File")).isEqualTo(InfoTypes.FILE);
    assertThat(resolveInfoType("Analysis")).isEqualTo(InfoTypes.ANALYSIS);
    assertThat(resolveInfoType("SequencingRead")).isEqualTo(InfoTypes.SEQUENCING_READ);
    assertThat(resolveInfoType("VariantCall")).isEqualTo(InfoTypes.VARIANT_CALL);

    val thrown = catchThrowable(() -> resolveInfoType("somethingThatsNotAnInfoType"));
    assertThat(thrown).isExactlyInstanceOf(IllegalStateException.class);
  }

  @Test
  public void testUploadStates(){
    assertThat(UploadStates.CREATED.toString()).isEqualTo("CREATED");
    assertThat(UploadStates.VALIDATED.toString()).isEqualTo("VALIDATED");
    assertThat(UploadStates.VALIDATION_ERROR.toString()).isEqualTo("VALIDATION_ERROR");
    assertThat(UploadStates.UPLOADED.toString()).isEqualTo("UPLOADED");
    assertThat(UploadStates.UPDATED.toString()).isEqualTo("UPDATED");
    assertThat(UploadStates.SAVED.toString()).isEqualTo("SAVED");
    assertThat(UploadStates.values()).hasSize(6);

    assertThat(resolveState("CREATED")).isEqualTo(UploadStates.CREATED);
    assertThat(resolveState("VALIDATED")).isEqualTo(UploadStates.VALIDATED);
    assertThat(resolveState("VALIDATION_ERROR")).isEqualTo(UploadStates.VALIDATION_ERROR);
    assertThat(resolveState("UPLOADED")).isEqualTo(UploadStates.UPLOADED);
    assertThat(resolveState("UPDATED")).isEqualTo(UploadStates.UPDATED);
    assertThat(resolveState("SAVED")).isEqualTo(UploadStates.SAVED);

    val thrown = catchThrowable(() -> resolveState("notAnUploadState"));
    assertThat(thrown).isExactlyInstanceOf(IllegalStateException.class);
  }

}
