import restaurant.util.*

def call(Map params) {
    pipeline {
        agent { label 'slave01' }
        stages {
            stage('Deploy VPC and Subnets') {
                steps {
                    ansiColor('xterm') {
                        script {
                            def command = "cd ansible/cloud-formation && ansible-playbook main.yml"
                            def sshUtil = new SSHUtil()
                            sshUtil.publish configName: 'ansible', command: command
                        }
                    }
                }
            }
        }
    }
}