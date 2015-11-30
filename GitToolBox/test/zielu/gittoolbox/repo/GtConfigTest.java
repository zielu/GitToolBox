package zielu.gittoolbox.repo;

import static org.junit.Assert.*;

import java.io.File;
import org.junit.BeforeClass;
import org.junit.Test;

public class GtConfigTest {
    private static GtConfig config;

    @BeforeClass
    public static void beforeClass() {
        File currentDir = new File(".").getAbsoluteFile();
        File testConfig = new File(currentDir, "testData"+File.separator+"GtConfig"+File.separator+"config");
        config = GtConfig.load(testConfig);
    }

    @Test
    public void isSvnRemote() throws Exception {
        assertTrue(config.isSvnRemote("svn"));
    }

    @Test
    public void isRegularRemote() throws Exception {
        assertFalse(config.isSvnRemote("origin"));
    }
}