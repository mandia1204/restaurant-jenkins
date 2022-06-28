def call(Map params) {
    container('git') {
        checkout([$class: 'GitSCM', branches: [[name: '*/main']], extensions: [[$class: 'LocalBranch', localBranch: 'main'], [$class: 'RelativeTargetDirectory', relativeTargetDir: 'gitops']], userRemoteConfigs: [[credentialsId: 'git-key', url: 'git@github.com:mandia1204/argocd-configuration.git']]])
        dir("gitops") {
            sh "git config --global --add safe.directory ${params.workspace}/gitops"
            sh 'git config user.email "mandia1204@gmail.com"'
            sh 'git config user.name "Marvin Andia"'
        }
    }                
}