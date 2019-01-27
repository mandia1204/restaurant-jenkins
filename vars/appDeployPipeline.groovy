import restaurant.util.*

def call(Map params) {
    pipeline {
        agent { label 'slave01' }
        stages {
            stage('Copy artifact to ws') {
                steps {
                    copyArtifacts fingerprintArtifacts: true, projectName: "${params.projectName}", selector: lastSuccessful()
                }
            }
            stage('Backup prev artifact and copy new from ws') {
                steps {
                    sh "${env.HOME}/restaurant/deploy/./copy-artifact.sh -a ${params.appName} -w ${env.WORKSPACE}"
                }
            }
            stage('Update docker file') {
                steps {
                    sh "cp ${env.WORKSPACE}/Dockerfile-Prod ${env.HOME}/restaurant/${params.appName}/Dockerfile"
                }
            }
            stage('Build and publish image') {
                steps {
                    script {
                        imageTag = TagGenerator.generateImageTag("${env.BUILD_NUMBER}")
                        sh "${env.HOME}/restaurant/deploy/./build-image.sh -t ${imageTag} -a ${params.appName}"
                    }
                }
            }
            stage('Update App in k8s') {
                steps {
                    ansiColor('xterm') {
                        script {
                            def command = "/home/matt/deploy/./deploy-app.sh -t ${imageTag} -a ${params.appName}"
                            def sshUtil = new SSHUtil()
                            sshUtil.publish configName: 'ansible', command: command
                        }
                    }
                }
            }
        }
    }
}