package org.icgc.dcc.sodalite.server.model.utils;

public class Views {
  
  /**
   * Name JSON View enforcing a defined metadata hierarchy: Study --> Donor --> Specimen --> Sample -->* Files
   */
  public static class Document {}
  
  /**
   * Name JSON View allowing looser metadata collections: Study -->* Donors -->* Specimens -->* Samples -->* Files
   */
  public static class Collection {}
}
