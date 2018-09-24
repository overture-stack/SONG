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

package bio.overture.song.server.service;

import bio.overture.song.server.model.analysis.AbstractAnalysis;
import bio.overture.song.server.model.analysis.SequencingReadAnalysis;
import bio.overture.song.server.model.analysis.VariantCallAnalysis;
import bio.overture.song.server.utils.TestFiles;
import lombok.val;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static bio.overture.song.core.utils.JsonUtils.fromJson;

public class DeserializationTest {

  @Test
  public void testVariantCallDeserialization(){
    val a1 = fromJson(TestFiles.getJsonNodeFromClasspath("documents/deserialization/variantcall-deserialize1.json"),
        AbstractAnalysis.class);
    val sa1 = ((VariantCallAnalysis) a1).getExperiment();
    assertThat(sa1.getAnalysisId()).isNull();
    assertThat(sa1.getMatchedNormalSampleSubmitterId()).isNull();
    assertThat(sa1.getVariantCallingTool()).isNull();
    assertThat(sa1.getInfo().path("random").isNull()).isTrue();

    val a2 = fromJson(TestFiles.getJsonNodeFromClasspath("documents/deserialization/variantcall-deserialize2.json"),
        AbstractAnalysis.class);
    val sa2 = ((VariantCallAnalysis) a2).getExperiment();
    assertThat(sa2.getAnalysisId()).isNull();
    assertThat(sa2.getMatchedNormalSampleSubmitterId()).isNull();
    assertThat(sa2.getVariantCallingTool()).isNull();

  }

  @Test
  public void testSequencingReadDeserialization(){
    val a1 = fromJson(TestFiles.getJsonNodeFromClasspath("documents/deserialization/sequencingread-deserialize1.json"),
        AbstractAnalysis.class);
    val sa1 = ((SequencingReadAnalysis) a1).getExperiment();
    Assertions.assertThat(sa1.getAnalysisId()).isNull();
    Assertions.assertThat(sa1.getAligned()).isNull();
    Assertions.assertThat(sa1.getAlignmentTool()).isNull();
    Assertions.assertThat(sa1.getInsertSize()).isNull();
    Assertions.assertThat(sa1.getLibraryStrategy()).isEqualTo("WXS");
    Assertions.assertThat(sa1.getPairedEnd()).isNull();
    Assertions.assertThat(sa1.getReferenceGenome()).isNull();
    assertThat(sa1.getInfo().path("random").isNull()).isTrue();

    val a2 = fromJson(TestFiles.getJsonNodeFromClasspath("documents/deserialization/sequencingread-deserialize2.json"),
        AbstractAnalysis.class);
    val sa2 = ((SequencingReadAnalysis) a2).getExperiment();
    Assertions.assertThat(sa2.getAnalysisId()).isNull();
    Assertions.assertThat(sa2.getAligned()).isNull();
    Assertions.assertThat(sa2.getAlignmentTool()).isNull();
    Assertions.assertThat(sa2.getInsertSize()).isNull();
    Assertions.assertThat(sa2.getLibraryStrategy()).isEqualTo("WXS");
    Assertions.assertThat(sa2.getPairedEnd()).isTrue();
    Assertions.assertThat(sa2.getReferenceGenome()).isNull();

  }

}
