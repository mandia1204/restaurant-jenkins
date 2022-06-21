import restaurant.util.*

def call(Map params) {
    pipeline {
        options { 
            buildDiscarder(logRotator(numToKeepStr: '10')) 
        }
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
                            repoUser = params.repoName.split('/')[0];
                            repoAppName = params.repoName.split('/')[1];
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
                    pushToDockerRegistry imageName
                }
            }
            stage('Updating image tag and pushing to git repo') {
                steps {
                    container('git') {
                        gitopsPush workspace:env.WORKSPACE, repoUser:repoUser, repoAppName:repoAppName, repoDir:params.repoDir, imageName: imageName
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
