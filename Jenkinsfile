pipeline {
    agent { label 'bazel-debian' }
    stages {
        stage('GJF') {
            steps {
                sh "find . -name '*.java' | xargs /home/jenkins/format/google-java-format-1.7 -i"
                script {
                    def formatOut = sh (script: 'git status --porcelain', returnStdout: true)
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
        stage('build') {
            steps {
                gerritReview labels: [Verified: 0], message: "Build started: ${env.BUILD_URL}"
                sh "git clone --recursive -b ${env.GERRIT_BRANCH} https://gerrit.googlesource.com/gerrit"
                sh 'cd gerrit/plugins && ln -s ../../. zookeeper && ln -s zookeeper/external_plugin_deps.bzl .'
                sh 'cd gerrit && bazel build plugins/zookeeper && bazel test plugins/zookeeper:zokeeper_tests'
            }
        }
    }
    post {
        success { gerritReview labels: [Verified: 1] }
        unstable { gerritReview labels: [Verified: 0], message: "Build is unstable: ${env.BUILD_URL}" }
        failure { gerritReview labels: [Verified: -1], message: "Build failed: ${env.BUILD_URL}" }
    }
}
