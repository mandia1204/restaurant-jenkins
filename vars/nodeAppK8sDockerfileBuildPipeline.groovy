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
                    withCredentials([usernamePassword(credentialsId: 'dockerHub', usernameVariable: 'HUB_USER', passwordVariable: 'HUB_TOKEN')]) {                      
                        sh 'echo $HUB_TOKEN | docker login -u $HUB_USER --password-stdin'
                        sh "docker image push ${imageName}"
                    }
                }
            }
            stage('Updating image tag and pushing to git repo') {
                steps {
                    container('git') {
                        checkout([$class: 'GitSCM', branches: [[name: '*/main']], extensions: [[$class: 'RelativeTargetDirectory', relativeTargetDir: 'gitops']], userRemoteConfigs: [[credentialsId: 'git-key', url: 'git@github.com:mandia1204/argocd-configuration.git']]])
                        dir("gitops") {
                            sh "git config --global --add safe.directory ${WORKSPACE}/gitops"
                            sh 'git config --global user.email "mandia1204@gmail.com"'
                            sh 'git config --global user.name "Marvin Andia"'
                            sh 'git checkout main'
                            sh "sed -i \"/${repoUser}\\/${repoAppName}:/c\\        image: ${imageName}\" ./${params.repoDir}/deployment.yml"
                            sh 'git add .'
                            sh "git commit -m \"Patching image to ${imageName}\""
                            withCredentials([sshUserPrivateKey(credentialsId: 'git-key', keyFileVariable: 'SSH_KEY')]) {
                                sh 'GIT_SSH_COMMAND="ssh -o StrictHostKeyChecking=no -i $SSH_KEY" git push origin main'
                            }
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
