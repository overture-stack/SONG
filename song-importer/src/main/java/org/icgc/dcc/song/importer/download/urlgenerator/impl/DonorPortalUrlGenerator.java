package org.icgc.dcc.song.importer.download.urlgenerator.impl;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Joiner;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.icgc.dcc.song.importer.download.urlgenerator.UrlGenerator;

import java.net.URL;
import java.util.List;

import static java.net.URLEncoder.encode;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.icgc.dcc.common.core.json.JsonNodeBuilders.object;
import static org.icgc.dcc.song.importer.download.urlgenerator.UrlGenerator.createIs;

@Slf4j
@RequiredArgsConstructor
public class DonorPortalUrlGenerator implements UrlGenerator {

  private static final String REPOSITORY_DONORS_ENDPOINT = "/api/v1/donors";
  private static final String FACETS_ONLY_PARAM = "facetsOnly=true" ;
  private static final String INCLUDE_PARAM = "include=facets";
  private static final Joiner AMPERSAND_JOINER = Joiner.on("&");

  private final String serverUrl;
  private final List<String> donorIds;

  @Override
  @SneakyThrows
  public URL getUrl(int size, int from) {
    val url = new URL(
        AMPERSAND_JOINER.join(
            serverUrl+ REPOSITORY_DONORS_ENDPOINT +"?",
            getFiltersParam(size, from),
            getFromParam(1),
            INCLUDE_PARAM,
            getSizeParam(size+1)));
    log.info("{}: {}",getClass().getSimpleName(),url.toString());
    return url;
  }

  private static String getSizeParam(int size){
    return "size="+size;
  }
  private  static String getFromParam(int from){
    return "from="+from;
  }

  public static DonorPortalUrlGenerator createDonorPortalUrlGenerator(String serverUrl, List<String> donorIds){
    return new DonorPortalUrlGenerator(serverUrl, donorIds);
  }

  private String getFiltersParam(int size, int from){
    return "filters="+encodeFilter(size,from);
  }

  @SneakyThrows
  private String encodeFilter(int size, int from){
    return encode(createFilter(size,from).toString(), UTF_8.name());
  }

  private ObjectNode createFilter(int size, int from){
    val start = Math.max(from - 2, 0); //inclusive
    val end = start + 1 + size; //exclusive
    val sublist = donorIds.subList(start, end);
    log.info("Start: {}  End: {}  Size: {}   From: {}   SublistSize: {}", start, end, size, from, sublist.size());
    return object()
        .with("donor",
            object()
                .with("id", createIs(sublist))
        )
        .end();
  }


}
