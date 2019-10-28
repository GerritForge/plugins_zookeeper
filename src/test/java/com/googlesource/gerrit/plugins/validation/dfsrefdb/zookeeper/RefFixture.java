// Copyright (C) 2019 The Android Open Source Project
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

import com.google.gerrit.entities.Project;
import com.google.gerrit.entities.RefNames;
import org.eclipse.jgit.lib.ObjectId;
import org.junit.Ignore;

@Ignore
public interface RefFixture {

  String A_TEST_PROJECT_NAME = "A_TEST_PROJECT_NAME";
  Project.NameKey A_TEST_PROJECT_NAME_KEY = Project.nameKey(A_TEST_PROJECT_NAME);
  ObjectId AN_OBJECT_ID_1 = new ObjectId(1, 2, 3, 4, 5);
  ObjectId AN_OBJECT_ID_2 = new ObjectId(1, 2, 3, 4, 6);
  ObjectId AN_OBJECT_ID_3 = new ObjectId(1, 2, 3, 4, 7);
  String A_TEST_REF_NAME = "refs/heads/master";

  default String aBranchRef() {
    return RefNames.REFS_HEADS + testBranch();
  }

  default String testBranch() {
    return "aTestBranch";
  }
}
