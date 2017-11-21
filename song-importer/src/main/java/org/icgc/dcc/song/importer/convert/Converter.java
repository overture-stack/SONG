package org.icgc.dcc.song.importer.convert;

public interface Converter<IN, OUT> {

  OUT convert(IN in);
}
