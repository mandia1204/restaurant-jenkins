def call(Map pipelineParams) {
    pipeline {
        agent { label 'slave01' }
        stages {
            stage('Restore packages') {
                steps {
                    sh "dotnet restore ${pipelineParams.projectSrcPath}"
                }
            }
            stage('Build') {
                steps {
                    sh "dotnet restore ${pipelineParams.projectSrcPath}"
                }
            }
            stage('Test') {
                steps {
                    ansiColor('xterm') {
                        sh "dotnet test ${pipelineParams.projectSrcPath}"
                    }
                }
            }
            stage('Build release') {
                steps {
                    sh "dotnet publish ${pipelineParams.projectSrcPath} -c Release -o ../dist"
                }
            }
        }
        post {
            always {
                archiveArtifacts 'dist/**/*'
            }
        }
    }
}

