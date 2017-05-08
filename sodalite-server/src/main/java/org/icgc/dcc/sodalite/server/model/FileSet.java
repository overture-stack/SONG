package org.icgc.dcc.sodalite.server.model;

public class FileSet {

  String id;
  String analysis_id;
  String file_id;
}

class VariantCallFileSet extends FileSet {}

class SequencingReadFileSet extends FileSet {}

class MAFFileSet extends FileSet {}
