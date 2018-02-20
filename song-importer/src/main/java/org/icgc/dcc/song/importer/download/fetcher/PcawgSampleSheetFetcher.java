package org.icgc.dcc.song.importer.download.fetcher;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectReader;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import org.icgc.dcc.song.importer.model.PcawgSampleBean;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static java.nio.file.Files.copy;
import static java.nio.file.Files.exists;
import static java.nio.file.Files.isRegularFile;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

@RequiredArgsConstructor
public class PcawgSampleSheetFetcher {

  private static final String TEMP_FILE_NAME = "pcawgSampleSheet.csv";

  @NonNull private final String url;
  @NonNull private final String tempDir;
  private final boolean persist;
  private final ObjectReader objectReader;

  @SneakyThrows
  public List<PcawgSampleBean> fetch(){
    val file = downloadFile();
    MappingIterator<PcawgSampleBean> it = objectReader.readValues(file);
    return it.readAll();
  }

  @SneakyThrows
  private File downloadFile(){
    val urlObject = new URL(url);
    val tempFilePath = getTempPath();
    if (persist && exists(tempFilePath)){
      checkArgument(isRegularFile(tempFilePath),
          "The path '%s' exists and is not a file", tempFilePath);
    } else {
      copy(urlObject.openStream(), tempFilePath, REPLACE_EXISTING);
    }
    return tempFilePath.toFile();
  }

  private Path getTempPath(){
    return Paths.get(tempDir+File.separator+TEMP_FILE_NAME);
  }


  public static PcawgSampleSheetFetcher createPcawgSampleSheetFetcher(String url, String tempDir, boolean persist,
      ObjectReader pcawgSampleSheetReader) {
    return new PcawgSampleSheetFetcher(url, tempDir, persist, pcawgSampleSheetReader);
  }

}
