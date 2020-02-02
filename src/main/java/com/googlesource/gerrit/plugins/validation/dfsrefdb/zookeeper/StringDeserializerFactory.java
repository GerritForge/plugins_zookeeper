// Copyright (C) 2020 The Android Open Source Project
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.googlesource.gerrit.plugins.validation.dfsrefdb.zookeeper;

import com.google.gerrit.extensions.registration.DynamicSet;
import com.google.inject.Inject;

public class StringDeserializerFactory {

  private final DynamicSet<StringDeserializer> stringToGenericDeserializers;

  @Inject
  public StringDeserializerFactory(
      DynamicSet<StringDeserializer> stringToGenericDeserializers) {
    this.stringToGenericDeserializers = stringToGenericDeserializers;
  }

  public StringDeserializer create(final Class clazz) throws DeserializerException {
    for (StringDeserializer stringDeserializer : stringToGenericDeserializers) {
      if (stringDeserializer.getTypeClass().getName().equals(clazz.getTypeName())) {
        return stringDeserializer;
      }
    }
    throw new DeserializerException("No serializer registered for class " + clazz.getName());
  }
}
