def call(String imageName) {
    withCredentials([usernamePassword(credentialsId: 'dockerHub', usernameVariable: 'HUB_USER', passwordVariable: 'HUB_TOKEN')]) {          
        sh 'echo $HUB_TOKEN | docker login -u $HUB_USER --password-stdin'
        sh "docker image push ${imageName}"
    }  
}