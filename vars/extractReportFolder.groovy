def call(Map params) {
    script {
        def tempContainerName = "tmp-copy-${params.buildId}"
        sh """
        echo 'extracting report and test files...'
        docker run --name ${tempContainerName}  -d ${params.imageName} sleep 5000
        docker cp ${tempContainerName}:/var/www/report/ .
        docker rm -f ${tempContainerName}
        """
    }  
}