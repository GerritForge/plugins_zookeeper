# Build

This plugin can be built with Bazel.

## Build in Gerrit tree

Clone or link both this plugin and also [plugins/mulit-site](https://gerrit-review.googlesource.com/admin/repos/plugins%2Fmulti-site) to 
the plugins directory of Gerrit's source tree. 
Put the external dependency Bazel build file into the Gerrit /plugins 
directory, replacing the existing empty one.

```
  cd gerrit/plugins
  rm external_plugin_deps.bzl
  ln -s @PLUGIN@/external_plugin_deps.bzl .
```

From the Gerrit source tree issue the command:

```
  bazel build plugins/@PLUGIN@
```

The output is created in

```
  bazel-bin/plugins/@PLUGIN@/@PLUGIN@.jar
```

This project can be imported into the Eclipse IDE:
Add the plugin name to the `CUSTOM_PLUGINS` and to the
`CUSTOM_PLUGINS_TEST_DEPS` set in Gerrit core in
`tools/bzl/plugins.bzl`, and execute:

```
  ./tools/eclipse/project.py
```

To execute the tests run:

```
  bazel test --test_tag_filters=@PLUGIN@
```

[Back to @PLUGIN@ documentation index][index]

[index]: index.html
