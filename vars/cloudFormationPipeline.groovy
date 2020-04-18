import restaurant.util.*

def call(Map params) {
    pipeline {
        agent { label 'slave01' }
        options {
            timeout(time: 20, unit: 'MINUTES')
        }
        stages {
            stage('Running stack') {
                steps {
                    ansiColor('xterm') {
                        script {
                            def command = "cd ansible/cloud-formation && ansible-playbook main.yml --tags \"${params.tags}\""
                            def sshUtil = new SSHUtil()
                            sshUtil.publish configName: 'ansible', command: command
                        }
                    }
                }
            }
        }
    }
}