def call(Map params) {
    container('git') {
        dir("gitops") {
            sh 'git add .'
            sh "git commit -m \"Repo: ${params.repoAppName},patching image to ${params.imageName}\""
            withCredentials([sshUserPrivateKey(credentialsId: 'git-key', keyFileVariable: 'SSH_KEY')]) {
                sh 'GIT_SSH_COMMAND="ssh -o StrictHostKeyChecking=no -i $SSH_KEY" git push origin main'
            }
        }
    }                
}