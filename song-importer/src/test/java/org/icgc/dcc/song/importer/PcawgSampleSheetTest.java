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

package org.icgc.dcc.song.importer;

import lombok.val;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.icgc.dcc.song.importer.config.PcawgSampleSheetConfig.pcawgSampleSheetReader;
import static org.icgc.dcc.song.importer.strategies.PcawgSampleSheetStrategy.createPcawgSampleSheetStrategy;
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
    val dao = createPcawgSampleSheetStrategy(data);
    val tumourSubmitterSampleId = "C0021RT";
    val expectedMatchedNormalSubmitterSampleId = "C0021RA";
    val actualMatchedNormalSubmitterSampleId = dao.getNormalSubmitterSampleId(
        "DO46877", "RECA-EU", tumourSubmitterSampleId, "RNA-Seq"  );
    assertThat(actualMatchedNormalSubmitterSampleId).isEqualTo(expectedMatchedNormalSubmitterSampleId);
  }



}
