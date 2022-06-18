def call(Map pipelineParams) {
    pipeline {
        agent {
            kubernetes {
                yaml '''
apiVersion: v1
kind: Pod
spec:
  nodeSelector:
    kubernetes.io/hostname: k8s-node3
  volumes:
  - name: sock-volume
    hostPath:
      path: /var/run/docker.sock
  containers:
  - name: shell
    imagePullPolicy: IfNotPresent
    image: alpinelinux/docker-cli
    volumeMounts:
      - name: sock-volume
        mountPath: /var/run/docker.sock
    command:
    - sleep
    args:
    - infinity
'''
                defaultContainer 'shell'
            }
        }
        stages {
            stage('Build image') {
                steps {
                    // checkout([$class: 'GitSCM', branches: [[name: '*/master']], extensions: [], userRemoteConfigs: [[credentialsId: 'git-key', url: 'git@github.com:mandia1204/restaurant-security.git']]])
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
