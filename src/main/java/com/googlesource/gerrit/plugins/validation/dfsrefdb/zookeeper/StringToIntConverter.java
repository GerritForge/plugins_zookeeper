package com.googlesource.gerrit.plugins.validation.dfsrefdb.zookeeper;

public class StringToIntConverter implements StringToGenericConverter {

  public StringToIntConverter() {}

  public String getClassType() {
    return Integer.class.getName();
  }

  @SuppressWarnings("unckecked")
  public Integer fromString(String str) {
    return Integer.parseInt(str);
  }
}
