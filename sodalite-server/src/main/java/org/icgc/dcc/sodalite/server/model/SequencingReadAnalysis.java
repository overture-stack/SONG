package org.icgc.dcc.sodalite.server.model;

public class SequencingReadAnalysis extends Analysis {

  LibraryStrategy library_strategy;
  Boolean paired_end;
  Long insert_size;
  Boolean aligned;
  String alignment_tool;
  String reference;
}
