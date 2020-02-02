package com.googlesource.gerrit.plugins.validation.dfsrefdb.zookeeper;

public class StringToLongConverter implements StringToGenericConverter {

  public StringToLongConverter() {}

  public String getClassType() {
    return Long.class.getName();
  }

  @SuppressWarnings("unckecked")
  public Long fromString(String str) {
    return Long.parseLong(str);
  }
}
