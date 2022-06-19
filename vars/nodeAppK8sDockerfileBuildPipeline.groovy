def call(Map pipelineParams) {
    pipeline {
        agent {
            kubernetes {
                yaml readFile('k8s-podspec.yaml')
                defaultContainer 'shell'
            }
        }
        stages {
            stage('Build image') {
                steps {
                    sh 'ls -l'
                    script {
                        def dockerfile = 'Dockerfile-dev'
                        def customImage = docker.build("nodeapp:${env.BUILD_ID}", "-f ${dockerfile} .")

                        customImage.inside {
                            sh 'node -v'
                        }
                    }
                    sh 'docker version'
                }
            }
        }
        post {
            always {
                step([$class: "TapPublisher", testResults: "report/test/test.out.tap", outputTapToConsole:false, enableSubtests:true ])
                junit keepLongStdio: true, testResults: 'report/junit/*.xml'
                archiveArtifacts 'dist/**/*'
            }
        }
    }
}
