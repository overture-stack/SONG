package org.icgc.dcc.song.importer.download.urlgenerator.impl;

import com.google.common.base.Joiner;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.icgc.dcc.song.importer.download.urlgenerator.UrlGenerator;

import java.net.URL;

import static java.net.URLEncoder.encode;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.icgc.dcc.song.importer.download.PortalFilterQuerys.buildRepoFilter;

@RequiredArgsConstructor
public class FilePortalUrlGenerator implements UrlGenerator {

  private static final String REPOSITORY_FILES_ENDPOINT = "/api/v1/repository/files";
  private static final String INCLUDE_PARAM = "include=facets";
  private static final Joiner AMPERSAND_JOINER = Joiner.on("&");

  private final String serverUrl;
  private final String repoName;

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

  public static FilePortalUrlGenerator createFilePortalUrlGenerator(String serverUrl, String repoName){
    return new FilePortalUrlGenerator(serverUrl, repoName);
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
    return encode(buildRepoFilter(repoName).toString(), UTF_8.name());
  }


}
