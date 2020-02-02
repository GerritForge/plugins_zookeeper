load("//tools/bzl:maven_jar.bzl", "maven_jar")

def external_plugin_deps():
    CURATOR_VER = "4.2.0"

    maven_jar(
        name = "curator-test",
        artifact = "org.apache.curator:curator-test:" + CURATOR_VER,
        sha1 = "98ac2dd69b8c07dcaab5e5473f93fdb9e320cd73",
    )

    maven_jar(
        name = "curator-framework",
        artifact = "org.apache.curator:curator-framework:" + CURATOR_VER,
        sha1 = "5b1cc87e17b8fe4219b057f6025662a693538861",
    )

    maven_jar(
        name = "curator-recipes",
        artifact = "org.apache.curator:curator-recipes:" + CURATOR_VER,
        sha1 = "7f775be5a7062c2477c51533b9d008f70411ba8e",
    )

    maven_jar(
        name = "curator-client",
        artifact = "org.apache.curator:curator-client:" + CURATOR_VER,
        sha1 = "d5d50930b8dd189f92c40258a6ba97675fea3e15",
    )

    maven_jar(
        name = "zookeeper",
        artifact = "org.apache.zookeeper:zookeeper:3.4.14",
        sha1 = "c114c1e1c8172a7cd3f6ae39209a635f7a06c1a1",
    )

    maven_jar(
        name = "global-refdb",
        artifact = "com.gerritforge:global-refdb:3.0.2",
        sha1 = "293a807bd82a284c215213b442b3930258e01f5e",
    )
