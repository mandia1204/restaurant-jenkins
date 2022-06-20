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
                            imageName = "${params.repoName}:${imageTag}"
                            docker.build(imageName, ".") // add -f ${dockerfile} if we need a differnet docker file name
                            def tempContainerName = "tmp-copy-${env.BUILD_ID}"
                            sh """
                            echo 'extracting report and test files...'
                            docker run --name ${tempContainerName}  -d ${imageName} sleep 5000
                            docker cp ${tempContainerName}:/var/www/report/ .
                            docker rm -f ${tempContainerName}
                            """
                        }
                    }
                }
            }
            stage('Pushing image to registry') {
                steps {
                    withCredentials([usernamePassword(credentialsId: 'dockerHub', usernameVariable: 'HUB_USER', passwordVariable: 'HUB_TOKEN')]) {                      
                        sh 'echo $HUB_TOKEN | docker login -u $HUB_USER --password-stdin'
                        sh "docker image push ${imageName}"
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
