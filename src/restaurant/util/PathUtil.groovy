package restaurant.util

class PathUtil {
    static def convertPath(String path){
        return path.replace(':\\','/').replace('\\','/')
    }
}
