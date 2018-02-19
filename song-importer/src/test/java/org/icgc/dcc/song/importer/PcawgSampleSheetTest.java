package org.icgc.dcc.song.importer;

import lombok.val;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.icgc.dcc.song.importer.config.PcawgSampleSheetConfig.pcawgSampleSheetReader;
import static org.icgc.dcc.song.importer.dao.PcawgSampleSheetDao.createPcawgSampleSheetDao;
import static org.icgc.dcc.song.importer.download.fetcher.PcawgSampleSheetFetcher.createPcawgSampleSheetFetcher;

public class PcawgSampleSheetTest {

  @Test
  public void testMatchedSubmitterSampleId(){
    val url = "http://pancancer.info/data_releases/latest/pcawg_sample_sheet.tsv";
    val tempUrl = "/tmp";
    val persist = true;
    val hasHeader = true;
    char separator = '\t';

    val reader = pcawgSampleSheetReader(hasHeader, separator);
    val fetcher = createPcawgSampleSheetFetcher(url, tempUrl, persist, reader);
    val data = fetcher.fetch();
    val dao = createPcawgSampleSheetDao(data);
    val expectedMatchedNormalSubmitterSampleId = "C0021RA";
    val actualMatchedNormalSubmitterSampleId = dao.findNormalSubmitterSampleId("DO46877", "RECA-EU", "RNA-Seq"  );
    assertThat(actualMatchedNormalSubmitterSampleId).isEqualTo(expectedMatchedNormalSubmitterSampleId);
  }



}
