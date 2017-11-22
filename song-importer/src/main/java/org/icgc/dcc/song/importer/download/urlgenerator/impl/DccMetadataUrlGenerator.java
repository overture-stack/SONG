package org.icgc.dcc.song.importer.download.urlgenerator.impl;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import org.icgc.dcc.song.importer.download.urlgenerator.UrlGenerator;

import java.net.URL;

import static java.lang.String.format;

@RequiredArgsConstructor
public class DccMetadataUrlGenerator implements UrlGenerator {

  @NonNull private final String serverUrl;

  @Override
  @SneakyThrows
  public URL getUrl(int size, int from) {
    val page = calcPage(size, from);
    val urlString = format("%s/entities?size=%s&page=%s", serverUrl, size, page);
    return new URL(urlString);
  }

  private int calcPage(int size, int from){
    return from/size;
  }

  public static DccMetadataUrlGenerator createDccMetadataUrlGenerator(String serverUrl) {
    return new DccMetadataUrlGenerator(serverUrl);
  }

}
