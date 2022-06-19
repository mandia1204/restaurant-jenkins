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
                    sh 'ls -l'
                    script {
                        def imageTag = TagGenerator.generateImageTag("${env.BUILD_NUMBER}")
                        def customImage = docker.build("${params.repoName}:${imageTag}", ".") // add -f ${dockerfile} if we need a differnet docker file name
                    }
                }
            }
        }
        // post {
        //     always {
        //         step([$class: "TapPublisher", testResults: "report/test/test.out.tap", outputTapToConsole:false, enableSubtests:true ])
        //         junit keepLongStdio: true, testResults: 'report/junit/*.xml'
        //         archiveArtifacts 'dist/**/*'
        //     }
        // }
    }
}
