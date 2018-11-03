import restaurant.util.*

def call(Map params) {
    pipeline {
        agent any
        stages {
            stage('Copy last successful artifact to ws') {
                steps {
                    script {
                        sshUtil = new SSHUtil()
                    }
                    copyArtifacts fingerprintArtifacts: true, projectName: "${params.projectName}", selector: lastSuccessful()
                }
            }
            stage('Publish artifact to server') {
                steps {
                    script {
                        sshUtil.publish configName: 'kube-server', removePrefix: 'dist', sourceFiles:'dist/**', dir: "${params.appName}/dist"
                    }
                }
            }
            stage('Build and publish image') {
                steps {
                    script {
                        imageTag = TagGenerator.generateImageTag("${env.BUILD_NUMBER}")
                        def command = "/restaurant/deploy/./build-image.sh -t ${imageTag} -a ${params.appName}"
                        sshUtil.publish configName: 'kube-server', command: command
                    }
                }
            }
            stage('Update App in k8s') {
                steps {
                    script {
                        def command = "/restaurant/deploy/./update-app.sh -t ${imageTag} -a ${params.appName}"
                        sshUtil.publish configName: 'kube-server', command: command
                    }
                }
            }
        }
    }
}
