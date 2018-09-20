import org.junit.*
import restaurant.util.PathUtil
import static org.junit.Assert.*

class PathUtilTest {

    @Test
    void convertPath_passingString_returnsConvertedPath() throws  Exception {
        def path = 'c:\\path\\demo'
        def result = PathUtil.convertPath(path)

        assertEquals('c/path/demo', result)
    }
}
