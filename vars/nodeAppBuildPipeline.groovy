def call(Map pipelineParams) {
    pipeline {
        tools {nodejs 'node'}
        agent {
            kubernetes {
                containerTemplate {
                    name 'shell'
                    image 'ubuntu'
                    command 'sleep'
                    args 'infinity'
                }
                defaultContainer 'shell'
            }
        }
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
    }
}
