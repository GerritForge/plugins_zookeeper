This plugin is a Zookeeper based implementation of the Global Ref-DB interface.

It is the responsibility of the plugin to store key/pairs of the most recent `sha`
for each specific mutable refs, by the usage of some sort of atomic Compare and
Set operation. This enables the detection of out-of-sync refs across gerrit sites.
It also enables concurrent writes on a multi-master setup by enabling a shared locking
mechanism for refs across multiple nodes.

Originally this code was a part of [multi-site plugin](https://gerrit.googlesource.com/plugins/multi-site/) but currently can be use independently.

## Setup

* Install @PLUGIN@ plugin

Install the zookeeper plugin into the `$GERRIT_SITE/plugins` directory of all
the Gerrit servers that are part of the cluster.

* Configure @PLUGIN@ plugin

Create the `$GERRIT_SITE/etc/@PLUGIN@.config` on all Gerrit servers with the
following basic settings. Where `zookeeperhost` is the host that is running zookeeper
and `2181` is the default zookeeper port, please change them accordingly:

```
[ref-database "zookeeper"]
  connectString = "zookeeperhost:2181"
```

For further information and supported options, refer to [config](config.md)
documentation.
