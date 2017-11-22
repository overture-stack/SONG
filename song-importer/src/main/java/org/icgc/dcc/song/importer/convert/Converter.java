package org.icgc.dcc.song.importer.convert;

public interface Converter<I, O> {

  O convert(I in);

}
