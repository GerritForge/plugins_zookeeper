This plugin provides shared-ref db implementation based on Zookeeper technology.

Originally this code was a part of [multi-site plugin](https://gerrit.googlesource.com/plugins/multi-site/) but currently can be use independently.

## Setup

* Install @PLUGIN@ plugin

Install the zookeeper plugin into the `$GERRIT_SITE/plugins` directory of all
the Gerrit servers that are part of the cluster.

* Configure @PLUGIN@ plugin 

Create the `$GERRIT_SITE/etc/@PLUGIN@.config` on all Gerrit servers with the
following basic settings:

```
[ref-database "zookeeper"]
  connectString = "localhost:2181"
```

For further information and supported options, refer to [config](config.md)
documentation.
