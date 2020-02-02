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

import static com.google.common.truth.Truth.assertThat;

import com.google.gerrit.extensions.registration.DynamicSet;
import com.google.gerrit.reviewdb.client.Project;
import java.util.Optional;
import org.apache.curator.retry.RetryNTimes;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectIdRef;
import org.eclipse.jgit.lib.Ref;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

public class ZkSharedRefDatabaseTest implements RefFixture {
  @Rule public TestName nameRule = new TestName();

  ZookeeperTestContainerSupport zookeeperContainer;

  private ZkSharedRefDatabase zkSharedRefDatabase;

  private StringToGenericConverterFactory stringToGenericConverterFactory =
      new StringToGenericConverterFactory(asDynamicSet());

  private DynamicSet<StringToGenericConverter> asDynamicSet() {
    DynamicSet<StringToGenericConverter> result = new DynamicSet<>();
    result.add("zookeeper", new StringToLongConverter());
    result.add("zookeeper", new StringToIntConverter());
    return result;
  }

  @Before
  public void setup() {
    zookeeperContainer = new ZookeeperTestContainerSupport();
    int SLEEP_BETWEEN_RETRIES_MS = 30;
    long TRANSACTION_LOCK_TIMEOUT = 1000l;
    int NUMBER_OF_RETRIES = 5;

    zkSharedRefDatabase =
        new ZkSharedRefDatabase(
            zookeeperContainer.getCurator(),
            new ZkConnectionConfig(
                new RetryNTimes(NUMBER_OF_RETRIES, SLEEP_BETWEEN_RETRIES_MS),
                TRANSACTION_LOCK_TIMEOUT),
            stringToGenericConverterFactory);
  }

  @After
  public void cleanup() {
    zookeeperContainer.cleanup();
  }

  @Test
  public void shouldCompareAndPutSuccessfully() throws Exception {
    Ref oldRef = refOf(AN_OBJECT_ID_1);
    Ref newRef = refOf(AN_OBJECT_ID_2);
    Project.NameKey projectName = A_TEST_PROJECT_NAME_KEY;

    zookeeperContainer.createRefInZk(projectName, oldRef);

    assertThat(zkSharedRefDatabase.compareAndPut(projectName, oldRef, newRef.getObjectId()))
        .isTrue();
  }

  @Test
  public void shouldCompareAndPutGenericSuccessfullyNewEntry() {
    assertThat(
            zkSharedRefDatabase.compareAndPut(
                A_TEST_PROJECT_NAME_KEY, A_TEST_REF_NAME, null, new Object()))
        .isTrue();
  }

  @Test
  public void shouldFailCompareAndPutGenericIfOutOfSync() {
    Object object1 = new Object();
    assertThat(
            zkSharedRefDatabase.compareAndPut(
                A_TEST_PROJECT_NAME_KEY, A_TEST_REF_NAME, null, object1))
        .isTrue();

    Object object2 = new Object();
    Object object3 = new Object();
    assertThat(
            zkSharedRefDatabase.compareAndPut(
                A_TEST_PROJECT_NAME_KEY, A_TEST_REF_NAME, object2, object3))
        .isFalse();
  }

  @Test
  public void shouldCompareAndPutGenericSuccessfullyUpdateEntry() {
    Object object1 = new Object();
    assertThat(
            zkSharedRefDatabase.compareAndPut(
                A_TEST_PROJECT_NAME_KEY, A_TEST_REF_NAME, null, object1))
        .isTrue();
    Object object2 = new Object();
    assertThat(
            zkSharedRefDatabase.compareAndPut(
                A_TEST_PROJECT_NAME_KEY, A_TEST_REF_NAME, object1, object2))
        .isTrue();
  }

  @Test
  public void shouldFetchLatestObjectIdInZk() throws Exception {
    Ref oldRef = refOf(AN_OBJECT_ID_1);
    Ref newRef = refOf(AN_OBJECT_ID_2);
    Project.NameKey projectName = A_TEST_PROJECT_NAME_KEY;

    zookeeperContainer.createRefInZk(projectName, oldRef);

    assertThat(zkSharedRefDatabase.compareAndPut(projectName, oldRef, newRef.getObjectId()))
        .isTrue();

    assertThat(zkSharedRefDatabase.isUpToDate(projectName, newRef)).isTrue();
    assertThat(zkSharedRefDatabase.isUpToDate(projectName, oldRef)).isFalse();
  }

  @Test
  public void shouldCompareAndPutWithNullOldRefSuccessfully() throws Exception {
    Ref oldRef = refOf(null);
    Ref newRef = refOf(AN_OBJECT_ID_2);
    Project.NameKey projectName = A_TEST_PROJECT_NAME_KEY;

    assertThat(zkSharedRefDatabase.compareAndPut(projectName, oldRef, newRef.getObjectId()))
        .isTrue();
  }

  @Test
  public void shouldCompareAndPutPreviouslyRemovedRefSuccessfully() throws Exception {
    Ref ref = refOf(AN_OBJECT_ID_1);
    Project.NameKey projectName = A_TEST_PROJECT_NAME_KEY;

    zookeeperContainer.createRefInZk(projectName, ref);

    assertThat(zkSharedRefDatabase.compareAndPut(projectName, ref, ObjectId.zeroId())).isTrue();

    Ref zerosRef = refOf(ObjectId.zeroId());
    assertThat(zkSharedRefDatabase.compareAndPut(projectName, zerosRef, AN_OBJECT_ID_1)).isTrue();
  }

  @Test
  public void compareAndPutShouldFailIfTheObjectionHasNotTheExpectedValue() throws Exception {
    Project.NameKey projectName = A_TEST_PROJECT_NAME_KEY;

    Ref oldRef = refOf(AN_OBJECT_ID_1);
    Ref expectedRef = refOf(AN_OBJECT_ID_2);

    zookeeperContainer.createRefInZk(projectName, oldRef);

    assertThat(zkSharedRefDatabase.compareAndPut(projectName, expectedRef, AN_OBJECT_ID_3))
        .isFalse();
  }

  private Ref refOf(ObjectId objectId) {
    return new ObjectIdRef.Unpeeled(Ref.Storage.NETWORK, aBranchRef(), objectId);
  }

  @Test
  public void removeProjectShouldRemoveTheWholePathInZk() throws Exception {
    Project.NameKey projectName = A_TEST_PROJECT_NAME_KEY;
    Ref someRef = refOf(AN_OBJECT_ID_1);

    zookeeperContainer.createRefInZk(projectName, someRef);

    assertThat(zookeeperContainer.readRefValueFromZk(projectName, someRef))
        .isEqualTo(AN_OBJECT_ID_1);

    assertThat(getNumChildrenForPath("/")).isEqualTo(1);

    zkSharedRefDatabase.remove(projectName);

    assertThat(getNumChildrenForPath("/")).isEqualTo(0);
  }

  @Test
  public void shouldReturnIntValueIfExists() throws Exception {
    zkSharedRefDatabase.compareAndPut(A_TEST_PROJECT_NAME_KEY, A_TEST_REF_NAME, null, 1);
    assertThat(
            zkSharedRefDatabase
                .get(A_TEST_PROJECT_NAME_KEY, A_TEST_REF_NAME, Integer.class)
                .isPresent())
        .isTrue();
    assertThat(
            zkSharedRefDatabase.<Integer>get(
                A_TEST_PROJECT_NAME_KEY, A_TEST_REF_NAME, Integer.class))
        .isEqualTo(Optional.of(1));
  }

  @Test
  public void shouldReturnLongValueIfExists() throws Exception {
    zkSharedRefDatabase.compareAndPut(A_TEST_PROJECT_NAME_KEY, A_TEST_REF_NAME, null, 1L);
    assertThat(
            zkSharedRefDatabase
                .get(A_TEST_PROJECT_NAME_KEY, A_TEST_REF_NAME, Long.class)
                .isPresent())
        .isTrue();
    assertThat(zkSharedRefDatabase.<Long>get(A_TEST_PROJECT_NAME_KEY, A_TEST_REF_NAME, Long.class))
        .isEqualTo(Optional.of(1L));
  }

  @Test
  public void shouldReturnEmptyIfDoesntExists() throws Exception {
    Ref ref = refOf(AN_OBJECT_ID_1);
    Project.NameKey projectName = A_TEST_PROJECT_NAME_KEY;

    zookeeperContainer.createRefInZk(projectName, ref);

    assertThat(zkSharedRefDatabase.compareAndPut(projectName, ref, ObjectId.zeroId())).isTrue();

    Ref zerosRef = refOf(ObjectId.zeroId());
    assertThat(zkSharedRefDatabase.compareAndPut(projectName, zerosRef, AN_OBJECT_ID_1)).isTrue();
  }

  @Override
  public String testBranch() {
    return "branch_" + nameRule.getMethodName();
  }

  private int getNumChildrenForPath(String path) throws Exception {
    return zookeeperContainer
        .getCurator()
        .checkExists()
        .forPath(String.format(path))
        .getNumChildren();
  }
}
