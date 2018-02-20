package org.icgc.dcc.song.importer.config;

import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import lombok.val;
import org.icgc.dcc.song.importer.download.fetcher.PcawgSampleSheetFetcher;
import org.icgc.dcc.song.importer.model.PcawgSampleBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.icgc.dcc.song.importer.download.fetcher.PcawgSampleSheetFetcher.createPcawgSampleSheetFetcher;

@Configuration
public class PcawgSampleSheetConfig {

  @Value("${pcawg-sample-sheet.enable:false}")
  private boolean enable;

  @Value("${pcawg-sample-sheet.url}")
  private String pcawgSampleSheetUrl;

  @Value("${pcawg-sample-sheet.tempDir:/tmp}")
  private String tempDir;

  @Value("${pcawg-sample-sheet.persist:false}")
  private boolean persist;

  @Value("${pcawg-sample-sheet.hasHeader}")
  private boolean hasHeader;

  @Value("${pcawg-sample-sheet.separator}")
  private char separator;

  public static ObjectReader pcawgSampleSheetReader(boolean hasHeader, char separator){
    val csvMapper = new CsvMapper();
    val csvSchema = CsvSchema.emptySchema()
        .withHeader()
        .withColumnSeparator(separator);
    return csvMapper
        .readerFor(PcawgSampleBean.class)
        .with(csvSchema);
  }

  @Bean
  public PcawgSampleSheetFetcher pcawgSampleSheetFetcher(){
    return  createPcawgSampleSheetFetcher(pcawgSampleSheetUrl, tempDir,
        persist, pcawgSampleSheetReader(hasHeader, separator));
  }

}
