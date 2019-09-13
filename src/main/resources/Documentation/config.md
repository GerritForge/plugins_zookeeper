
@PLUGIN@ Configuration
=========================

Global configuration of the @PLUGIN@ plugin is done in the @PLUGIN@.config file in the site's etc directory.

File '@PLUGIN@.config'
--------------------

## Sample configuration.

```
[ref-database "zookeeper"]
  connectString = "localhost:2181"
  rootNode = "/gerrit/multi-site"
  sessionTimeoutMs = 1000
  connectionTimeoutMs = 1000
  retryPolicyBaseSleepTimeMs = 1000
  retryPolicyMaxSleepTimeMs = 3000
  retryPolicyMaxRetries = 3
  casRetryPolicyBaseSleepTimeMs = 100
  casRetryPolicyMaxSleepTimeMs = 100
  casRetryPolicyMaxRetries = 3
  transactionLockTimeoutMs = 1000
```

## Configuration parameters

```ref-database.zookeeper.connectString```
:   Connection string to Zookeeper

```ref-database.zookeeper.rootNode```
:   Root node to use in Zookeeper to store/retrieve information

    Defaults: "/gerrit/multi-site"


```ref-database.zookeeper.sessionTimeoutMs```
:   Zookeeper session timeout in milliseconds

    Defaults: 1000

```ref-database.zookeeper.connectionTimeoutMs```
:   Zookeeper connection timeout in milliseconds

    Defaults: 1000

```ref-database.zookeeper.retryPolicyBaseSleepTimeMs```
:   Configuration for the base sleep timeout in milliseconds of the
    BoundedExponentialBackoffRetry policy used for the Zookeeper connection

    Defaults: 1000

```ref-database.zookeeper.retryPolicyMaxSleepTimeMs```
:   Configuration for the maximum sleep timeout in milliseconds of the
    BoundedExponentialBackoffRetry policy used for the Zookeeper connection

    Defaults: 3000

```ref-database.zookeeper.retryPolicyMaxRetries```
:   Configuration for the maximum number of retries of the
    BoundedExponentialBackoffRetry policy used for the Zookeeper connection

    Defaults: 3

```ref-database.zookeeper.casRetryPolicyBaseSleepTimeMs```
:   Configuration for the base sleep timeout in milliseconds of the
    BoundedExponentialBackoffRetry policy used for the Compare and Swap
    operations on Zookeeper

    Defaults: 1000

```ref-database.zookeeper.casRetryPolicyMaxSleepTimeMs```
:   Configuration for the maximum sleep timeout in milliseconds of the
    BoundedExponentialBackoffRetry policy used for the Compare and Swap
    operations on Zookeeper

    Defaults: 3000

```ref-database.zookeeper.casRetryPolicyMaxRetries```
:   Configuration for the maximum number of retries of the
    BoundedExponentialBackoffRetry policy used for the Compare and Swap
    operations on Zookeeper

    Defaults: 3

```ref-database.zookeeper.transactionLockTimeoutMs```
:   Configuration for the Zookeeper Lock timeout (in milliseconds) used when reading data
    from Zookeeper, applying the git local changes and writing the new objectId
    into Zookeeper

    Defaults: 1000
