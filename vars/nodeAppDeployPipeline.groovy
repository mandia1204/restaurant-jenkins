import restaurant.util.*

def call(Map params) {
    pipeline {
        agent { label 'slave01' }
        stages {
            stage('Copy last successful artifact to ws') {
                steps {
                    copyArtifacts fingerprintArtifacts: true, projectName: "${params.projectName}", selector: lastSuccessful()
                    sh "mv ${env.WORKSPACE}/dist /home/jenkins/restaurant/${params.appName}"
                }
            }
            stage('Build and publish image') {
                steps {
                    script {
                        imageTag = TagGenerator.generateImageTag("${env.BUILD_NUMBER}")
                        sh "~/restaurant/deploy/./build-image.sh -t ${imageTag} -a ${params.appName}"
                    }
                }
            }
            stage('Update App in k8s') {
                steps {
                    script {
                        def command = "/restaurant/deploy/./update-app.sh -t ${imageTag} -a ${params.appName}"
                        def sshUtil = new SSHUtil()
                        sshUtil.publish configName: 'kube-server', command: command
                    }
                }
            }
        }
    }
}