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

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.base.Strings;
import com.google.gerrit.extensions.annotations.PluginName;
import com.google.gerrit.server.config.PluginConfigFactory;
import com.google.inject.Inject;
import org.apache.commons.lang.StringUtils;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.BoundedExponentialBackoffRetry;
import org.eclipse.jgit.lib.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZookeeperConfig {
  private static final Logger log = LoggerFactory.getLogger(ZookeeperConfig.class);
  public static final int defaultSessionTimeoutMs;
  public static final int defaultConnectionTimeoutMs;
  public static final String DEFAULT_ZK_CONNECT = "localhost:2181";
  private final int DEFAULT_RETRY_POLICY_BASE_SLEEP_TIME_MS = 1000;
  private final int DEFAULT_RETRY_POLICY_MAX_SLEEP_TIME_MS = 3000;
  private final int DEFAULT_RETRY_POLICY_MAX_RETRIES = 3;
  private final int DEFAULT_CAS_RETRY_POLICY_BASE_SLEEP_TIME_MS = 100;
  private final int DEFAULT_CAS_RETRY_POLICY_MAX_SLEEP_TIME_MS = 300;
  private final int DEFAULT_CAS_RETRY_POLICY_MAX_RETRIES = 3;
  private final int DEFAULT_TRANSACTION_LOCK_TIMEOUT = 1000;

  static {
    CuratorFrameworkFactory.Builder b = CuratorFrameworkFactory.builder();
    defaultSessionTimeoutMs = b.getSessionTimeoutMs();
    defaultConnectionTimeoutMs = b.getConnectionTimeoutMs();
  }

  public static final String SUBSECTION = "zookeeper";
  public static final String KEY_CONNECT_STRING = "connectString";
  public static final String KEY_SESSION_TIMEOUT_MS = "sessionTimeoutMs";
  public static final String KEY_CONNECTION_TIMEOUT_MS = "connectionTimeoutMs";
  public static final String KEY_RETRY_POLICY_BASE_SLEEP_TIME_MS = "retryPolicyBaseSleepTimeMs";
  public static final String KEY_RETRY_POLICY_MAX_SLEEP_TIME_MS = "retryPolicyMaxSleepTimeMs";
  public static final String KEY_RETRY_POLICY_MAX_RETRIES = "retryPolicyMaxRetries";
  public static final String KEY_ROOT_NODE = "rootNode";
  public final String KEY_CAS_RETRY_POLICY_BASE_SLEEP_TIME_MS = "casRetryPolicyBaseSleepTimeMs";
  public final String KEY_CAS_RETRY_POLICY_MAX_SLEEP_TIME_MS = "casRetryPolicyMaxSleepTimeMs";
  public final String KEY_CAS_RETRY_POLICY_MAX_RETRIES = "casRetryPolicyMaxRetries";
  public final String TRANSACTION_LOCK_TIMEOUT_KEY = "transactionLockTimeoutMs";

  private final String connectionString;
  private final String root;
  private final int sessionTimeoutMs;
  private final int connectionTimeoutMs;
  private final int baseSleepTimeMs;
  private final int maxSleepTimeMs;
  private final int maxRetries;
  private final int casBaseSleepTimeMs;
  private final int casMaxSleepTimeMs;
  private final int casMaxRetries;

  public static final String SECTION = "ref-database";
  private final Long transactionLockTimeOut;

  private CuratorFramework build;

  @Inject
  public ZookeeperConfig(PluginConfigFactory cfgFactory, @PluginName String pluginName) {
    Config zkConfig = cfgFactory.getGlobalPluginConfig(pluginName);
    connectionString =
        getString(zkConfig, SECTION, SUBSECTION, KEY_CONNECT_STRING, DEFAULT_ZK_CONNECT);
    root = getString(zkConfig, SECTION, SUBSECTION, KEY_ROOT_NODE, "gerrit/multi-site");
    sessionTimeoutMs =
        getInt(zkConfig, SECTION, SUBSECTION, KEY_SESSION_TIMEOUT_MS, defaultSessionTimeoutMs);
    connectionTimeoutMs =
        getInt(
            zkConfig, SECTION, SUBSECTION, KEY_CONNECTION_TIMEOUT_MS, defaultConnectionTimeoutMs);

    baseSleepTimeMs =
        getInt(
            zkConfig,
            SECTION,
            SUBSECTION,
            KEY_RETRY_POLICY_BASE_SLEEP_TIME_MS,
            DEFAULT_RETRY_POLICY_BASE_SLEEP_TIME_MS);

    maxSleepTimeMs =
        getInt(
            zkConfig,
            SECTION,
            SUBSECTION,
            KEY_RETRY_POLICY_MAX_SLEEP_TIME_MS,
            DEFAULT_RETRY_POLICY_MAX_SLEEP_TIME_MS);

    maxRetries =
        getInt(
            zkConfig,
            SECTION,
            SUBSECTION,
            KEY_RETRY_POLICY_MAX_RETRIES,
            DEFAULT_RETRY_POLICY_MAX_RETRIES);

    casBaseSleepTimeMs =
        getInt(
            zkConfig,
            SECTION,
            SUBSECTION,
            KEY_CAS_RETRY_POLICY_BASE_SLEEP_TIME_MS,
            DEFAULT_CAS_RETRY_POLICY_BASE_SLEEP_TIME_MS);

    casMaxSleepTimeMs =
        getInt(
            zkConfig,
            SECTION,
            SUBSECTION,
            KEY_CAS_RETRY_POLICY_MAX_SLEEP_TIME_MS,
            DEFAULT_CAS_RETRY_POLICY_MAX_SLEEP_TIME_MS);

    casMaxRetries =
        getInt(
            zkConfig,
            SECTION,
            SUBSECTION,
            KEY_CAS_RETRY_POLICY_MAX_RETRIES,
            DEFAULT_CAS_RETRY_POLICY_MAX_RETRIES);

    transactionLockTimeOut =
        getLong(
            zkConfig,
            SECTION,
            SUBSECTION,
            TRANSACTION_LOCK_TIMEOUT_KEY,
            DEFAULT_TRANSACTION_LOCK_TIMEOUT);

    checkArgument(StringUtils.isNotEmpty(connectionString), "zookeeper.%s contains no servers");
  }

  public CuratorFramework buildCurator() {
    if (build == null) {
      this.build =
          CuratorFrameworkFactory.builder()
              .connectString(connectionString)
              .sessionTimeoutMs(sessionTimeoutMs)
              .connectionTimeoutMs(connectionTimeoutMs)
              .retryPolicy(
                  new BoundedExponentialBackoffRetry(baseSleepTimeMs, maxSleepTimeMs, maxRetries))
              .namespace(root)
              .build();
      this.build.start();
    }

    return this.build;
  }

  public Long getZkInterProcessLockTimeOut() {
    return transactionLockTimeOut;
  }

  public RetryPolicy buildCasRetryPolicy() {
    return new BoundedExponentialBackoffRetry(casBaseSleepTimeMs, casMaxSleepTimeMs, casMaxRetries);
  }

  private long getLong(
      Config cfg, String section, String subSection, String name, long defaultValue) {
    try {
      return cfg.getLong(section, subSection, name, defaultValue);
    } catch (IllegalArgumentException e) {
      log.error("invalid value for {}; using default value {}", name, defaultValue);
      log.debug("Failed to retrieve long value: {}", e.getMessage(), e);
      return defaultValue;
    }
  }

  private int getInt(Config cfg, String section, String subSection, String name, int defaultValue) {
    try {
      return cfg.getInt(section, subSection, name, defaultValue);
    } catch (IllegalArgumentException e) {
      log.error("invalid value for {}; using default value {}", name, defaultValue);
      log.debug("Failed to retrieve integer value: {}", e.getMessage(), e);
      return defaultValue;
    }
  }

  private String getString(
      Config cfg, String section, String subsection, String name, String defaultValue) {
    String value = cfg.getString(section, subsection, name);
    if (!Strings.isNullOrEmpty(value)) {
      return value;
    }
    return defaultValue;
  }
}
