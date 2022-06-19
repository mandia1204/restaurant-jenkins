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
                    sh 'mkdir report'
                    sh 'ls -l'
                    ansiColor('xterm') {
                        script {
                            def imageTag = TagGenerator.generateImageTag("${env.BUILD_NUMBER}")
                            def customImage = docker.build("${params.repoName}:${imageTag}", ".") // add -f ${dockerfile} if we need a differnet docker file name
                            customImage.inside('-v /home/jenkins/agent/workspace/restaurant-security-build_master/report:/output') {
                                sh 'cp -R /var/www/report/* /output' // can see that test.html is generated
                                sh 'ls -l /output'
                            }
                        }
                    }
                    sh 'ls -l'
                    sh 'ls -l $WORKSPACE/report'
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
