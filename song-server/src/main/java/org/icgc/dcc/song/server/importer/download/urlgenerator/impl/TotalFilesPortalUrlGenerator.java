package org.icgc.dcc.song.server.importer.download.urlgenerator.impl;

import com.google.common.base.Joiner;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.icgc.dcc.song.server.importer.download.urlgenerator.UrlGenerator;

import java.net.URL;

import static java.net.URLEncoder.encode;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.icgc.dcc.song.server.importer.download.PortalFilterQuerys.COLLAB_FILTER;

@RequiredArgsConstructor
public class TotalFilesPortalUrlGenerator implements UrlGenerator {

  private static final String REPOSITORY_FILES_ENDPOINT = "/api/v1/repository/files/summary";
  private static final Joiner AMPERSAND_JOINER = Joiner.on("&");

  private final String serverUrl;

  @Override
  @SneakyThrows
  public URL getUrl(int size, int from) {
    return new URL(
        AMPERSAND_JOINER.join(
            serverUrl+ REPOSITORY_FILES_ENDPOINT +"?",
            getFiltersParam()
        ));
  }

  public static TotalFilesPortalUrlGenerator createTotalFilesPortalUrlGenerator(String serverUrl){
    return new TotalFilesPortalUrlGenerator(serverUrl);
  }


  private static String getFiltersParam(){
    return "filters="+encodeFilter();
  }

  @SneakyThrows
  private static String encodeFilter(){
    return encode(COLLAB_FILTER.toString(), UTF_8.name());
  }

}
