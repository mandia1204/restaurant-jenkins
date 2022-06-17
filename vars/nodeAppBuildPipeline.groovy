def call(Map pipelineParams) {
    pipeline {
        podTemplate {
            node("node-app-build") {
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
}
