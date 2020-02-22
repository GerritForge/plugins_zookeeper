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

import com.gerritforge.gerrit.globalrefdb.GlobalRefDatabase;
import com.google.common.flogger.FluentLogger;
import com.google.gerrit.extensions.registration.DynamicItem;
import com.google.gerrit.extensions.registration.DynamicSet;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Scopes;
import org.apache.curator.framework.CuratorFramework;

public class ZkValidationModule extends AbstractModule {
  private static final FluentLogger logger = FluentLogger.forEnclosingClass();

  private ZookeeperConfig cfg;

  @Inject
  public ZkValidationModule(ZookeeperConfig cfg) {
    this.cfg = cfg;
  }

  @Override
  protected void configure() {
    logger.atInfo().log("Shared ref-db engine: Zookeeper");
    DynamicItem.bind(binder(), GlobalRefDatabase.class)
        .to(ZkSharedRefDatabase.class)
        .in(Scopes.SINGLETON);
    bind(CuratorFramework.class).toInstance(cfg.buildCurator());
    bind(ZkConnectionConfig.class)
        .toInstance(
            new ZkConnectionConfig(cfg.buildCasRetryPolicy(), cfg.getZkInterProcessLockTimeOut()));

    DynamicSet.setOf(binder(), StringDeserializer.class);
    DynamicSet.bind(binder(), StringDeserializer.class)
        .to(StringToIntDeserializer.class)
        .in(Scopes.SINGLETON);
    DynamicSet.bind(binder(), StringDeserializer.class)
        .to(StringToLongDeserializer.class)
        .in(Scopes.SINGLETON);
    DynamicSet.bind(binder(), StringDeserializer.class)
        .to(StringToObjectIdDeserializer.class)
        .in(Scopes.SINGLETON);
    DynamicSet.bind(binder(), StringDeserializer.class)
        .to(IdentityDeserializer.class)
        .in(Scopes.SINGLETON);
  }
}
