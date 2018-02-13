package org.icgc.dcc.song.importer.download.urlgenerator.impl;

import com.google.common.base.Joiner;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.icgc.dcc.song.importer.download.queries.PortalQuery;
import org.icgc.dcc.song.importer.download.urlgenerator.UrlGenerator;

import java.net.URL;

import static java.net.URLEncoder.encode;
import static java.nio.charset.StandardCharsets.UTF_8;

@RequiredArgsConstructor
public class FilePortalUrlGenerator implements UrlGenerator {

  private static final String REPOSITORY_FILES_ENDPOINT = "/api/v1/repository/files";
  private static final String INCLUDE_PARAM = "include=facets";
  private static final Joiner AMPERSAND_JOINER = Joiner.on("&");

  @NonNull private final String serverUrl;
  @NonNull private final PortalQuery portalQuery;


  @Override
  @SneakyThrows
  public URL getUrl(int size, int from) {
    return new URL(
        AMPERSAND_JOINER.join(
            serverUrl+ REPOSITORY_FILES_ENDPOINT +"?",
            getFiltersParam(),
            getFromParam(from),
            INCLUDE_PARAM,
            getSizeParam(size)));
  }

  private static String getSizeParam(int size){
    return "size="+size;
  }
  private  static String getFromParam(int from){
    return "from="+from;
  }

  private String getFiltersParam(){
    return "filters="+encodeFilter();
  }

  @SneakyThrows
  private String encodeFilter(){
    return encode(portalQuery.buildQuery().toString(), UTF_8.name());
  }

  public static FilePortalUrlGenerator createFilePortalUrlGenerator(String serverUrl, PortalQuery portalQuery) {
    return new FilePortalUrlGenerator(serverUrl, portalQuery);
  }

}
