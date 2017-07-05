package org.icgc.dcc.song.server.importer.download;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableList;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.icgc.dcc.song.server.importer.download.urlgenerator.UrlGenerator;

import java.net.URL;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import static org.icgc.dcc.common.core.util.stream.Collectors.toImmutableList;
import static org.icgc.dcc.common.core.util.stream.Streams.stream;

@Slf4j
@RequiredArgsConstructor
public class PortalDownloadIterator implements Iterator<List<ObjectNode>>, Iterable<List<ObjectNode>>{

  private static final int PORTAL_FETCH_CLAMP_SIZE = 100;
  private static final ObjectMapper DEFAULT_MAPPER = new ObjectMapper();
  private static final String HITS = "hits";

  private final UrlGenerator urlGenerator;
  private final int portalFetchSize;

  /**
   * State
   */
  private int from = 1;
  private boolean first = true;
  private int prevHitsSize = -1;

  public List<ObjectNode> getFileMetas(){
    return stream((Iterator<List<ObjectNode>>)this)
        .flatMap(Collection::stream)
        .collect(toImmutableList());
  }

  @Override
  public Iterator<List<ObjectNode>> iterator() {
    return this;
  }

  @Override
  public boolean hasNext() {
    if (!first && prevHitsSize != -1){
      return prevHitsSize >= portalFetchSize;
    }
    return true;
  }

  @Override
  public List<ObjectNode> next() {
    first = false;
    val fileMetas = ImmutableList.<ObjectNode>builder();
    val url = urlGenerator.getUrl(portalFetchSize, from);
    val result = read(url);
    val hits = getHits(result);

    for (val hit : hits) {
      val fileMeta = (ObjectNode) hit;
      fileMetas.add(fileMeta);
    }

    prevHitsSize = hits.size();
    from += portalFetchSize;
    return fileMetas.build();
  }

  private static JsonNode getHits(JsonNode result) {
    return result.get(HITS);
  }

  @SneakyThrows
  private static JsonNode read(URL url) {
    return DEFAULT_MAPPER.readTree(url);
  }

  public static PortalDownloadIterator createPortalDownloadIterator(UrlGenerator urlGenerator, int portalFetchSize) {
    return new PortalDownloadIterator(urlGenerator,clampValue(portalFetchSize));
  }
  private static int clampValue(int value){
    if (value > PORTAL_FETCH_CLAMP_SIZE){
      log.warn("Input fetchSize of {} was clamped to {}", value, PORTAL_FETCH_CLAMP_SIZE);
      return PORTAL_FETCH_CLAMP_SIZE;
    } else {
      return value;
    }
  }

  public static PortalDownloadIterator createDefaultPortalDownloadIterator(UrlGenerator urlGenerator) {
    return createPortalDownloadIterator(urlGenerator,PORTAL_FETCH_CLAMP_SIZE);
  }
}
