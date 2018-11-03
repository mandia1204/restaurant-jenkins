def call(Map pipelineParams) {
    pipeline {
        agent { label 'slave01' }
        tools {nodejs 'node'}
        stages {
            stage('Install dependencies') {
                steps {
                    sh 'npm install'
                }
            }
            stage('Build') {
                steps {
                    sh 'npm run build'
                }
            }
            stage('Test') {
                steps {
                    ansiColor('xterm') {
                        sh 'npm run test'
                    }
                }
            }
        }
        post {
            always {
                step([$class: "TapPublisher", testResults: "report/test/test.out.tap", outputTapToConsole:false, enableSubtests:true ])
                archiveArtifacts 'dist/**/*'
            }
        }
    }
}