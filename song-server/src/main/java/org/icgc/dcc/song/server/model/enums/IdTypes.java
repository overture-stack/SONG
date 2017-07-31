package org.icgc.dcc.song.server.model.enums;

public enum IdTypes {
 Study,Donor,Specimen,Sample,File,Analysis,SequencingRead,VariantCall;

 public String toString(){
  return name();
 }

}
