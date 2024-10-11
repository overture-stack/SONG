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

package bio.overture.song.server.constants;

import static bio.overture.song.core.model.enums.AccessTypes.*;
import static bio.overture.song.core.model.enums.AnalysisStates.*;
import static bio.overture.song.core.testing.SongErrorAssertions.assertExceptionThrownBy;
import static bio.overture.song.server.model.enums.InfoSearchResponseColumns.ANALYSIS_ID;
import static bio.overture.song.server.model.enums.InfoSearchResponseColumns.INFO;
import static bio.overture.song.server.model.enums.InfoTypes.*;
import static bio.overture.song.server.model.enums.UploadStates.*;
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
  public void testInfoTypes() {
    assertEquals(STUDY.toString(), "Study");
    assertEquals(FILE.toString(), "File");
    assertEquals(SEQUENCING_READ.toString(), "SequencingRead");
    assertEquals(VARIANT_CALL.toString(), "VariantCall");
    assertEquals(InfoTypes.values().length, 7);

    assertEquals(resolveInfoType("Study"), STUDY);
    assertEquals(resolveInfoType("File"), FILE);
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
