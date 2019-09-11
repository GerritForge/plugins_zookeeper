load("//tools/bzl:junit.bzl", "junit_tests")
load(
    "//tools/bzl:plugin.bzl",
    "PLUGIN_DEPS",
    "PLUGIN_TEST_DEPS",
    "gerrit_plugin",
)

gerrit_plugin(
    name = "zookeeper",
    srcs = glob(["src/main/java/**/*.java"]),
    manifest_entries = [
        "Gerrit-PluginName: zookeeper",
        "Gerrit-Module: com.googlesource.gerrit.plugins.validation.dfsrefdb.zookeeper.Module",
        "Implementation-Title: zookeeper plugin",
        "Implementation-URL: https://review.gerrithub.io/admin/repos/GerritForge/plugins_zookeeper",
    ],
    resources = glob(["src/main/resources/**/*"]),
    deps = [
        "@curator-client//jar",
        "@curator-framework//jar",
        "@curator-recipes//jar",
        "@zookeeper//jar",
        "//plugins/multi-site",
    ],
)

junit_tests(
    name = "zookeeper_tests",
    srcs = glob(["src/test/java/**/*.java"]),
    resources = glob(["src/test/resources/**/*"]),
    tags = [
        "local",
        "zookeeper",
    ],
    deps = [
        ":zookeeper__plugin_test_deps",
    ],
)

java_library(
    name = "zookeeper__plugin_test_deps",
    testonly = 1,
    visibility = ["//visibility:public"],
    exports = PLUGIN_DEPS + PLUGIN_TEST_DEPS + [
        ":zookeeper__plugin",
        "@curator-framework//jar",
        "@curator-recipes//jar",
        "@curator-test//jar",
        "@curator-client//jar",
        "@zookeeper//jar",
        "//plugins/multi-site:multi-site__plugin_test_deps",
    ],
)
