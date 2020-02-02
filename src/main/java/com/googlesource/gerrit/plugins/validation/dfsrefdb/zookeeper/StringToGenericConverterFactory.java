package com.googlesource.gerrit.plugins.validation.dfsrefdb.zookeeper;

import com.google.gerrit.extensions.registration.DynamicSet;
import com.google.inject.Inject;

public class StringToGenericConverterFactory {

  DynamicSet<StringToGenericConverter> stringToGenericConverterDynamicSet;

  @Inject
  public StringToGenericConverterFactory(
      DynamicSet<StringToGenericConverter> stringToGenericConverterDynamicSet) {
    this.stringToGenericConverterDynamicSet = stringToGenericConverterDynamicSet;
  }

  public StringToGenericConverter create(final Class clazz) throws Exception {
    for (StringToGenericConverter stringToGenericConverter : stringToGenericConverterDynamicSet) {
      if (stringToGenericConverter.getClassType().equals(clazz.getTypeName())) {
        return stringToGenericConverter;
      }
    }
    // XXX Exception need to be more specific
    throw new Exception("Iterator not found");
  }
}
