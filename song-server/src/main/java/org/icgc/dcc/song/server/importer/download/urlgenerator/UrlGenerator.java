package org.icgc.dcc.song.server.importer.download.urlgenerator;

import java.net.URL;

public interface UrlGenerator {

  URL getUrl(int size, int from);

}
