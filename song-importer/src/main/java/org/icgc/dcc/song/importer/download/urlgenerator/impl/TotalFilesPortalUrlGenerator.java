package org.icgc.dcc.song.importer.download.urlgenerator.impl;

import com.google.common.base.Joiner;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.icgc.dcc.song.importer.download.urlgenerator.UrlGenerator;

import java.net.URL;

import static java.net.URLEncoder.encode;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.icgc.dcc.song.importer.download.PortalFilterQuerys.buildRepoFilter;

@RequiredArgsConstructor
public class TotalFilesPortalUrlGenerator implements UrlGenerator {

  private static final String REPOSITORY_FILES_ENDPOINT = "/api/v1/repository/files/summary";
  private static final Joiner AMPERSAND_JOINER = Joiner.on("&");

  @NonNull private final String serverUrl;
  @NonNull private final String repoName;

  @Override
  @SneakyThrows
  public URL getUrl(int size, int from) {
    return new URL(
        AMPERSAND_JOINER.join(
            serverUrl+ REPOSITORY_FILES_ENDPOINT +"?",
            getFiltersParam()
        ));
  }

  public static TotalFilesPortalUrlGenerator createTotalFilesPortalUrlGenerator(String serverUrl, String repoName){
    return new TotalFilesPortalUrlGenerator(serverUrl, repoName);
  }


  private String getFiltersParam(){
    return "filters="+encodeFilter();
  }

  @SneakyThrows
  private String encodeFilter(){
    return encode(buildRepoFilter(repoName).toString(), UTF_8.name());
  }

}
