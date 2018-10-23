import org.junit.*
import restaurant.util.TagGenerator
import static org.junit.Assert.*

class TagGeneratorTest {
    @Test
    void generateImageTag_passingBuildNumber_returnsTag() throws  Exception {
        def buildNumber = '123'
        def result = TagGenerator.generateImageTag(buildNumber)

        assertNotNull(result)
        assertTrue(result.endsWith("_${buildNumber}"))
        assertEquals(9 + buildNumber.length(), result.length())
    }

    @Test(expected = IllegalArgumentException.class)
    void generateImageTag_passingNullBuildNumber_throwsException() throws  Exception {
        TagGenerator.generateImageTag(null)
    }

    @Test(expected = IllegalArgumentException.class)
    void generateImageTag_passingEmptyBuildNumber_throwsException() throws  Exception {
        TagGenerator.generateImageTag('')
    }
}
