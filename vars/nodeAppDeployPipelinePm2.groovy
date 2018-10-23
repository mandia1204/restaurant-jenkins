import restaurant.util.PathUtil

def call(Map pipelineParams) {
    pipeline {
        agent any
        tools {nodejs "localnode"}
        stages {
            stage('Stop service') {
                steps {
                    sh "pm2 stop ${pipelineParams.serviceName}"
                }
            }
            stage('Clean folder') {
                steps {
                    script {
                        //noinspection GroovyAssignabilityCheck
                        svcPath = PathUtil.convertPath(pipelineParams.servicePath)
                        sh "rm -rf /${svcPath}/*"
                    }
                }
            }
            stage('Copy artifact') {
                steps {
                    copyArtifacts(projectName: "${pipelineParams.projectName}", target: "${pipelineParams.servicePath}")
                    script {
                        def distPath = "/${svcPath}/dist"

                        sh "[ -d ${distPath} ] && [ \"\$(ls -A ${distPath})\" ] && mv -v ${distPath}/* /${svcPath} || echo 'Nothing to copy!'"
                        sh "rm -rf ${distPath}"
                    }
                }
            }
            stage('Install dependencies') {
                when { expression { return pipelineParams.skipInstallDependencies } }
                steps {
                    script {
                        sh "(cd /${svcPath} ; npm install --production)"
                    }
                }
            }
            stage('Start service') {
                steps {
                    sh "pm2 start ${pipelineParams.serviceName}"
                }
            }
        }
    }
}