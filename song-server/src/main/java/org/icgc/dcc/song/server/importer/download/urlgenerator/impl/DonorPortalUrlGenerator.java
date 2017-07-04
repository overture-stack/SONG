package org.icgc.dcc.song.server.importer.download.urlgenerator.impl;

import com.google.common.base.Joiner;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.icgc.dcc.song.server.importer.download.urlgenerator.UrlGenerator;

import java.net.URL;

@RequiredArgsConstructor
public class DonorPortalUrlGenerator implements UrlGenerator {

  private static final String REPOSITORY_DONORS_ENDPOINT = "/api/v1/donors";
  private static final String FACETS_ONLY_PARAM = "facetsOnly=true" ;
  private static final String FILTERS_PARAM = "filters=%7B%7D" ;
  private static final String INCLUDE_PARAM = "include=facets";
  private static final Joiner AMPERSAND_JOINER = Joiner.on("&");

  private final String serverUrl;

  @Override
  @SneakyThrows
  public URL getUrl(int size, int from) {
    return new URL(
        AMPERSAND_JOINER.join(
            serverUrl+ REPOSITORY_DONORS_ENDPOINT +"?",
            FACETS_ONLY_PARAM,
            FILTERS_PARAM,
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

  public static DonorPortalUrlGenerator createDonorPortalUrlGenerator(String serverUrl){
    return new DonorPortalUrlGenerator(serverUrl);
  }

}
