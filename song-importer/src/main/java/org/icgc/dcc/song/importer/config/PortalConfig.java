package org.icgc.dcc.song.importer.config;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.icgc.dcc.song.importer.download.DownloadIterator;
import org.icgc.dcc.song.importer.download.urlgenerator.UrlGenerator;
import org.icgc.dcc.song.importer.model.PortalFileMetadata;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import static org.icgc.dcc.song.importer.convert.PortalUrlConverter.createPortalUrlConverter;
import static org.icgc.dcc.song.importer.download.DownloadIterator.createDownloadIterator;
import static org.icgc.dcc.song.importer.download.queries.impl.DefaultPortalQuery.createDefaultPortalQuery;
import static org.icgc.dcc.song.importer.download.urlgenerator.impl.FilePortalUrlGenerator.createFilePortalUrlGenerator;

@Slf4j
@Configuration
@Lazy
@Getter
public class PortalConfig {

  private static final int PORTAL_MAX_FETCH_SIZE = 100;
  private static final int PORTAL_INITIAL_FROM = 1;

  @Value("${portal.url}")
  private String url;

  @Value("${portal.repoName}")
  private String repoName;

  @Value("${portal.fetchSize}")
  private int fetchSize;

  public UrlGenerator portalUrlGenerator(){
    val portalQuery = createDefaultPortalQuery(repoName);
    return createFilePortalUrlGenerator(url, portalQuery);
  }

  @Bean
  public DownloadIterator<PortalFileMetadata> portalFileMetadataDownloadIterator(){
    log.info("Building PortalFileMetadata DownloadIterator for repoName: {} and portal.url: {} with fetchSize: {}",
        repoName, url, fetchSize);
    val portalUrlGenerator = portalUrlGenerator();
    val urlConverter = createPortalUrlConverter(repoName);
    return createDownloadIterator(urlConverter, portalUrlGenerator, fetchSize,
        PORTAL_MAX_FETCH_SIZE, PORTAL_INITIAL_FROM);
  }

}
