package org.icgc.dcc.song.server.importer;

import lombok.RequiredArgsConstructor;
import org.icgc.dcc.song.server.importer.data.DataFactory;

@RequiredArgsConstructor
public class Importer implements  Runnable {

  private final DataFactory dataFactory;
  //add DataFactory
  // build donor and file lists
  // create donor processor and file processor
  // run them

  @Override
  public void run() {

  }

}
