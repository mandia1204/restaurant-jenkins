import restaurant.util.*

def call(Map params) {
    pipeline {
        agent {
            kubernetes {
                yamlFile 'k8s-jenkins-agent-pod.yaml'
                defaultContainer 'shell'
            }
        }
        stages {
            stage('Build image') {
                steps {
                    ansiColor('xterm') {
                        script {
                            imageTag = TagGenerator.generateImageTag("${env.BUILD_NUMBER}")
                            docker.build("${params.repoName}:${imageTag}", ".") // add -f ${dockerfile} if we need a differnet docker file name
                            def tempContainerName = "tmp-copy-${env.BUILD_ID}"
                            sh """
                            echo 'extracting report and test files...'
                            docker run --name ${tempContainerName}  -d ${params.repoName}:${imageTag} sleep 5000
                            docker cp ${tempContainerName}:/var/www/report/ .
                            docker rm -f ${tempContainerName}
                            """
                        }
                    }
                }
            }
        }
        post {
            always {
                step([$class: "TapPublisher", testResults: "report/test/test.out.tap", outputTapToConsole:false, enableSubtests:true ])
                junit keepLongStdio: true, testResults: 'report/junit/*.xml'
            }
        }
    }
}
