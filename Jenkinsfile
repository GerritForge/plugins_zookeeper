pipeline {
    options { checkoutToSubdirectory('zookeeper') }
    agent { label 'bazel-debian' }
    stages {
        stage('GJF') {
            steps {
                sh "find zookeeper -name '*.java' | xargs /home/jenkins/format/google-java-format-1.7 -i"
                script {
                    def formatOut = sh (script: 'cd zookeeper && git status --porcelain', returnStdout: true)
                    if (formatOut.trim()) {
                        def files = formatOut.split('\n').collect { it.split(' ').last() }
                        files.each { gerritComment path:it, message: 'Needs reformatting with GJF' }
                        gerritReview labels: [Formatting: -1]
                    } else {
                        gerritReview labels: [Formatting: 1]
                    }
                }
            }
        }
        stage('Copyright') {
            steps {
                script {
                    def formatOut = sh (script: "cd zookeeper && find . -type f -name '*.java'   -exec awk '!/Copyright \(C\) [[:digit:]][[:digit:]][[:digit:]][[:digit:]] The Android Open Source Project/ {print FILENAME} {nextfile}' {} +", returnStdout: true)
                    if (formatOut.trim()) {
                        def files = formatOut.split('\n').collect { it.split(' ').last() }
                        files.each { gerritComment path:it, message: 'Missing Copyright header' }
                        gerritReview labels: [Formatting: -1]
                    } else {
                        gerritReview labels: [Formatting: 1]
                    }
                }
            }
        }
        stage('build') {
            steps {
                gerritReview labels: [Verified: 0], message: "Build started: ${env.BUILD_URL}"
                sh "git clone --recursive -b stable-3.0 https://gerrit.googlesource.com/gerrit"
                sh 'cd gerrit/plugins && ln -sf ../../zookeeper . && ln -sf zookeeper/external_plugin_deps.bzl .'
                sh 'cd gerrit && bazelisk build plugins/zookeeper && bazelisk test plugins/zookeeper:zookeeper_tests'
            }
        }
    }
    post {
        success { gerritReview labels: [Verified: 1] }
        unstable { gerritReview labels: [Verified: 0], message: "Build is unstable: ${env.BUILD_URL}" }
        failure { gerritReview labels: [Verified: -1], message: "Build failed: ${env.BUILD_URL}" }
    }
}
