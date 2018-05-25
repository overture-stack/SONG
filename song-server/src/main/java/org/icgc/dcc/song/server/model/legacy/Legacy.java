package org.icgc.dcc.song.server.model.legacy;

public interface Legacy {

  String getId();

  String getGnosId();

  String getFileName();

  String getProjectCode();

  String getAccess();

  void setId(String id);

  void setGnosId(String gnosId);

  void setFileName(String fileName);

  void setProjectCode(String projectCode);

  void setAccess(String access);
}
