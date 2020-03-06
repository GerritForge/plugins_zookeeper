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

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import com.gerritforge.gerrit.globalrefdb.GlobalRefDatabase;
import com.gerritforge.gerrit.globalrefdb.GlobalRefDbLockException;
import com.gerritforge.gerrit.globalrefdb.GlobalRefDbSystemError;
import com.google.common.flogger.FluentLogger;
import com.google.gerrit.entities.Project;
import com.google.inject.Inject;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.atomic.AtomicValue;
import org.apache.curator.framework.recipes.atomic.DistributedAtomicValue;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.framework.recipes.locks.Locker;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;

public class ZkSharedRefDatabase implements GlobalRefDatabase {
  private static final FluentLogger logger = FluentLogger.forEnclosingClass();

  private final CuratorFramework client;
  private final RetryPolicy retryPolicy;

  private final Long transactionLockTimeOut;
  private StringDeserializerFactory stringDeserializerFactory;

  @Inject
  public ZkSharedRefDatabase(
      CuratorFramework client,
      ZkConnectionConfig connConfig,
      StringDeserializerFactory stringDeserializerFactory) {
    this.client = client;
    this.retryPolicy = connConfig.curatorRetryPolicy;
    this.transactionLockTimeOut = connConfig.transactionLockTimeout;
    this.stringDeserializerFactory = stringDeserializerFactory;
  }

  @Override
  public boolean isUpToDate(Project.NameKey project, Ref ref) throws GlobalRefDbLockException {
    if (!exists(project, ref.getName())) {
      return true;
    }

    try {
      final byte[] valueInZk = client.getData().forPath(pathFor(project, ref.getName()));

      // Assuming this is a delete node NULL_REF
      if (valueInZk == null) {
        logger.atFine().log(
            "%s:%s not found in Zookeeper, assumed as delete node NULL_REF",
            project, ref.getName());
        return false;
      }

      ObjectId objectIdInSharedRefDb = readObjectId(valueInZk);
      Boolean isUpToDate = objectIdInSharedRefDb.equals(ref.getObjectId());

      if (!isUpToDate) {
        logger.atFine().log(
            "%s:%s is out of sync: local=%s zk=%s",
            project, ref.getName(), ref.getObjectId(), objectIdInSharedRefDb);
      }

      return isUpToDate;
    } catch (Exception e) {
      throw new GlobalRefDbLockException(project.get(), ref.getName(), e);
    }
  }

  @Override
  public void remove(Project.NameKey project) throws GlobalRefDbSystemError {
    try {
      client.delete().deletingChildrenIfNeeded().forPath("/" + project);
    } catch (Exception e) {
      throw new GlobalRefDbSystemError(
          String.format("Not able to delete project '%s'", project), e);
    }
  }

  @Override
  public boolean exists(Project.NameKey project, String refName) throws ZookeeperRuntimeException {
    try {
      return client.checkExists().forPath(pathFor(project, refName)) != null;
    } catch (Exception e) {
      throw new ZookeeperRuntimeException("Failed to check if path exists in Zookeeper", e);
    }
  }

  @Override
  public Locker lockRef(Project.NameKey project, String refName) throws GlobalRefDbLockException {
    InterProcessMutex refPathMutex =
        new InterProcessMutex(client, "/locks" + pathFor(project, refName));
    try {
      return new Locker(refPathMutex, transactionLockTimeOut, MILLISECONDS);
    } catch (Exception e) {
      throw new GlobalRefDbLockException(project.get(), refName, e);
    }
  }

  @Override
  public boolean compareAndPut(Project.NameKey projectName, Ref oldRef, ObjectId newRefValue)
      throws GlobalRefDbSystemError {

    final DistributedAtomicValue distributedRefValue =
        new DistributedAtomicValue(client, pathFor(projectName, oldRef.getName()), retryPolicy);

    try {
      if ((oldRef.getObjectId() == null || oldRef.getObjectId().equals(ObjectId.zeroId()))
          && refNotInZk(projectName, oldRef)) {
        return distributedRefValue.initialize(writeObjectId(newRefValue));
      }
      final ObjectId newValue = newRefValue == null ? ObjectId.zeroId() : newRefValue;
      final AtomicValue<byte[]> newDistributedValue =
          distributedRefValue.compareAndSet(
              writeObjectId(oldRef.getObjectId()), writeObjectId(newValue));

      if (!newDistributedValue.succeeded() && refNotInZk(projectName, oldRef)) {
        return distributedRefValue.initialize(writeObjectId(newRefValue));
      }

      return newDistributedValue.succeeded();
    } catch (Exception e) {
      logger.atWarning().withCause(e).log(
          "Error trying to perform CAS at path %s", pathFor(projectName, oldRef.getName()));
      throw new GlobalRefDbSystemError(
          String.format(
              "Error trying to perform CAS at path %s", pathFor(projectName, oldRef.getName())),
          e);
    }
  }

  @Override
  public <T> boolean compareAndPut(
      Project.NameKey project, String refName, T expectedValue, T newValue)
      throws GlobalRefDbSystemError {

    final DistributedAtomicValue distributedRefValue =
        new DistributedAtomicValue(client, pathFor(project, refName), retryPolicy);

    try {
      if (expectedValue == null && refNotInZk(project, refName)) {
        return distributedRefValue.initialize(writeGeneric(newValue));
      }

      final AtomicValue<byte[]> newDistributedValue =
          distributedRefValue.compareAndSet(writeGeneric(expectedValue), writeGeneric(newValue));

      return newDistributedValue.succeeded();
    } catch (Exception e) {
      String message =
          String.format(
              "Error trying to perform CAS of generic value at path %s", pathFor(project, refName));
      logger.atWarning().withCause(e).log(message);
      throw new GlobalRefDbSystemError(message, e);
    }
  }

  @Override
  public <T> Optional<T> get(Project.NameKey project, String refName, Class<T> clazz)
      throws GlobalRefDbSystemError {
    if (!exists(project, refName)) {
      return Optional.empty();
    }

    try {
      final byte[] valueInZk = client.getData().forPath(pathFor(project, refName));

      if (valueInZk == null) {
        logger.atInfo().log("%s:%s not found in Zookeeper", project, refName);
        return Optional.empty();
      }

      return Optional.of(readGenericType(valueInZk, clazz));

    } catch (Exception e) {
      logger.atSevere().withCause(e).log("Cannot get value for %s:%s", project, refName);
      return Optional.empty();
    }
  }

  private boolean refNotInZk(Project.NameKey projectName, Ref oldRef) throws Exception {
    return client.checkExists().forPath(pathFor(projectName, oldRef.getName())) == null;
  }

  private boolean refNotInZk(Project.NameKey projectName, String oldRef) throws Exception {
    return client.checkExists().forPath(pathFor(projectName, oldRef)) == null;
  }

  static String pathFor(Project.NameKey projectName, String refName) {
    return "/" + projectName + "/" + refName;
  }

  static ObjectId readObjectId(byte[] value) {
    return ObjectId.fromString(value, 0);
  }

  @SuppressWarnings("unchecked")
  <T> T readGenericType(byte[] value, Class<T> clazz) throws DeserializerException {
    StringDeserializer stringDeserializer = stringDeserializerFactory.create(clazz);
    String str = new String(value, StandardCharsets.US_ASCII);
    return (T) stringDeserializer.fromString(str);
  }

  static byte[] writeObjectId(ObjectId value) {
    return ObjectId.toString(value).getBytes(StandardCharsets.US_ASCII);
  }

  static <T> byte[] writeGeneric(T value) {
    return value.toString().getBytes(StandardCharsets.US_ASCII);
  }
}
