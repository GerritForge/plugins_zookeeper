package com.googlesource.gerrit.plugins.validation.dfsrefdb.zookeeper;

public abstract class StringToGenericConverter {

  public StringToGenericConverter() {}

  public <T> T fromString(String str) {
    return (T) str;
  }

  abstract String getClassType();
}
