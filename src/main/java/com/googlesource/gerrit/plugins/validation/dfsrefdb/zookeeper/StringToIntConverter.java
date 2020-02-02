package com.googlesource.gerrit.plugins.validation.dfsrefdb.zookeeper;

public class StringToIntConverter extends StringToGenericConverter {

  public StringToIntConverter() {}

  @Override
  String getClassType() {
    return Integer.class.getName();
  }

  @Override
  @SuppressWarnings("unckecked")
  public Integer fromString(String str) {
    return Integer.parseInt(str);
  }
}
