package com.googlesource.gerrit.plugins.validation.dfsrefdb.zookeeper;

public class StringToLongConverter extends StringToGenericConverter {

  public StringToLongConverter() {}

  @Override
  String getClassType() {
    return Long.class.getName();
  }

  @Override
  @SuppressWarnings("unckecked")
  public Long fromString(String str) {
    return Long.parseLong(str);
  }
}
