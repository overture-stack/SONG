/*
 * Copyright (c) 2018. Ontario Institute for Cancer Research
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.icgc.dcc.song.importer.download;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.icgc.dcc.common.core.util.stream.Streams;
import org.icgc.dcc.song.importer.convert.Converter;
import org.icgc.dcc.song.importer.download.urlgenerator.UrlGenerator;

import java.net.URL;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import static java.lang.Math.min;

@Slf4j
public class DownloadIterator<T> implements Iterator<List<T>> {

  /**
   * Config
   */
  private final UrlGenerator urlGenerator;
  private final Converter<URL, List<T>> urlConverter;
  private final int fetchSize;

  /**
   * State
   */
  private int from;
  private boolean first = true;
  private int prevHitsSize = -1;
  private int iterationCount = 0;
  private int totalHitCount = 0;

  public DownloadIterator(@NonNull Converter<URL, List<T>> urlConverter,
      @NonNull UrlGenerator urlGenerator, final int fetchSize,
      final int maxFetchSize, final int initialFrom) {
    this.urlGenerator = urlGenerator;
    this.fetchSize =  resolveFetchSize(fetchSize, maxFetchSize);
    this.urlConverter = urlConverter;
    this.from = initialFrom;
  }

  public Stream<T> stream(){
    return Streams.stream(this)
        .flatMap(Collection::stream);
  }

  @Override
  public boolean hasNext() {
    if (!first && prevHitsSize != -1){
      return prevHitsSize >= fetchSize;
    }
    return true;
  }

  @Override
  public List<T> next() {
    first = false;
    val url = urlGenerator.getUrl(fetchSize, from);
    log.info("URL: {}", url.toString());
    val results = urlConverter.convert(url);
    prevHitsSize = results.size();
    totalHitCount += results.size();
    from += fetchSize;
    log.info("NextFrom: {}  Size: {}  Iteration {}: {} hits", from,prevHitsSize,++iterationCount, totalHitCount);
    return results;
  }

  private static int resolveFetchSize(int fetchSize, int maxFetchSize){
    return min(fetchSize, maxFetchSize);
  }

  public static <T> DownloadIterator<T> createDownloadIterator(@NonNull Converter<URL, List<T>> urlConverter,
      @NonNull UrlGenerator urlGenerator, final int fetchSize,
      final int maxFetchSize, final int initialFrom) {
    return new DownloadIterator<T>(urlConverter, urlGenerator, fetchSize, maxFetchSize, initialFrom);
  }

}
