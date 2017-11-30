package org.icgc.dcc.song.server.service;

import lombok.val;
import org.icgc.dcc.song.server.model.analysis.Analysis;
import org.icgc.dcc.song.server.model.analysis.SequencingReadAnalysis;
import org.icgc.dcc.song.server.model.analysis.VariantCallAnalysis;
import org.icgc.dcc.song.server.utils.TestFiles;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.icgc.dcc.song.core.utils.JsonUtils.fromJson;

public class DeserializationTest {

  @Test
  public void testVariantCallDeserialization(){
    val a1 = fromJson(TestFiles.getJsonNodeFromClasspath("documents/deserialization/variantcall-deserialize1.json"),
        Analysis.class);
    val sa1 = ((VariantCallAnalysis) a1).getExperiment();
    assertThat(sa1.getAnalysisId()).isNull();
    assertThat(sa1.getMatchedNormalSampleSubmitterId()).isNull();
    assertThat(sa1.getVariantCallingTool()).isNull();
    assertThat(sa1.getInfo().path("random").isNull()).isTrue();

    val a2 = fromJson(TestFiles.getJsonNodeFromClasspath("documents/deserialization/variantcall-deserialize2.json"),
        Analysis.class);
    val sa2 = ((VariantCallAnalysis) a2).getExperiment();
    assertThat(sa2.getAnalysisId()).isNull();
    assertThat(sa2.getMatchedNormalSampleSubmitterId()).isNull();
    assertThat(sa2.getVariantCallingTool()).isNull();

  }

  @Test
  public void testSequencingReadDeserialization(){
    val a1 = fromJson(TestFiles.getJsonNodeFromClasspath("documents/deserialization/sequencingread-deserialize1.json"),
        Analysis.class);
    val sa1 = ((SequencingReadAnalysis) a1).getExperiment();
    assertThat(sa1.getAnalysisId()).isNull();
    assertThat(sa1.getAligned()).isNull();
    assertThat(sa1.getAlignmentTool()).isNull();
    assertThat(sa1.getInsertSize()).isNull();
    assertThat(sa1.getLibraryStrategy()).isEqualTo("WXS");
    assertThat(sa1.getPairedEnd()).isNull();
    assertThat(sa1.getReferenceGenome()).isNull();
    assertThat(sa1.getInfo().path("random").isNull()).isTrue();

    val a2 = fromJson(TestFiles.getJsonNodeFromClasspath("documents/deserialization/sequencingread-deserialize2.json"),
        Analysis.class);
    val sa2 = ((SequencingReadAnalysis) a2).getExperiment();
    assertThat(sa2.getAnalysisId()).isNull();
    assertThat(sa2.getAligned()).isNull();
    assertThat(sa2.getAlignmentTool()).isNull();
    assertThat(sa2.getInsertSize()).isNull();
    assertThat(sa2.getLibraryStrategy()).isEqualTo("WXS");
    assertThat(sa2.getPairedEnd()).isTrue();
    assertThat(sa2.getReferenceGenome()).isNull();

  }

}
