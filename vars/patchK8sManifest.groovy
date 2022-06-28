def call(Map params) {
    container("yq") {
        dir("gitops") {
            script {
                def workloadYaml = params.workloadYaml != null ? params.workloadYaml : "deployment.yml"
                sh "yq -i '.spec.template.spec.containers[0].image = \"${params.imageName}\"' ./${params.repoDir}/${workloadYaml}"

                if(params.workloadType == "job") {
                    // yourString.replace("\\", "/")
                    def jobName = params.imageName.replace("/", "-").replace(":", "-")
                    sh "yq -i '.metadata.name = \"${jobName}\"' ./${params.repoDir}/${workloadYaml}"
                }
            }
         }                
     }                
}