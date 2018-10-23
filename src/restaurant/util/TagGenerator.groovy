package restaurant.util

class TagGenerator {
    static def generateImageTag(String buildNumber) {
        if(buildNumber == null || buildNumber.isEmpty()) {
            throw new IllegalArgumentException('buildNumber not passed');
        }
        def dateFormat = (new Date()).format('MMddyyyy')
        return "${dateFormat}_${buildNumber}"
    }
}
