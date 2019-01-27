package restaurant.util

def publish(Map params) {
    sshPublisher(publishers:
            [sshPublisherDesc(
                    configName: params.configName,
                    transfers: [
                            sshTransfer(
                                    cleanRemote: false,
                                    excludes: '',
                                    execCommand: params.command,
                                    execTimeout: 120000,
                                    flatten: false,
                                    makeEmptyDirs: false,
                                    noDefaultExcludes: false,
                                    patternSeparator: '[, ]+',
                                    remoteDirectory: params.dir,
                                    remoteDirectorySDF: false,
                                    removePrefix: params.removePrefix,
                                    sourceFiles: params.sourceFiles,
                                    usePty: true)
                    ],
                    usePromotionTimestamp: false,
                    useWorkspaceInPromotion: false,
                    verbose: false)
            ]
    )
}

return this

