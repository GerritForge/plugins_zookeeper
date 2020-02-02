package com.googlesource.gerrit.plugins.validation.dfsrefdb.zookeeper;

public interface StringToGenericConverter {

  public <T> T fromString(String str);

  String getClassType();
}
