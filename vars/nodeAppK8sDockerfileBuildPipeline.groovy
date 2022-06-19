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
                            imageTag = TagGenerator.generateImageTag("${env.BUILD_NUMBER}")
                            docker.build("${params.repoName}:${imageTag}", ".") // add -f ${dockerfile} if we need a differnet docker file name
                            // customImage.inside('-v /home/jenkins/agent/workspace/restaurant-security-build_master/report:/output -u root') {
                            //     sh 'cp -R /var/www/report/* /output'
                            //     sh 'ls -l /output'
                            // }
                        }
                    }
                    sh """
                    docker run --name tocopy -d -u root ${params.repoName}:${imageTag}
                    docker cp tocopy:/var/www/report/* ./report
                    docker rm -f tocopy
                    """
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
