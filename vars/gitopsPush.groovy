def call(Map params) {
    checkout([$class: 'GitSCM', branches: [[name: '*/main']], extensions: [[$class: 'RelativeTargetDirectory', relativeTargetDir: 'gitops']], userRemoteConfigs: [[credentialsId: 'git-key', url: 'git@github.com:mandia1204/argocd-configuration.git']]])
    dir("gitops") {
        sh "git config --global --add safe.directory ${params.workspace}/gitops"
        sh 'git config --global user.email "mandia1204@gmail.com"'
        sh 'git config --global user.name "Marvin Andia"'
        sh 'git checkout main'
        sh "sed -i \"/${params.repoUser}\\/${params.repoAppName}:/c\\        image: ${params.imageName}\" ./${params.repoDir}/deployment.yml"
        sh 'git add .'
        sh "git commit -m \"Repo: ${params.repoAppName},patching image to ${params.imageName}\""
        withCredentials([sshUserPrivateKey(credentialsId: 'git-key', keyFileVariable: 'SSH_KEY')]) {
            sh 'GIT_SSH_COMMAND="ssh -o StrictHostKeyChecking=no -i $SSH_KEY" git push origin main'
        }
    }                   
}