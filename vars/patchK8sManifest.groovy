def call(Map params) {
    container("yq") {
        dir("gitops") {
            sh "yq -i '.spec.template.spec.containers[0].image = \"${params.imageName}\"' ./${params.repoDir}/deployment.yml"
         }                
     }                
}