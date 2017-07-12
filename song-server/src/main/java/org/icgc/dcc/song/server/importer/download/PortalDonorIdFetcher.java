package org.icgc.dcc.song.server.importer.download;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.net.URL;

import static org.icgc.dcc.song.core.utils.JsonUtils.read;

@Slf4j
@RequiredArgsConstructor
public class PortalDonorIdFetcher {

  private static final String REPOSITORY_DONORS_ENDPOINT = "/api/v1/donors";
  private static final String INCLUDE_PARAM = "include=specimen";

  private final String serverUrl;

  @SneakyThrows
  public URL getUrl(String donorId) {
    val url = new URL( serverUrl+ REPOSITORY_DONORS_ENDPOINT+"/"+donorId +"?"+INCLUDE_PARAM);
    return url;
  }

  public JsonNode getDonorMetadata(String donorId){
    return read(getUrl(donorId));
  }

  public static PortalDonorIdFetcher createPortalDonorIdFetcher(String serverUrl) {
    return new PortalDonorIdFetcher(serverUrl);
  }

}
