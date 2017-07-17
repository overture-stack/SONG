package org.icgc.dcc.song.importer.download.urlgenerator.impl;

import com.google.common.base.Joiner;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.icgc.dcc.song.importer.download.urlgenerator.UrlGenerator;

import java.net.URL;

import static java.net.URLEncoder.encode;
import static java.nio.charset.StandardCharsets.UTF_8;

@Slf4j
@RequiredArgsConstructor
public class AllDonorPortalUrlGenerator implements UrlGenerator {

  private static final String REPOSITORY_DONORS_ENDPOINT = "/api/v1/donors";
  private static final String FACETS_ONLY_PARAM = "facetsOnly=true" ;
  private static final String INCLUDE_PARAM = "include=facets";
  private static final Joiner AMPERSAND_JOINER = Joiner.on("&");

  private final String serverUrl;

  @Override
  @SneakyThrows
  public URL getUrl(int size, int from) {
    val url = new URL(
        AMPERSAND_JOINER.join(
            serverUrl+ REPOSITORY_DONORS_ENDPOINT +"?",
            getFiltersParam(size, from),
            getFromParam(from),
            INCLUDE_PARAM,
            getSizeParam(size)));
//    log.info("{}: {}",getClass().getSimpleName(),url.toString());
    return url;
  }

  private static String getFiltersParam(int size, int from){
    return "filters="+encodeFilter(size,from);
  }

  @SneakyThrows
  private static String encodeFilter(int size, int from){
    return encode("{}", UTF_8.name());
  }

  private static String getSizeParam(int size){
    return "size="+size;
  }
  private  static String getFromParam(int from){
    return "from="+from;
  }

  public static AllDonorPortalUrlGenerator createAllDonorPortalUrlGenerator(String serverUrl){
    return new AllDonorPortalUrlGenerator(serverUrl);
  }

}
